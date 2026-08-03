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
        System.out.println("  delete-author         - Удалить автора");
        System.out.println("  list-authors          - Показать всех авторов");
        System.out.println("  list-books            - Показать все книги");
        System.out.println("  find-books            - Найти книги по названию");
        System.out.println("  transfer-book         - Переместить книгу к другому автору");
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
                    case "delete-author":
                        handleDeleteAuthor();
                        break;
                    case "list-books": {
                        List<Book> books = bookService.listBooks();
                        if (books.isEmpty()) {
                            System.out.println("Библиотека пуста.");
                        } else {
                            System.out.println("\n=== Список книг ===");
                            books.forEach(System.out::println);
                            System.out.println("Всего книг: " + books.size());
                        }
                    }
                    break;
                    case "list-authors": {
                        List<Author> authors = authorService.getAllAuthors();
                        if (authors.isEmpty()) {
                            System.out.println("Писателей не найдено.");
                        } else {
                            System.out.println("\n=== Список авторов ===");
                            authors.forEach(System.out::println);
                            System.out.println("Всего авторов: " + authors.size());
                        }
                    }
                    break;
                    case "find-books":
                        handleFindBooks();
                        break;
                    case "transfer-book":
                        handleTransferBook();
                        break;
                    case "exit":
                        System.out.println("До свидания!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Неизвестная команда: '" + input + "'");
                        System.out.println(
                                "Доступные команды: add-book, add-author, add-author-with-books, find-author, delete-author, list-books, list-authors, find-books, transfer-book, exit");
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

        System.out.print("Введите автора: ");
        String author = scanner.nextLine().trim();

        if (author.isEmpty()) {
            System.out.println("Автор не может быть пустым!");
            return;
        }
        // Проверяем, существует ли уже автор
        Optional<Author> author1 = authorService.findAuthorIdByName(author);
        if (author1.isEmpty()) {
            throw new IllegalArgumentException("Автор с именем '" + author + "' не существует");
        }

        System.out.print("Введите год издания: ");
        String yearInput = scanner.nextLine().trim();

        try {
            int year = Integer.parseInt(yearInput);
            if (year < 0 || year > Year.now().getValue()) {
                System.out.println("Некорректный год!");
                return;
            }
            bookService.addBook(new Book(title, year, author1.get().getId()));
            System.out.println("Книга успешно добавлена!");
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
            System.out.println("\nСоздание автора и книг...");
            authorService.createAuthor(authorName, biography);
            System.out.println("Автор успешно создан!");
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
                    if (year < 0 || year > 2026) {
                        System.out.println("Некорректный год!");
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Год должен быть числом!");
                    return;
                }

                books.add(new Book(title, year, null)); // authorId будет установлен позже
            }

            System.out.println("\nСоздание автора и книг...");
            authorService.createAuthorWithBooks(authorName, biography, books);
            System.out.println("Автор и книги успешно созданы!");
        } catch (Exception e) {
            System.out.println("Ошибка при создании: " + e.getMessage());
        }
    }

    private void handleFindAuthor() {
        System.out.print("Введите ID автора: ");
        String idInput = scanner.nextLine().trim();

        try {
            Long authorId = Long.parseLong(idInput);
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

    private void handleDeleteAuthor() {
        System.out.print("Введите ID автора для удаления: ");
        String idInput = scanner.nextLine().trim();

        try {
            Long authorId = Long.parseLong(idInput);
            authorService.deleteAuthor(authorId);
            System.out.println("Автор успешно удален!");
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
            System.out.println("Книга успешно перенесена к новому автору!");
        } catch (NumberFormatException e) {
            System.out.println("ID должен быть числом!");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
