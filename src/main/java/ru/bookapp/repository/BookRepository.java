package ru.bookapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.bookapp.model.Book;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

/**
 * Читает/пишет в таблицу книг
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
                    year INTEGER,
                    author_id INTEGER REFERENCES authors(id) ON DELETE RESTRICT
                )
                """;
        jdbcTemplate.execute(sql);
    }

    public Long addBook(Book book) {
        String sql = "INSERT INTO books (title, year, author_id) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, book.getTitle());
            ps.setInt(2, book.getYear());
            ps.setLong(3, book.getAuthorId());
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public List<Book> findAllBooks() {
        String sql = """
                SELECT b.id, b.title, b.year, a.name as author_name\s
                FROM books b\s
                LEFT JOIN authors a ON b.author_id = a.id\s
                ORDER BY b.id
               \s""";
        return jdbcTemplate.query(sql, new BookWithAuthorRowMapper());
    }

    public List<Book> findBooksByTitle(String title) {
        String sql = """
                SELECT b.id, b.title, b.year, a.name as author_name\s
                FROM books b\s
                LEFT JOIN authors a ON b.author_id = a.id\s
                WHERE LOWER(b.title) LIKE ?\s
                ORDER BY b.id
               \s""";
        String searchPattern = "%" + title.toLowerCase() + "%";
        return jdbcTemplate.query(sql, new BookWithAuthorRowMapper(), searchPattern);
    }

    public List<Book> findBooksByAuthorId(Long authorId) {
        String sql = """
                SELECT b.id, b.title, b.year, a.name as author_name\s
                FROM books b\s
                LEFT JOIN authors a ON b.author_id = a.id\s
                WHERE b.author_id = ?
                ORDER BY b.id
               \s""";
        return jdbcTemplate.query(sql, new BookWithAuthorRowMapper(), authorId);
    }

    public void updateBookAuthor(Long bookId, Long newAuthorId) {
        String sql = "UPDATE books SET author_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, newAuthorId, bookId);
    }

    public boolean bookExists(Long bookId) {
        String sql = "SELECT COUNT(*) FROM books WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, bookId);
        return count != null && count > 0;
    }

    private static class BookWithAuthorRowMapper implements RowMapper<Book> {
        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Book(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("author_name") != null ? rs.getString("author_name") : "Без автора",
                    rs.getInt("year")
            );
        }
    }
}
