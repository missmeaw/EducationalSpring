package ru.bookapp.model;

import jakarta.persistence.*;

/**
 * Модель данных Книга
 */

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "year")
    private int year;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    public Book() {
    }

    public Book(String title, int year) {
        this.title = title;
        this.year = year;
    }

    public Book(String title, int year, Author author) {
        this.title = title;
        this.year = year;
        this.author = author;
    }

    public Book(Long id, String title, int year, Author author) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.author = author;
    }

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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Long getAuthorId() {
        return author != null ? author.getId() : null;
    }

    public void setAuthorId(Long authorId) {
        if (authorId != null) {
            Author author = new Author();
            author.setId(authorId);
            this.author = author;
        }
    }

    @Override
    public String toString() {
        return String.format("ID: %d | %s %s г. %s",
                id, title, year, author.getName());
    }
}
