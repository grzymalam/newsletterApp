package zad1.Server;

import java.util.ArrayList;

public class News {
    private String data;
    private int ownId;
    private static int id = 0;
    public News(String data) {
        ownId = id++;
        this.data = data;
    }
    public int getId() {
        return ownId;
    }
    public String getData() {
        return data;
    }
}
