package com.autumn.spider.resolver;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;

public class ResolverUtils {

    private ResolverUtils(){}
    public static LinkedList<HttpRequest.Builder> toRequestsBuilder(HttpResponse<String> response, ToURIsResolver resolver){
        LinkedList<URI> uris = resolver.resolver(response);
        LinkedList<HttpRequest.Builder> result=new LinkedList<>();
        uris.forEach(uri -> result.add(HttpRequest.newBuilder(uri).GET()));
        return result;
    }

    public static <T> T toData(HttpResponse<String> response,ToDataResolver<T> resolver){
        return resolver.resolver(response);
    }

}
