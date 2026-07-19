package ru.bookapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.bookapp.model.Book;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Читает/пишет в базу данных книг
 */
@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Создание таблицы при старте
    public void initTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS books (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    author VARCHAR(255) NOT NULL,
                    year INTEGER
                )
                """;
        jdbcTemplate.execute(sql);
    }

    public void addBook(Book book) {
        String sql = "INSERT INTO books (title, author, year) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, book.getTitle(), book.getAuthor(), book.getYear());
    }

    public List<Book> findAllBooks() {
        String sql = "SELECT * FROM books ORDER BY id";
        return jdbcTemplate.query(sql, new BookRowMapper());
    }

    public List<Book> findBooksByTitle(String title) {
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? ORDER BY id";
        String searchPattern = "%" + title.toLowerCase() + "%";
        return jdbcTemplate.query(sql, new BookRowMapper(), searchPattern);
    }

    private static class BookRowMapper implements RowMapper<Book> {
        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Book(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("year")
            );
        }
    }
}
