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

    @Transactional
    public void createAuthorWithBooks(String authorName, String biography, List<Book> books) {
        // Проверяем, существует ли уже автор
        if (authorRepository.authorExistsByName(authorName)) {
            throw new IllegalArgumentException("Автор с именем '" + authorName + "' уже существует");
        }

        // Создаем автора
        Author author = new Author(authorName, biography);
        Long authorId = authorRepository.addAuthor(author);

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
        return authorRepository.findAuthorById(id);
    }

    public void deleteAuthor(Long authorId) {
        if (!authorRepository.authorExists(authorId)) {
            throw new IllegalArgumentException("Автор с ID " + authorId + " не найден");
        }
        authorRepository.deleteAuthor(authorId);
    }

    public List<Author> getAllAuthors() {
        return authorRepository.findAllAuthors();
    }

    public Optional<Author> findAuthorIdByName(String authorName) {
        return authorRepository.findAuthorIdByName(authorName);
    }
}
