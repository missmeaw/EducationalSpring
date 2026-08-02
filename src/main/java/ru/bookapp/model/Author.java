package ru.bookapp.model;

import java.util.List;

/**
 * Модель данных Автор
 */
public class Author {
    private Long id;
    private String name;
    private String biography;
    private List<Book> books;

    public Author() {
    }

    public Author(String name, String biography) {
        this.name = name;
        this.biography = biography;
    }

    public Author(Long id, String name, String biography) {
        this.id = id;
        this.name = name;
        this.biography = biography;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Автор: ").append(name);
        if (biography != null && !biography.isEmpty()) {
            sb.append(" (Биография: ").append(biography).append(")");
        }
        sb.append("\nКниги:");
        if (books == null || books.isEmpty()) {
            sb.append(" нет");
        } else {
            books.forEach(book -> sb.append("\n  - ").append(book));
        }
        return sb.toString();
    }
}
