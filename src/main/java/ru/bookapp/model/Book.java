package ru.bookapp.model;

/**
 * Модель данных Книга
 */


public class Book {

    public Book() {
    }

    public Book(String title, String author, int year) {
        this.title = title;
        this.author = author;
        this.year = year;
    }

    public Book(String title, int year, Long authorId) {
        this.title = title;
        this.year = year;
        this.authorId = authorId;
    }

    public Book(Long id, String title, int year, Long authorId) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.authorId = authorId;
    }

    public Book(Long id, String title, String author, int year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
    }

    private Long id;
    private String title;
    private String author;
    private int year;
    private Long authorId;

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    @Override
        public String toString() {
            return String.format("ID: %d | %s by %s (%d)", id, title, author, year);
        }
}
