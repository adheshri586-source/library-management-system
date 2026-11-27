package library.backend;

import java.io.Serializable;

public final class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String id, title, author, category;
    public int total, available;

    public Book(String id, String title, String author, String category, int total, int available) {
        this.id = id; this.title = title; this.author = author; this.category = category;
        this.total = total; this.available = available;
    }
}
