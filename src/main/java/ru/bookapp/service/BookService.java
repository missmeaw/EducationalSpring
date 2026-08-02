package ru.bookapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookapp.model.Book;
import ru.bookapp.repository.AuthorRepository;
import ru.bookapp.repository.BookRepository;

import java.util.List;

/**
 * Сервис добавления/изменения/чтения книг
 */
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Autowired
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @Transactional
    public void addBook(Book book) {
        bookRepository.addBook(book);
    }

    public List<Book> listBooks() {
        return bookRepository.findAllBooks();
    }

    public List<Book> findBooks(String searchTerm) {
        return bookRepository.findBooksByTitle(searchTerm);
    }

    @Transactional
    public void transferBook(Long bookId, Long newAuthorId) {
        if (!bookRepository.bookExists(bookId)) {
            throw new IllegalArgumentException("Книга с ID " + bookId + " не найдена");
        }
        if (authorRepository.authorNotExists(newAuthorId)) {
            throw new IllegalArgumentException("Автор с ID " + newAuthorId + " не найден");
        }
        bookRepository.updateBookAuthor(bookId, newAuthorId);
    }
}
