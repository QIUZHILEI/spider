package com.autumn.spider.resolver;


import java.net.URI;
import java.net.http.HttpResponse;
import java.util.LinkedList;

public interface ToURIsResolver{
    LinkedList<URI> resolver(HttpResponse<String> response);
}
