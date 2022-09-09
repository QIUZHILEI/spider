package com.autumn.spider;

import com.autumn.spider.persistence.Persistence;
import com.autumn.spider.resolver.ResolverUtils;
import com.autumn.spider.resolver.ToDataResolver;
import com.autumn.spider.resolver.ToURIsResolver;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.concurrent.*;

//T代表最终数据类型
class DataEngine {

    private ConcurrentLinkedQueue<CompletableFuture<HttpResponse<String>>> finalData;
    private Semaphore finalSemaphore;
    private Node head;

    private Node tail;

    private Thread persistenceThread;


    //U 解析产生的类型
    static class Node{
        private final Semaphore semaphore;
        private final Node pre;
        private volatile boolean complete;
        private Thread worker;
        private final ConcurrentLinkedQueue<CompletableFuture<HttpResponse<String>>> futures;
        private static int step=0;

        Node(Node prev){
            semaphore =new Semaphore(0);
            pre=prev;
            futures =new ConcurrentLinkedQueue<>();
        }

        public void initTask(ToURIsResolver resolver, RequestSender sender,TimeUnit unit,long sleepTime) {
            worker=new Thread(()->{
                while (!pre.complete|| !pre.futures.isEmpty()){
                    try{
                        if (!pre.futures.isEmpty()) {
                            try {
                                pre.semaphore.acquire();
                            }catch (InterruptedException e){
                                e.printStackTrace();
                                complete=true;
                            }

                            CompletableFuture<HttpResponse<String>> future = pre.futures.poll();

                            HttpResponse<String> response = future.get();
                            if (response.statusCode()!=200) {
                                System.out.println(response.uri().toString()+"\t failed with "+response.statusCode());
                                continue;
                            }
                            LinkedList<HttpRequest.Builder> builders = ResolverUtils.toRequestsBuilder(response, resolver);
                            for (HttpRequest.Builder builder : builders) {
                                unit.sleep(sleepTime);
                                this.futures.add(sender.sendRequest(builder));
                                semaphore.release();
                            }
                        }
                    }catch (InterruptedException |ExecutionException  e) {
                        e.printStackTrace();
                    }
                }
                complete=true;
            },"step-"+(++Node.step));
        }

        public void start(){
            worker.start();
        }
    }


    public <T>  void init(String uri, RequestSender sender, LinkedList<ToURIsResolver> resolvers, ToDataResolver<T> dataResolver, Persistence<T> persistence,TimeUnit unit,long sleepTime) throws IOException, InterruptedException{
        head=new Node(null);
        Node prev=head;
        for (ToURIsResolver resolver:resolvers){
            Node node=new Node(prev);
            node.initTask(resolver,sender,unit,sleepTime);
            prev=node;
        }
        tail=prev;
        finalSemaphore=tail.semaphore;
        finalData=tail.futures;
        initHead(sender,URI.create(uri));
        initPersistence(persistence,sender,dataResolver);
    }

    private void initHead(RequestSender sender,URI uri) throws IOException, InterruptedException {
        HttpRequest.Builder builder=HttpRequest.newBuilder().GET().uri(uri);
        CompletableFuture<HttpResponse<String>> future = sender.sendRequest(builder);
        head.futures.add(future);
        head.semaphore.release();
        head.complete=true;
    }

    private <T> void initPersistence(Persistence<T> persistence,RequestSender sender,ToDataResolver<T> resolver){
        persistenceThread=new Thread(()->{
            while (!tail.complete||!finalData.isEmpty()){
                try {
                    if (!finalData.isEmpty()) {
                        try {
                            finalSemaphore.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            sender.destroyExecutor();
                            try {
                                persistence.closeResources();
                            } catch (IOException ex) {
                                System.out.println(ex.getMessage());
                            }
                            System.exit(130);
                        }
                        CompletableFuture<HttpResponse<String>> future = finalData.poll();
                        HttpResponse<String> response = future.get();
                        if (response.statusCode()!=200) {
                            System.out.println(response.uri().toString()+"\t failed with "+response.statusCode());
                            continue;
                        }
                        T item = resolver.resolver(response);
                        persistence.persistence(item);
                    }
                } catch (InterruptedException | ExecutionException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                    sender.destroyExecutor();
                    try {
                        persistence.closeResources();
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                    System.exit(130);
                }

            }
            sender.destroyExecutor();
            try {
                persistence.closeResources();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        },"Persistence-Thread");
    }

    public void start(){
        persistenceThread.start();
        Node node=tail;
        do{
            node.start();
            node=node.pre;
        }while (node!=head);
    }
}
