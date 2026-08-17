package ru.bookapp.console;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bookapp.model.Author;
import ru.bookapp.model.Book;
import ru.bookapp.service.AuthorService;
import ru.bookapp.service.BookService;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Консоль для работы с пользователем
 */

@Component
public class ConsoleApplication {

    private final BookService bookService;
    private final AuthorService authorService;
    private final Scanner scanner;

    @Autowired
    public ConsoleApplication(BookService bookService, AuthorService authorService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {

        System.out.println("=== Добро пожаловать в систему управления библиотекой ===");
        System.out.println("Доступные команды:");
        System.out.println("  add-book              - Добавить книгу");
        System.out.println("  add-author            - Создать автора");
        System.out.println("  add-author-with-books - Создать автора с книгами");
        System.out.println("  find-author           - Найти автора по ID");
        System.out.println("  find-author-books     - Найти автора с книгами");
        System.out.println("  delete-author         - Удалить автора");
        System.out.println("  list-authors          - Показать всех авторов");
        System.out.println("  list-books            - Показать все книги");
        System.out.println("  find-books            - Найти книги по названию");
        System.out.println("  find-books-jpql       - Найти книги по названию и автору");
        System.out.println("  transfer-book         - Переместить книгу к другому автору");
        System.out.println("  update-author         - Обновить биографию автора");
        System.out.println("  exit                  - Выход из программы");
        System.out.println("=".repeat(50));

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim().toLowerCase();

            try {
                if (input.isEmpty()) {
                    continue;
                }

                switch (input) {
                    case "add-book":
                        handleAddBook();
                        break;
                    case "add-author":
                        handleAddAuthor();
                        break;
                    case "add-author-with-books":
                        handleAddAuthorWithBooks();
                        break;
                    case "find-author":
                        handleFindAuthor();
                        break;
                    case "find-author-books":
                        handleFindAuthorWithBooks();
                        break;
                    case "delete-author":
                        handleDeleteAuthor();
                        break;
                    case "list-books":
                        handleListBooks();
                        break;
                    case "list-authors":
                        handleListAuthors();
                        break;
                    case "find-books":
                        handleFindBooks();
                        break;
                    case "find-books-jpql":
                        handleFindBooksJPQL();
                        break;
                    case "transfer-book":
                        handleTransferBook();
                        break;
                    case "update-author":
                        handleUpdateAuthor();
                        break;
                    case "exit":
                        System.out.println("До свидания!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Неизвестная команда: '" + input + "'");
                        System.out.println(
                                "Доступные команды: add-book, add-author, add-author-with-books," +
                                        " find-author, find-author-books, delete-author, list-books, list-authors," +
                                        " find-books, find-books-jpql, transfer-book, update-author, exit");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void handleAddBook() {
        System.out.print("Введите название книги: ");
        String title = scanner.nextLine().trim();

        if (title.isEmpty()) {
            System.out.println("Название не может быть пустым!");
            return;
        }

        System.out.print("Введите имя автора: ");
        String authorName = scanner.nextLine().trim();

        if (authorName.isEmpty()) {
            System.out.println("Имя автора не может быть пустым!");
            return;
        }
        // Проверяем, существует ли уже автор
        Optional<Author> authorOpt = authorService.findAuthorByName(authorName);
        if (authorOpt.isEmpty()) {
            System.out.println("Автор с именем '" + authorName + "' не существует. Сначала создайте автора.");
            return;
        }

        System.out.print("Введите год издания: ");
        String yearInput = scanner.nextLine().trim();

        try {
            int year = Integer.parseInt(yearInput);
            if (year < 0 || year > Year.now().getValue()) {
                System.out.println("Некорректный год! Допустимый диапазон: 0 - " + Year.now().getValue());
                return;
            }

            // Создаем книгу и устанавливаем связь с автором
            Book book = new Book(title, year);
            book.setAuthor(authorOpt.get());

            bookService.addBook(book);
            System.out.println("✅ Книга успешно добавлена!");
        } catch (NumberFormatException e) {
            System.out.println("Год должен быть числом!");
        }
    }

    private void handleFindBooks() {
        System.out.print("Введите название для поиска: ");
        String searchTerm = scanner.nextLine().trim();

        if (searchTerm.isEmpty()) {
            System.out.println("Поисковый запрос не может быть пустым!");
            return;
        }

        List<Book> books = bookService.findBooks(searchTerm);

        if (books.isEmpty()) {
            System.out.println("Книги по запросу '" + searchTerm + "' не найдены.");
        } else {
            System.out.println("\n=== Результаты поиска по '" + searchTerm + "' ===");
            books.forEach(System.out::println);
            System.out.println("Найдено книг: " + books.size());
        }
    }

    private void handleFindBooksJPQL() {
        System.out.print("Введите часть названия книги: ");
        String titlePart = scanner.nextLine().trim();
        if (titlePart.isEmpty()) {
            System.out.println("Название не может быть пустым!");
            return;
        }

        System.out.print("Введите имя автора: ");
        String authorName = scanner.nextLine().trim();
        if (authorName.isEmpty()) {
            System.out.println("Имя автора не может быть пустым!");
            return;
        }

        System.out.println("Поиск...");
        List<Book> books = bookService.findBooksByTitleAndAuthor(titlePart, authorName);

        if (books.isEmpty()) {
            System.out.println("Книги по запросу (название: '" + titlePart + "', автор: '" + authorName + "') не найдены.");
        } else {
            System.out.println("\n=== Результаты поиска ===");
            books.forEach(System.out::println);
            System.out.println("Найдено книг: " + books.size());
        }
    }

    private void handleAddAuthor() {
        try {
            System.out.print("Введите имя автора: ");
            String authorName = scanner.nextLine().trim();
            if (authorName.isEmpty()) {
                System.out.println("Имя автора не может быть пустым!");
                return;
            }

            System.out.print("Введите биографию автора (опционально): ");
            String biography = scanner.nextLine().trim();
            if (biography.isEmpty()) {
                biography = null;
            }

            System.out.println("Создание автора и книг...");
            Long authorId = authorService.createAuthor(authorName, biography);
            System.out.println("✅ Автор успешно создан с ID: " + authorId);
        } catch (Exception e) {
            System.out.println("Ошибка при создании: " + e.getMessage());
        }
    }

    private void handleAddAuthorWithBooks() {
        try {
            System.out.print("Введите имя автора: ");
            String authorName = scanner.nextLine().trim();
            if (authorName.isEmpty()) {
                System.out.println("Имя автора не может быть пустым!");
                return;
            }

            System.out.print("Введите биографию автора (опционально): ");
            String biography = scanner.nextLine().trim();
            if (biography.isEmpty()) {
                biography = null;
            }

            System.out.print("Сколько книг добавить? ");
            String countInput = scanner.nextLine().trim();
            int bookCount;
            try {
                bookCount = Integer.parseInt(countInput);
                if (bookCount <= 0) {
                    System.out.println("Количество книг должно быть положительным!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Введите корректное число!");
                return;
            }

            List<Book> books = new ArrayList<>();
            for (int i = 0; i < bookCount; i++) {
                System.out.println("\nКнига " + (i + 1) + ":");
                System.out.print("  Название: ");
                String title = scanner.nextLine().trim();
                if (title.isEmpty()) {
                    System.out.println("Название книги не может быть пустым!");
                    return;
                }

                System.out.print("  Год издания: ");
                String yearInput = scanner.nextLine().trim();
                int year;
                try {
                    year = Integer.parseInt(yearInput);
                    if (year < 0 || year > Year.now().getValue()) {
                        System.out.println("Некорректный год! Допустимый диапазон: 0 - " + Year.now().getValue());
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Год должен быть числом!");
                    return;
                }

                books.add(new Book(title, year));
            }

            System.out.println("\nСоздание автора и книг...");
            authorService.createAuthorWithBooks(authorName, biography, books);
            System.out.println("✅ Автор и книги успешно созданы!");
        } catch (Exception e) {
            System.out.println("Ошибка при создании: " + e.getMessage());
        }
    }

    private void handleFindAuthor() {
        System.out.print("Введите ID автора: ");
        String idInput = scanner.nextLine().trim();

        try {
            Long authorId = Long.parseLong(idInput);
            System.out.println("Поиск автора...");
            Optional<Author> authorOpt = authorService.findAuthorById(authorId);

            if (authorOpt.isPresent()) {
                Author author = authorOpt.get();
                System.out.println("\n=== Найден автор ===");
                System.out.println(author);
            } else {
                System.out.println("Автор с ID " + authorId + " не найден.");
            }
        } catch (NumberFormatException e) {
            System.out.println("ID должен быть числом!");
        }
    }

    private void handleFindAuthorWithBooks() {
        System.out.print("Введите ID автора: ");
        String idInput = scanner.nextLine().trim();

        try {
            Long authorId = Long.parseLong(idInput);
            System.out.println("Поиск автора с книгами...");
            Optional<Author> authorOpt = authorService.findAuthorByIdWithBooks(authorId);

            if (authorOpt.isPresent()) {
                Author author = authorOpt.get();
                System.out.println("\n=== Найден автор с книгами ===");
                System.out.println(author);
            } else {
                System.out.println("Автор с ID " + authorId + " не найден.");
            }
        } catch (NumberFormatException e) {
            System.out.println("ID должен быть числом!");
        }
    }

    private void handleDeleteAuthor() {
        System.out.print("Введите ID автора для удаления: ");
        String idInput = scanner.nextLine().trim();

        try {
            Long authorId = Long.parseLong(idInput);
            authorService.deleteAuthor(authorId);
            System.out.println("✅ Автор успешно удален!");
        } catch (NumberFormatException e) {
            System.out.println("ID должен быть числом!");
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void handleTransferBook() {
        try {
            System.out.print("Введите ID книги: ");
            String bookIdInput = scanner.nextLine().trim();
            Long bookId = Long.parseLong(bookIdInput);

            System.out.print("Введите ID нового автора: ");
            String authorIdInput = scanner.nextLine().trim();
            Long authorId = Long.parseLong(authorIdInput);

            bookService.transferBook(bookId, authorId);
            System.out.println("✅ Книга успешно перенесена к новому автору!");
        } catch (NumberFormatException e) {
            System.out.println("ID должен быть числом!");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void handleUpdateAuthor() {
        try {
            System.out.print("Введите ID автора для обновления: ");
            String idInput = scanner.nextLine().trim();
            Long authorId = Long.parseLong(idInput);

            System.out.print("Введите новую биографию: ");
            String newBiography = scanner.nextLine().trim();
            if (newBiography.isEmpty()) {
                System.out.println("Биография не может быть пустой!");
                return;
            }

            System.out.println("Обновление...");
            authorService.updateAuthorBiography(authorId, newBiography);
            System.out.println("✅ Биография обновлена!");

            // Показываем обновленного автора
            Optional<Author> authorOpt = authorService.findAuthorById(authorId);
            if (authorOpt.isPresent()) {
                System.out.println("Обновленный автор: " + authorOpt.get().getName());
                System.out.println("Новая биография: " + authorOpt.get().getBiography());
            }
        } catch (NumberFormatException e) {
            System.out.println("ID должен быть числом!");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void handleListBooks() {
        List<Book> books = bookService.listBooks();
        if (books.isEmpty()) {
            System.out.println("Библиотека пуста.");
        } else {
            System.out.println("\n=== Список книг ===");
            books.forEach(System.out::println);
            System.out.println("Всего книг: " + books.size());
        }
    }

    private void handleListAuthors() {
        List<Author> authors = authorService.getAllAuthors();
        if (authors.isEmpty()) {
            System.out.println("Писателей не найдено.");
        } else {
            System.out.println("\n=== Список авторов ===");
            authors.forEach(System.out::println);
            System.out.println("Всего авторов: " + authors.size());
        }
    }
}