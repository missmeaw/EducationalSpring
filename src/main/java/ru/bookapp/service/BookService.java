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

    public void addBook(Book book) {
        // Проверяем, есть ли автор
        if (book.getAuthorId() != null && authorRepository.authorNotExists(book.getAuthorId())) {
            throw new IllegalArgumentException("Автор с ID " + book.getAuthorId() + " не найден");
        }
        bookRepository.save(book);
    }

    public List<Book> listBooks() {
        return bookRepository.findAllWithAuthors();
    }

    public List<Book> findBooks(String searchTerm) {
        return bookRepository.findByTitleContainingIgnoreCase(searchTerm);
    }

    @Transactional
    public void transferBook(Long bookId, Long newAuthorId) {
        if (!bookRepository.existsById(bookId)) {
            throw new IllegalArgumentException("Книга с ID " + bookId + " не найдена");
        }
        if (authorRepository.authorNotExists(newAuthorId)) {
            throw new IllegalArgumentException("Автор с ID " + newAuthorId + " не найден");
        }
        bookRepository.updateBookAuthor(bookId, newAuthorId);
    }

    public List<Book> findBooksByTitleAndAuthor(String titlePart, String authorName) {
        return bookRepository.findBooksByTitleAndAuthor(titlePart, authorName);
    }
}
