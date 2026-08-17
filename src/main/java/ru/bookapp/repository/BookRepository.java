package ru.bookapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.bookapp.model.Book;

import java.util.List;

/**
 * Читает/пишет в таблицу книг
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.author")
    List<Book> findAllWithAuthors();

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :titlePart, '%')) " +
            "AND LOWER(b.author.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    List<Book> findBooksByTitleAndAuthor(@Param("titlePart") String titlePart,
            @Param("authorName") String authorName);

    @Query("SELECT b FROM Book b WHERE b.author.id = :authorId")
    List<Book> findByAuthorId(@Param("authorId") Long authorId);

    @Modifying
    @Query("UPDATE Book b SET b.author.id = :newAuthorId WHERE b.id = :bookId")
    void updateBookAuthor(@Param("bookId") Long bookId,
            @Param("newAuthorId") Long newAuthorId);
}