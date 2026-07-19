package ru.bookapp.console;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bookapp.service.BookService;

import java.util.Scanner;

/**
 * Консоль для работы с пользователем
 */

@Component
public class ConsoleApplication {

    private final BookService bookService;
    private final Scanner scanner;

    @Autowired
    public ConsoleApplication(BookService bookService) {
        this.bookService = bookService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        // Инициализация базы данных
        bookService.initializeDatabase();

        System.out.println("=== Добро пожаловать в систему управления библиотекой ===");
        System.out.println("Доступные команды:");
        System.out.println("  add    - Добавить книгу");
        System.out.println("  list   - Показать все книги");
        System.out.println("  find   - Найти книгу по названию");
        System.out.println("  exit   - Выход из программы");
        System.out.println("=".repeat(50));

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim().toLowerCase();

            try {
                if (input.isEmpty()) {
                    continue;
                }

                switch (input) {
                    case "add":
                        handleAddCommand();
                        break;
                    case "list":
                        bookService.listBooks();
                        break;
                    case "find":
                        handleFindCommand();
                        break;
                    case "exit":
                        System.out.println("До свидания!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Неизвестная команда: '" + input + "'");
                        System.out.println("Доступные команды: add, list, find, exit");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void handleAddCommand() {
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

        System.out.print("Введите год издания: ");
        String yearInput = scanner.nextLine().trim();

        try {
            int year = Integer.parseInt(yearInput);
            if (year < 0 || year > 2026) {
                System.out.println("Некорректный год!");
                return;
            }
            bookService.addBook(title, author, year);
        } catch (NumberFormatException e) {
            System.out.println("Год должен быть числом!");
        }
    }

    private void handleFindCommand() {
        System.out.print("Введите название для поиска: ");
        String searchTerm = scanner.nextLine().trim();

        if (searchTerm.isEmpty()) {
            System.out.println("Поисковый запрос не может быть пустым!");
            return;
        }

        bookService.findBooks(searchTerm);
    }
}
