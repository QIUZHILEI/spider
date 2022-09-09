package com.autumn.spider.middleware;

import java.net.ProxySelector;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

public interface Proxy {
    long PERMANENT=-1;
    ProxySelector selector();
    long milliTimeInterval();
}
