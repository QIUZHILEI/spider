package com.autumn.spider.middleware;

import java.net.http.HttpRequest;

public interface RequestBuilder {
    HttpRequest beforeRequest(HttpRequest.Builder builder);
}
