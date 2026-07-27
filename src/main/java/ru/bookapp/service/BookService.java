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
        bookRepository.initTable();
    }

    public void addBook(Book book) {
        bookRepository.addBook(book);
    }

    public List<Book> listBooks() {
        return bookRepository.findAllBooks();
    }

    public List<Book> findBooks(String searchTerm) {
        return bookRepository.findBooksByTitle(searchTerm);
    }
}
