package com.autumn.spider.middleware;

import java.net.http.HttpClient;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ProxyHandler {
    private volatile HttpClient.Builder builder;
    private final Proxy proxy;
    private Thread handler;
    public ProxyHandler(Proxy proxy) {
        this.proxy = proxy;
    }

    public HttpClient.Builder newClientBuilder(){
        while (builder == null) {
            Thread.onSpinWait();
        }
        return builder;
    }

    public void start(){
        handler=new Thread(()->{
            long timeInterval=proxy.milliTimeInterval();
            while (!handler.isInterrupted()){
                builder=HttpClient.newBuilder().proxy(proxy.selector());
                if (timeInterval==Proxy.PERMANENT){
                    break;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(timeInterval);
                } catch (InterruptedException e) {
                    System.out.println("Proxy handler crash!");
                    throw new RuntimeException(e);
                }
            }
        },"Proxy-Thread");
        handler.start();
    }

    public void stop(){
        if (handler.isAlive()) {
            handler.interrupt();
        }
    }
}
