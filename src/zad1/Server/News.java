package zad1.Server;

import java.util.ArrayList;

public class News {
    private String data;
    private static int id = 0;
    public News( String data) {
        this.id = id++;
        this.data = data;
    }
    public int getId() {
        return id;
    }
    public String getData() {
        return data;
    }
}
