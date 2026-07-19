package ru.bookapp;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.bookapp.config.AppConfig;
import ru.bookapp.console.ConsoleApplication;

/**
 * Основной метод - запуск приложения без SpringBoot с помощью ApplicationContext
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Запуск Spring ApplicationContext...");

        // Ручной запуск контекста
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        System.out.println("ApplicationContext успешно запущен!");
        System.out.println("Получение ConsoleApplication...");

        // Получение бина и запуск приложения
        ConsoleApplication consoleApp = context.getBean(ConsoleApplication.class);
        consoleApp.run();

        // Закрытие контекста при завершении
        context.close();

    }
}