package ru.bookapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bookapp.model.Book;
import ru.bookapp.repository.BookRepository;

import java.util.List;

/**
 * Сервис добавления/изменения/чтения книг
 */
@Service
public class BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void initializeDatabase() {
        bookRepository.initTable();
    }

    public void addBook(String title, String author, int year) {
        Book book = new Book(1L, title, author, year);
        bookRepository.addBook(book);
        System.out.println("Книга успешно добавлена!");
    }

    public void listBooks() {
        List<Book> books = bookRepository.findAllBooks();
        if (books.isEmpty()) {
            System.out.println("Библиотека пуста.");
        } else {
            System.out.println("\n=== Список книг ===");
            books.forEach(System.out::println);
            System.out.println("Всего книг: " + books.size());
        }
    }

    public void findBooks(String searchTerm) {
        List<Book> books = bookRepository.findBooksByTitle(searchTerm);
        if (books.isEmpty()) {
            System.out.println("Книги по запросу '" + searchTerm + "' не найдены.");
        } else {
            System.out.println("\n=== Результаты поиска по '" + searchTerm + "' ===");
            books.forEach(System.out::println);
            System.out.println("Найдено книг: " + books.size());
        }
    }
}
