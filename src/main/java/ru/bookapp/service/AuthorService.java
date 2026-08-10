package ru.bookapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookapp.model.Author;
import ru.bookapp.model.Book;
import ru.bookapp.repository.AuthorRepository;
import ru.bookapp.repository.BookRepository;

import java.util.List;
import java.util.Optional;

/**
 * Сервис добавления/изменения/чтения авторов
 */
@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        authorRepository.initTable();
    }

    public Long createAuthor(String authorName, String biography) {
        // Проверяем, существует ли уже автор
        if (authorRepository.authorExistsByName(authorName)) {
            throw new IllegalArgumentException("Автор с именем '" + authorName + "' уже существует");
        }

        // Создаем автора
        Author author = new Author(authorName, biography);
        return authorRepository.addAuthor(author);

    }

    @Transactional
    public void createAuthorWithBooks(String authorName, String biography, List<Book> books) {
        Long authorId = createAuthor(authorName, biography);

        // Добавляем все книги
        for (Book book : books) {
            book.setAuthorId(authorId);

            // Для проверки rollback - если книга называется "ERROR", выбрасываем исключение
            if ("ERROR".equalsIgnoreCase(book.getTitle())) {
                throw new RuntimeException("Тестовая ошибка при добавлении книги '" + book.getTitle() + "'");
            }

            bookRepository.addBook(book);
        }
    }

    public Optional<Author> findAuthorById(Long id) {
        Optional<Author> author = authorRepository.findAuthorById(id);

        // Загружаем книги автора
        if (author.isPresent()) {
            List<Book> books = bookRepository.findBooksByAuthorId(id);
            author.get().setBooks(books);
        }

        return author;
    }

    public void deleteAuthor(Long authorId) {
        if (authorRepository.authorNotExists(authorId)) {
            throw new IllegalArgumentException("Автор с ID " + authorId + " не найден");
        }
        List<Book> books = bookRepository.findBooksByAuthorId(authorId);
        if (!books.isEmpty()) {
            throw new IllegalStateException("Нельзя удалить автора, у которого есть книги. Сначала удалите все книги автора.");
        }
        authorRepository.deleteAuthor(authorId);
    }

    public List<Author> getAllAuthors() {
        List<Author> authors = authorRepository.findAllAuthors();
        for (Author author : authors) {
            // Загружаем книги автора
            List<Book> books = bookRepository.findBooksByAuthorId(author.getId());
            author.setBooks(books);
        }
        return authors;
    }

    public Optional<Author> findAuthorByName(String authorName) {
        return authorRepository.findAuthorByName(authorName);
    }
}
