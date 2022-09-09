package com.autumn.spider;

import com.autumn.spider.middleware.Proxy;
import com.autumn.spider.middleware.RequestBuilder;
import com.autumn.spider.persistence.Persistence;
import com.autumn.spider.resolver.ToDataResolver;
import com.autumn.spider.resolver.ToURIsResolver;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Spider {
    private static DataEngine engine;
    private Spider(){}

    public static <T>  void initWithProxyAndRequestBuilder(
            String startUrl, Proxy proxy, RequestBuilder requestBuilder,
            LinkedList<ToURIsResolver> toURIsResolvers, ToDataResolver<T> dataResolver, Persistence<T> persistence,TimeUnit unit,long sleepTime) throws IOException, ExecutionException, InterruptedException {
        engine=new DataEngine();
        int core=Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor=new ThreadPoolExecutor(
                core,core*5, 1,TimeUnit.SECONDS,new LinkedBlockingQueue<>()
        );
        RequestSender sender=new RequestSender(proxy,requestBuilder,executor);
        engine.init(startUrl,sender,toURIsResolvers,dataResolver,persistence,unit,sleepTime);
    }

    public static void start(){
        engine.start();
    }

    public static <T> void initWithProxy(
            String startUrl, Proxy proxy,
            LinkedList<ToURIsResolver> toURIsResolvers, ToDataResolver<T> dataResolver, Persistence<T> persistence,TimeUnit unit,long sleepTime) throws IOException, ExecutionException, InterruptedException {
        initWithProxyAndRequestBuilder(startUrl, proxy,null,toURIsResolvers,dataResolver,persistence,unit,sleepTime);

    }

    public static <T> void initWithRequestBuilder(
            String startUrl, RequestBuilder requestBuilder,
            LinkedList<ToURIsResolver> toURIsResolvers, ToDataResolver<T> dataResolver,Persistence<T> persistence,TimeUnit unit,long sleepTime) throws IOException, ExecutionException, InterruptedException {
        initWithProxyAndRequestBuilder(startUrl,null,requestBuilder,toURIsResolvers,dataResolver,persistence,unit,sleepTime);
    }

    public static <T> void init(
            String startUrl, LinkedList<ToURIsResolver> toURIsResolvers,
            ToDataResolver<T> dataResolver,Persistence<T> persistence,TimeUnit unit,long sleepTime) throws IOException, ExecutionException, InterruptedException {
        initWithProxyAndRequestBuilder(startUrl,null,null,toURIsResolvers,dataResolver,persistence,unit,sleepTime);
    }
}
