package ru.bookapp;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.bookapp.config.AppConfig;
import ru.bookapp.console.ConsoleApplication;
import ru.bookapp.repository.AuthorRepository;
import ru.bookapp.repository.BookRepository;

/**
 * Основной метод - запуск приложения без SpringBoot с помощью ApplicationContext
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Запуск Spring ApplicationContext...");

        // Ручной запуск контекста
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        System.out.println("ApplicationContext успешно запущен!");

        try {
            // Инициализация таблиц
            AuthorRepository authorRepo = context.getBean(AuthorRepository.class);
            BookRepository bookRepo = context.getBean(BookRepository.class);

            authorRepo.initTable();
            bookRepo.initTable();

            System.out.println("База данных инициализирована!");
        } catch (Exception e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
            e.printStackTrace();
        }

        // Получение бина и запуск приложения
        System.out.println("Получение ConsoleApplication...");
        ConsoleApplication consoleApp = context.getBean(ConsoleApplication.class);
        consoleApp.run();

        // Закрытие контекста при завершении
        context.close();
    }
}