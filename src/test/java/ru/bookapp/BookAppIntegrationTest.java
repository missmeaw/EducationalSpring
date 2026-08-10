package ru.bookapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.bookapp.config.AppConfig;
import ru.bookapp.model.Author;
import ru.bookapp.model.Book;
import ru.bookapp.repository.AuthorRepository;
import ru.bookapp.repository.BookRepository;
import ru.bookapp.service.AuthorService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class, TestConfig.class})
@Testcontainers
@ComponentScan(basePackages = "ru.bookapp")
@Tag("integration")
public class BookAppIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.5")
            .withDatabaseName("bookdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withReuse(false);

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        // Очистка данных перед каждым тестом
        try {
            bookRepository.deleteAll();
            authorRepository.deleteAll();
        } catch (Exception e) {
            // Игнорируем, если таблицы пустые
        }
    }

    @Test
    void testScenario1_SuccessfulSaveAuthorWithTwoBooks() {
        // Arrange
        String authorName = "Лев Толстой";
        String biography = "Русский писатель и мыслитель";
        List<Book> books = Arrays.asList(
                new Book("Война и мир", 1869, null),
                new Book("Анна Каренина", 1877, null)
        );

        // Act
        authorService.createAuthorWithBooks(authorName, biography, books);

        // Assert
        Optional<Author> savedAuthor = authorRepository.findAuthorByName(authorName);
        assertTrue(savedAuthor.isPresent(), "Автор должен быть сохранен");
        assertEquals(authorName, savedAuthor.get().getName());
        assertEquals(biography, savedAuthor.get().getBiography());

        List<Book> savedBooks = bookRepository.findBooksByAuthorId(savedAuthor.get().getId());
        assertEquals(2, savedBooks.size(), "Должно быть 2 книги");

        List<String> titles = savedBooks.stream()
                .map(Book::getTitle)
                .toList();
        assertTrue(titles.contains("Война и мир"));
        assertTrue(titles.contains("Анна Каренина"));
    }

    @Test
    void testScenario2_ErrorInSecondBook_ShouldRollbackAll() {
        // Arrange
        String authorName = "Тестовый Автор";
        String biography = "Тестовая биография";
        List<Book> books = Arrays.asList(
                new Book("Первая книга", 2000, null),
                new Book("ERROR", 2001, null)
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authorService.createAuthorWithBooks(authorName, biography, books);
        });

        assertTrue(exception.getMessage().contains("Тестовая ошибка"));

        Optional<Author> savedAuthor = authorRepository.findAuthorByName(authorName);
        assertFalse(savedAuthor.isPresent(), "Автор не должен быть сохранен");

        List<Book> allBooks = bookRepository.findAllBooks();
        boolean firstBookExists = allBooks.stream()
                .anyMatch(book -> "Первая книга".equals(book.getTitle()));
        assertFalse(firstBookExists, "Первая книга не должна быть сохранена");
    }

    @Test
    void testScenario3_SuccessfulFindAuthorWithBooks() {
        // Arrange
        String authorName = "Федор Достоевский";
        String biography = "Русский писатель";
        List<Book> books = Arrays.asList(
                new Book("Преступление и наказание", 1866, null),
                new Book("Идиот", 1869, null),
                new Book("Братья Карамазовы", 1880, null)
        );

        // Act
        authorService.createAuthorWithBooks(authorName, biography, books);
        Optional<Author> foundAuthor = authorService.findAuthorByName(authorName);

        // Assert
        assertTrue(foundAuthor.isPresent(), "Автор должен быть найден");
        assertEquals(authorName, foundAuthor.get().getName());
        // Act && Assert
        List<Book> assertBooks = bookRepository.findBooksByAuthorId(foundAuthor.get().getId());
        assertEquals(3, assertBooks.size());
    }

    @Test
    void testScenario4_DeleteAuthorWithNoBooks_Success() {
        // Arrange
        String authorName = "Уединенный Автор";
        authorService.createAuthorWithBooks(authorName, "Без книг", List.of());

        // Act
        Optional<Author> savedAuthor = authorRepository.findAuthorByName(authorName);
        assertTrue(savedAuthor.isPresent());

        authorService.deleteAuthor(savedAuthor.get().getId());

        // Assert
        Optional<Author> deletedAuthor = authorRepository.findAuthorByName(authorName);
        assertFalse(deletedAuthor.isPresent(), "Автор должен быть удален");
    }

    @Test
    void testScenario5_DeleteAuthorWithBooks_ShouldFail() {
        // Arrange
        String authorName = "Автор с книгами";
        List<Book> books = List.of(
                new Book("Книга 1", 2000, null)
        );
        authorService.createAuthorWithBooks(authorName, "Тест", books);

        // Act & Assert
        Optional<Author> savedAuthor = authorRepository.findAuthorByName(authorName);
        assertTrue(savedAuthor.isPresent());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authorService.deleteAuthor(savedAuthor.get().getId()));

        assertTrue(exception.getMessage().contains("Нельзя удалить автора, у которого есть книги"));
    }
}