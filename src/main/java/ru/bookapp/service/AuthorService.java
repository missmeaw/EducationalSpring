package ru.bookapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookapp.model.Author;
import ru.bookapp.model.Book;
import ru.bookapp.repository.AuthorRepository;

import java.util.List;
import java.util.Optional;

/**
 * Сервис добавления/изменения/чтения авторов
 */
@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    @Autowired
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public Long createAuthor(String authorName, String biography) {
        if (authorRepository.authorExistsByName(authorName)) {
            throw new IllegalArgumentException("Автор с именем '" + authorName + "' уже существует");
        }

        Author author = new Author(authorName, biography);
        Author saved = authorRepository.save(author);
        return saved.getId();
    }

    @Transactional
    public void createAuthorWithBooks(String authorName, String biography, List<Book> books) {
        if (authorRepository.authorExistsByName(authorName)) {
            throw new IllegalArgumentException("Автор с именем '" + authorName + "' уже существует");
        }

        Author author = new Author(authorName, biography);

        for (Book book : books) {
            if ("ERROR".equalsIgnoreCase(book.getTitle())) {
                throw new RuntimeException("Тестовая ошибка при добавлении книги '" + book.getTitle() + "'");
            }
            author.addBook(book);
        }

        authorRepository.save(author);
        System.out.println("✅ Автор и книги сохранены. ID автора: " + author.getId());
    }

    public Optional<Author> findAuthorById(Long id) {
        return authorRepository.findByIdWithBooks(id);
    }

    public Optional<Author> findAuthorByIdWithBooks(Long id) {
        return authorRepository.findByIdWithBooks(id);
    }

    public Optional<Author> findAuthorLazy(Long id) {
        return authorRepository.findById(id);
    }

    @Transactional
    public void deleteAuthor(Long authorId) {
        if (authorRepository.authorNotExists(authorId)) {
            throw new IllegalArgumentException("Автор с ID " + authorId + " не найден");
        }

        Optional<Author> authorOpt = authorRepository.findById(authorId);
        if (authorOpt.isPresent()) {
            Author author = authorOpt.get();
            if (!author.getBooks().isEmpty()) {
                throw new IllegalStateException("Нельзя удалить автора, у которого есть книги. Сначала удалите все книги автора.");
            }
            authorRepository.delete(author);
        }
    }

    public List<Author> getAllAuthors() {
        return authorRepository.findAllWithBooks();
    }

    public Optional<Author> findAuthorByName(String authorName) {
        return authorRepository.findByNameWithBooks(authorName);
    }

    public void updateAuthorBiography(Long authorId, String newBiography) {
        Optional<Author> authorOpt = authorRepository.findById(authorId);
        if (authorOpt.isPresent()) {
            Author author = authorOpt.get();
            author.setBiography(newBiography);
        } else {
            throw new IllegalArgumentException("Автор с ID " + authorId + " не найден");
        }
    }
}