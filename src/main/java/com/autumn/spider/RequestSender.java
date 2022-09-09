package com.autumn.spider;

import com.autumn.spider.middleware.Proxy;
import com.autumn.spider.middleware.ProxyHandler;
import com.autumn.spider.middleware.RequestBuilder;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

class RequestSender{
    private HttpClient client;
    private final ProxyHandler proxyHandler;
    private final RequestBuilder requestBuilderHandler;
    private final ThreadPoolExecutor executor;
    private final boolean proxied;
    private HttpClient.Builder oldBuilder;
    private final ReentrantLock lock;

    public RequestSender(Proxy proxy, RequestBuilder requestBuilderHandler, ThreadPoolExecutor executor) {
        if (proxy==null) {
            proxyHandler=null;
            proxied=false;
            client=HttpClient.newBuilder().executor(executor).build();
        }else {
            this.proxyHandler = new ProxyHandler(proxy);
            proxyHandler.start();
            proxied=true;
        }
        this.requestBuilderHandler = requestBuilderHandler;
        this.executor=executor;
        this.lock=new ReentrantLock(true);
    }

    public CompletableFuture<HttpResponse<String>> sendRequest(HttpRequest.Builder requestBuilder) throws InterruptedException {
        client=newClient();
        HttpRequest request= buildRequest(requestBuilder);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest buildRequest(HttpRequest.Builder builder){
        if (requestBuilderHandler ==null){
            return builder
                    .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36")
                    .build();
        }else {
            return requestBuilderHandler.beforeRequest(builder);
        }
    }

    private HttpClient newClient(){
        HttpClient.Builder newBuilder;
        if (proxied){
            lock.lock();
            newBuilder=proxyHandler.newClientBuilder();
            if (newBuilder!=oldBuilder){

                client=newBuilder.executor(executor).build();
                oldBuilder=newBuilder;
            }
            lock.unlock();
        }
        return client;
    }

    public void destroyExecutor(){
        executor.shutdown();
        if (proxyHandler!=null){
            proxyHandler.stop();
        }
    }

}
