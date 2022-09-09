package com.autumn;

import com.autumn.spider.persistence.Persistence;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ToFile implements Persistence<Book> {
    private final BufferedWriter writer;

    public ToFile(String filePath){
        try {
            writer=new BufferedWriter(new FileWriter(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void persistence(Book data) throws IOException {
        if (data!=null) {
            System.out.println(data);
            writer.flush();
            writer.write(data.toString());
            writer.newLine();
        }
    }

    @Override
    public void closeResources() throws IOException {
        writer.close();
    }
}
