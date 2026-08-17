package ru.bookapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.bookapp.model.Author;

import java.util.List;
import java.util.Optional;

/**
 * Читает/пишет в таблицу Авторов
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.name = :name")
    Optional<Author> findByNameWithBooks(@Param("name") String name);

    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.id = :id")
    Optional<Author> findByIdWithBooks(@Param("id") Long id);

    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books")
    List<Author> findAllWithBooks();

    boolean existsByName(String name);

    default boolean authorExistsByName(String name) {
        return existsByName(name);
    }

    default boolean authorNotExists(Long id) {
        return !existsById(id);
    }
}
