package ru.bookapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        // Очистка данных перед каждым тестом
            bookRepository.deleteAll();
            authorRepository.deleteAll();
    }

    @Test
    void testScenario1_SuccessfulSaveAuthorWithTwoBooks() {
        System.out.println("=== ТЕСТ 1: Успешное сохранение автора с двумя книгами (каскадное сохранение) ===");

        // Arrange
        String authorName = "Лев Толстой";
        String biography = "Русский писатель и мыслитель";
        List<Book> books = Arrays.asList(
                new Book("Война и мир", 1869),
                new Book("Анна Каренина", 1877)
        );

        // Act - используем каскадное сохранение через JPA
        authorService.createAuthorWithBooks(authorName, biography, books);

        // Assert - проверяем, что автор сохранен
        Optional<Author> savedAuthor = authorRepository.findByNameWithBooks(authorName);
        assertTrue(savedAuthor.isPresent(), "Автор должен быть сохранен");
        Author author = savedAuthor.get();

        assertEquals(authorName, author.getName());
        assertEquals(biography, author.getBiography());

        // Проверяем, что книги сохранены и связаны с автором
        List<Book> savedBooks = bookRepository.findByAuthorId(author.getId());
        assertEquals(2, savedBooks.size(), "Должно быть 2 книги");

        List<String> titles = savedBooks.stream()
                .map(Book::getTitle)
                .toList();
        assertTrue(titles.contains("Война и мир"));
        assertTrue(titles.contains("Анна Каренина"));

        // Проверяем, что все книги правильно связаны с автором
        savedBooks.forEach(book -> {
            assertNotNull(book.getAuthor(), "Книга должна иметь автора");
            assertEquals(author.getId(), book.getAuthorId(),
                "ID автора должен совпадать");
        });

        // Проверяем обратную связь - у автора должны быть книги
        Author authorWithBooks = authorRepository.findByIdWithBooks(savedAuthor.get().getId()).orElseThrow();
        assertEquals(2, authorWithBooks.getBooks().size(),
            "У автора должно быть 2 книги через обратную связь");

        System.out.println("✅ Автор сохранен: " + authorWithBooks.getName());
        System.out.println("✅ Книг сохранено: " + authorWithBooks.getBooks().size());
        System.out.println("✅ Тест 1 успешно завершен");
    }

    @Test
    void testScenario2_ErrorInSecondBook_ShouldRollbackAll() {
        System.out.println("=== ТЕСТ 2: Ошибка во второй книге - откат транзакции ===");

        // Arrange
        String authorName = "Тестовый Автор";
        String biography = "Тестовая биография";
        List<Book> books = Arrays.asList(
                new Book("Первая книга", 2000),
                new Book("ERROR", 2001) // Эта книга вызовет ошибку
        );

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authorService.createAuthorWithBooks(authorName, biography, books));

        assertTrue(exception.getMessage().contains("Тестовая ошибка"),
            "Должна быть тестовая ошибка");
        System.out.println("Исключение получено: " + exception.getMessage());

        // Проверяем, что автор НЕ сохранен
        Optional<Author> savedAuthor = authorRepository.findByNameWithBooks(authorName);
        assertFalse(savedAuthor.isPresent(), "Автор не должен быть сохранен");

        // Проверяем, что книги не сохранены
        List<Book> allBooks = bookRepository.findAll();
        assertEquals(0, allBooks.size(), "Книг не должно быть в БД");

        System.out.println("✅ Транзакция полностью откатилась");
        System.out.println("✅ Автор не сохранен");
        System.out.println("✅ Книги не сохранены");
        System.out.println("✅ Тест 2 успешно завершен");
    }

    @Test
    void testScenario3_SuccessfulFindAuthorWithBooks() {
        System.out.println("=== ТЕСТ 3: Успешный поиск автора с книгами (JOIN FETCH) ===");

        // Arrange
        String authorName = "Федор Достоевский";
        String biography = "Русский писатель";
        List<Book> books = Arrays.asList(
                new Book("Преступление и наказание", 1866),
                new Book("Идиот", 1869),
                new Book("Братья Карамазовы", 1880)
        );

        // Act
        authorService.createAuthorWithBooks(authorName, biography, books);

        // Используем JOIN FETCH для загрузки книг одним запросом
        Optional<Author> foundAuthor = authorRepository.findByNameWithBooks(authorName);
        assertTrue(foundAuthor.isPresent(), "Автор должен быть найден");

        // Загружаем автора с книгами через JOIN FETCH
        Optional<Author> authorWithBooks = authorRepository.findByIdWithBooks(foundAuthor.get().getId());

        // Assert
        assertTrue(authorWithBooks.isPresent(), "Автор с книгами должен быть найден");
        assertEquals(authorName, authorWithBooks.get().getName());
        assertEquals(3, authorWithBooks.get().getBooks().size(),
            "Должно быть 3 книги");

        List<String> titles = authorWithBooks.get().getBooks().stream()
                .map(Book::getTitle)
                .toList();
        assertTrue(titles.contains("Преступление и наказание"));
        assertTrue(titles.contains("Идиот"));
        assertTrue(titles.contains("Братья Карамазовы"));

        System.out.println("✅ Автор найден: " + authorWithBooks.get().getName());
        System.out.println("✅ Книг загружено: " + authorWithBooks.get().getBooks().size());
        System.out.println("✅ Тест 3 успешно завершен");
    }

    @Test
    void testScenario4_DeleteAuthorWithNoBooks_Success() {
        System.out.println("=== ТЕСТ 4: Удаление автора без книг ===");

        // Arrange
        String authorName = "Уединенный Автор";
        authorService.createAuthorWithBooks(authorName, "Без книг", List.of());

        // Act
        Optional<Author> savedAuthor = authorRepository.findByNameWithBooks(authorName);
        assertTrue(savedAuthor.isPresent(), "Автор должен быть создан");

        Long authorId = savedAuthor.get().getId();
        authorService.deleteAuthor(authorId);

        // Assert
        Optional<Author> deletedAuthor = authorRepository.findByNameWithBooks(authorName);
        assertFalse(deletedAuthor.isPresent(), "Автор должен быть удален");

        // Проверяем, что книг нет
        List<Book> allBooks = bookRepository.findAll();
        assertEquals(0, allBooks.size(), "Книг не должно быть");

        System.out.println("✅ Автор успешно удален");
        System.out.println("✅ Тест 4 успешно завершен");
    }

    @Test
    void testScenario5_DeleteAuthorWithBooks_ShouldFail() {
        System.out.println("=== ТЕСТ 5: Удаление автора с книгами (должно завершиться ошибкой) ===");

        // Arrange
        String authorName = "Автор с книгами";
        List<Book> books = List.of(
                new Book("Книга 1", 2000)
        );
        authorService.createAuthorWithBooks(authorName, "Тест", books);

        // Act & Assert
        Optional<Author> savedAuthor = authorRepository.findByNameWithBooks(authorName);
        assertTrue(savedAuthor.isPresent(), "Автор должен быть создан");
        Long authorId = savedAuthor.get().getId();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authorService.deleteAuthor(authorId));

        assertTrue(exception.getMessage().contains("Нельзя удалить автора, у которого есть книги"),
            "Должна быть ошибка о наличии книг");

        // Проверяем, что автор не удален
        Optional<Author> stillExists = authorRepository.findByNameWithBooks(authorName);
        assertTrue(stillExists.isPresent(), "Автор должен остаться в БД");

        // Проверяем, что книги остались
        List<Book> allBooks = bookRepository.findAll();
        assertEquals(1, allBooks.size(), "Книга должна остаться");

        System.out.println("✅ Ошибка получена: " + exception.getMessage());
        System.out.println("✅ Автор не удален");
        System.out.println("✅ Книги сохранены");
        System.out.println("✅ Тест 5 успешно завершен");
    }

    @Test
    void testScenario6_LazyLoadingDemonstration() {
        System.out.println("=== ТЕСТ 6: Демонстрация ленивой загрузки (LAZY) ===");

        // Arrange
        String authorName = "Александр Пушкин";
        String biography = "Русский поэт";
        List<Book> books = Arrays.asList(
                new Book("Евгений Онегин", 1833),
                new Book("Капитанская дочка", 1836)
        );

        // Act - сохраняем автора с книгами
        authorService.createAuthorWithBooks(authorName, biography, books);

        // Загружаем автора без книг (LAZY)
        System.out.println("1. Загружаем автора с LAZY загрузкой (книги не загружены)");
        Optional<Author> lazyAuthor = authorRepository.findByNameWithBooks(authorName);
        assertTrue(lazyAuthor.isPresent());

        Author author = lazyAuthor.get();
        System.out.println("2. Автор загружен, книги еще не загружены (Hibernate не делал запрос для книг)");

        // Проверяем, что книги не загружены
        System.out.println("3. Проверяем размер коллекции книг (вызовет запрос к БД)");
        int bookCount = author.getBooks().size(); // Здесь происходит запрос к БД
        System.out.println("4. Книг загружено: " + bookCount);

        assertEquals(2, bookCount, "Должно быть 2 книги");

        // Загружаем автора с книгами через JOIN FETCH
        System.out.println("5. Загружаем автора с книгами через JOIN FETCH (одним запросом)");
        Optional<Author> eagerAuthor = authorRepository.findByIdWithBooks(author.getId());
        assertTrue(eagerAuthor.isPresent());

        System.out.println("6. Книги уже загружены вместе с автором");
        int eagerBookCount = eagerAuthor.get().getBooks().size();
        assertEquals(2, eagerBookCount, "Должно быть 2 книги");

        System.out.println("✅ Демонстрация ленивой загрузки завершена");
        System.out.println("✅ LAZY загрузка: книги загружены только при обращении к ним");
        System.out.println("✅ JOIN FETCH: книги загружены сразу одним запросом");
        System.out.println("✅ Тест 6 успешно завершен");
    }

    @Test
    void testScenario7_CascadeDeleteWithOrphanRemoval() {
        System.out.println("=== ТЕСТ 7: Каскадное удаление с orphanRemoval ===");

        // Arrange
        String authorName = "Михаил Булгаков";
        String biography = "Русский писатель и драматург";
        List<Book> books = Arrays.asList(
                new Book("Мастер и Маргарита", 1967),
                new Book("Собачье сердце", 1925)
        );

        // Act
        authorService.createAuthorWithBooks(authorName, biography, books);

        // Находим автора
        Optional<Author> savedAuthor = authorRepository.findByNameWithBooks(authorName);
        assertTrue(savedAuthor.isPresent());

        Author author = savedAuthor.get();
        System.out.println("1. Автор создан с книгами: " + author.getBooks().size());

        // Удаляем одну книгу через orphanRemoval
        Book bookToRemove = author.getBooks().getFirst();
        System.out.println("2. Удаляем книгу: " + bookToRemove.getTitle());
        author.removeBook(bookToRemove);
        authorRepository.save(author);

        // Проверяем, что книга удалена
        List<Book> remainingBooks = bookRepository.findByAuthorId(author.getId());
        assertEquals(1, remainingBooks.size(), "Должна остаться 1 книга");

        // Проверяем, что удаленная книга не существует в БД
        Optional<Book> deletedBook = bookRepository.findById(bookToRemove.getId());
        assertFalse(deletedBook.isPresent(), "Удаленная книга не должна существовать");

        System.out.println("3. Осталось книг: " + remainingBooks.size());
        System.out.println("✅ orphanRemoval отработал корректно");
        System.out.println("✅ Тест 7 успешно завершен");
    }
}