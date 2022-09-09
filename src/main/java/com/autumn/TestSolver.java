package com.autumn;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TestSolver {
    public static void main(String[] args) throws IOException, InterruptedException {
        String api="http://api.xdaili.cn/xdaili-api//greatRecharge/getGreatIp?spiderId=6c47a6ee752b41b49c961400c20a0dfc&orderno=YZ2022999609jnvbuB&returnType=1&count=20";
        HttpClient client=HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder().uri(URI.create(api))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String[] split = response.body().split("\n");
        for (String address:split){
            String[] hp = address.split(":");
            System.out.println();
        }

    }
}
