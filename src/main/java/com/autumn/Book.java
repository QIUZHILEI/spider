package com.autumn;

public class Book {
    private final String ISBN;
    private final String category;
    private final String name;
    private final String imgUrl;
    private final String author;
    private final String publisher;
    private final String publishTime;
    private final double price;

    public Book(String ISBN, String category, String name, String imgUrl, String author, String publisher, String publishTime, double price) {
        this.ISBN = ISBN;
        this.category = category;
        this.name = name;
        this.imgUrl = imgUrl;
        this.author = author;
        this.publisher = publisher;
        this.publishTime = publishTime;
        this.price = price;
    }

    @Override
    public String toString() {
        return ISBN + ',' +
                category + ',' +
                name + ',' +
                imgUrl + ',' +
                author + ',' +
                publisher + ',' +
                publishTime + ',' +
                price;
    }
}
