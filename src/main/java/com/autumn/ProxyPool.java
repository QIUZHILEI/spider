package com.autumn;

import com.autumn.spider.middleware.Proxy;

import java.net.InetSocketAddress;
import java.net.ProxySelector;

public class ProxyPool implements Proxy {
    /*private HttpClient client;
    private LinkedList<Address> addresses;
    private String api;

    static class Address{
        String host;
        int port;

        public Address(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }
*/
    private ProxySelector selector;
    public ProxyPool(String api) {
        /*addresses=new LinkedList<>();
        client=HttpClient.newHttpClient();
        this.api = api;*/
        selector=ProxySelector.of(InetSocketAddress.createUnresolved("127.0.0.1",7890));
    }

    @Override
    public ProxySelector selector() {
        /*if (addresses.isEmpty()){
            HttpRequest request = HttpRequest.newBuilder(URI.create(api)).GET().build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String[] split = response.body().split("\n");
                for (String address:split){
                    String[] hp = address.split(":");
                    Address newAddress=new Address(hp[0].trim(),Integer.parseInt(hp[1].trim()));
                    addresses.add(newAddress);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        Address address=addresses.poll();
        System.out.println(address.host+":"+address.port);
        ProxySelector selector=ProxySelector.of(InetSocketAddress.createUnresolved(address.host,address.port));*/
        return selector;
    }

    @Override
    public long milliTimeInterval() {
        return PERMANENT;
    }
}
