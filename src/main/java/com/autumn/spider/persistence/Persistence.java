package com.autumn.spider.persistence;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

//实现这个接口 定义持久化行为
public interface Persistence <T>{
    void persistence(T data) throws IOException;
    void closeResources() throws IOException;
}
