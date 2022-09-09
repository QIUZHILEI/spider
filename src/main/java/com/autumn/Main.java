package com.autumn;

import com.autumn.spider.Spider;
import com.autumn.spider.middleware.RequestBuilder;
import com.autumn.spider.persistence.Persistence;
import com.autumn.spider.resolver.ToDataResolver;
import com.autumn.spider.resolver.ToURIsResolver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {
    private static final String startUrl = "https://book.dangdang.com/";
    private static final String filePath = "books.csv";
    private static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36";
    private static final String cookie = "__permanent_id=20220903210747955283874957293190820; secret_key=2d0a0efc2ef375c202ade4a8924e8049; ddscreen=2; dest_area=country_id=9000&province_id=111&city_id=0&district_id=0&town_id=0; bind_cust_third_id=2088822040401364; bind_custid=282825693; sessionID_temp=pc_cbf1e8301d17bb8e74a1710f8718524340cb9df16d4702d1f6056c4cae89e992; __visit_id=20220909085309798110505700378662570; __out_refer=1662684790|!|memberexprod.alipay.com|!|; bind_mobile=18304935041; mobile_custid=284872288; login.dangdang.com=.AYH=&.ASPXAUTH=etOlqOrgMBAOfDS/J8cfJoucppKMeVbr32v4USJfME+5nQiADF+VgA==; dangdang.com=email=N2VmOGQ2MDZmZWJjMDlkNkB0YW9iYW9fdXNlci5jb20=&nickname=&display_id=5769748054492&customerid=pQ8W+VeSXMXZCjDefvMjAA==&viptype=q5zpA3O9WKg=&show_name=7ef8d606febc09d6; ddoy=email=7ef8d606febc09d6@taobao_user.com&nickname=&agree_date=1&validatedflag=2&uname=18304935041&utype=1&.ALFG=1&.ALTM=1662684810; sessionID=pc_43e4752841963cf3edf4d2c72347d6bfb3a6ad8fe0f867e20cbbfb44291aa769; order_follow_source=-|-O-123|#11|#login_third_alipay|#0|#; LOGIN_TIME=1662685433864; __rpm=|p_1876880150...1662685924976; __trace_id=20220909091205060223977816858420284";
    private static final String api = "http://api.xdaili.cn/xdaili-api//greatRecharge/getGreatIp?spiderId=6c47a6ee752b41b49c961400c20a0dfc&orderno=YZ2022999609jnvbuB&returnType=1&count=20";

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        RequestBuilder requestBuilder = requestBuilder();
        LinkedList<ToURIsResolver> urIsResolvers = new LinkedList<>();
        urIsResolvers.add(firstStep());
        urIsResolvers.add(secondStep());
        urIsResolvers.add(thirdStep());
        Persistence<Book> persistence = new ToFile(filePath);
        ToDataResolver<Book> dataResolver = toBook();
        //ProxyPool proxyPool = new ProxyPool(api);
        Spider.initWithRequestBuilder(startUrl, requestBuilder, urIsResolvers, dataResolver, persistence, TimeUnit.MILLISECONDS, 100);
        //Spider.initWithProxyAndRequestBuilder();
        Spider.start();
    }

    public static RequestBuilder requestBuilder() {
        return (builder) -> builder
                .version(HttpClient.Version.HTTP_1_1)
                .header("User-Agent", userAgent)
                .header("cookie", cookie)
                .build();
    }

    public static ToURIsResolver firstStep() {
        return (response) -> {
            String[] categories = {
                    "http://category.dangdang.com/cp01.52.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.55.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.56.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.54.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.66.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.62.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.63.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.22.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.24.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.25.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.36.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.34.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.32.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.31.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.28.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.27.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.30.00.00.00.00.html",
                    "http://category.dangdang.com/cp01.26.00.00.00.00.html"
            };
            LinkedList<URI> uris = new LinkedList<>();
            for (String category : categories) {
                URI uri = URI.create(category);
                uris.add(uri);
            }
            return uris;
        };
    }

    public static ToURIsResolver secondStep() {
        return (response) -> {
            String prefix = "http://category.dangdang.com/pg";
            String link = "-" + response.uri().toString().substring(29);
            LinkedList<URI> uris = new LinkedList<>();
            Stream.iterate(1, (i) -> i + 1).limit(50)
                    .forEach(integer -> {
                        URI uri = URI.create(prefix + integer + link);
                        uris.add(uri);
                    });
            return uris;
        };
    }

    public static ToURIsResolver thirdStep() {
        return (response) -> {
            Matcher matcher = Pattern.compile("(?<=d" +
                    "d_name=\"单品图片\" href=\"//product.dangdang.com/)[0-9]+.html").matcher(response.body());
            LinkedList<URI> uris = new LinkedList<>();
            String prefix = "http://product.dangdang.com/";
            while (matcher.find()) {
                URI uri = URI.create(prefix + matcher.group());
                uris.add(uri);
            }
            return uris;
        };
    }

    public static ToDataResolver<Book> toBook() {
        return (response) -> {
            try {
                String body = response.body();
                Matcher isbnMatch = Pattern.compile("(?<=国际标准书号ISBN：)[0-9]+").matcher(body);
                String isbn = "";
                if (isbnMatch.find()) {
                    isbn = isbnMatch.group();
                }
                Document document = Jsoup.parse(response.body());
                String category = document.getElementById("breadcrumb").getElementsByTag("a").get(1).text();
                String imgUrl = "http:" + document.getElementById("largePic").attr("src");
                String author = document.getElementById("author").getElementsByTag("a").text();
                Matcher publisherMatch = Pattern.compile("(?<=dd_name=\"出版社\">).+(?=</a>)").matcher(body);
                String publisher = "";
                if (publisherMatch.find()) {
                    publisher = publisherMatch.group();
                }
                Matcher publishTimeMatch = Pattern.compile("(?<=出版时间:).+(?=&)").matcher(body);
                String publishTime = "";
                if (publishTimeMatch.find()) {
                    publishTime = publishTimeMatch.group();
                }
                Matcher nameMatch = Pattern.compile("(?<=<h1 title=\").+(?=\">)").matcher(body);
                String name = "";
                if (nameMatch.find()) {
                    name = nameMatch.group();
                }
                double price = Double.parseDouble(document.getElementById("dd-price").text().trim().replace("¥", ""));
                return new Book(isbn, category, name, imgUrl, author, publisher, publishTime, price);
            } catch (Exception e) {
                System.out.println("book resolver fail!");
                return null;
            }
        };
    }
}
