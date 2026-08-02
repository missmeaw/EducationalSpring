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
import java.util.List;
import java.util.Map;

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
        String sql = "CREATE TABLE IF NOT EXISTS books (" +
                "id SERIAL PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "year INTEGER, " +
                "author_id INTEGER REFERENCES authors(id) ON DELETE RESTRICT" +
                ")";
        jdbcTemplate.execute(sql);

        // Проверяем, есть ли колонка author (старая)
        if (columnExists("books", "author")) {
            //Удаляем старую колонку author
            String dropColumnSql = "ALTER TABLE books DROP COLUMN author";
            jdbcTemplate.execute(dropColumnSql);
        }

        // Проверяем, есть ли колонка author_id (новая)
        if (!columnExists("books", "author_id")) {
            //Добавляем новую колонку author_id
            String addColumnSql = "ALTER TABLE books ADD COLUMN author_id INTEGER REFERENCES authors(id) ON DELETE RESTRICT";
            jdbcTemplate.execute(addColumnSql);
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        String sql = "SELECT EXISTS (" +
                "SELECT FROM information_schema.columns " +
                "WHERE table_name = ? AND column_name = ?" +
                ")";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, tableName, columnName));
    }

    public Long addBook(Book book) {
        String sql = "INSERT INTO books (title, year, author_id) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"}); // Явно указываем колонку
            ps.setString(1, book.getTitle());
            ps.setInt(2, book.getYear());
            ps.setLong(3, book.getAuthorId());
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys == null || keys.isEmpty()) {
            throw new RuntimeException("Не удалось получить ID книги");
        }
        return ((Number) keys.get("id")).longValue();
    }

    public List<Book> findAllBooks() {
        String sql = "SELECT b.id, b.title, b.year, a.name as author_name " +
                "FROM books b " +
                "LEFT JOIN authors a ON b.author_id = a.id " +
                "ORDER BY b.id";
        return jdbcTemplate.query(sql, new BookWithAuthorRowMapper());
    }

    public List<Book> findBooksByTitle(String title) {
        String sql = "SELECT b.id, b.title, b.year, a.name as author_name " +
                "FROM books b " +
                "LEFT JOIN authors a ON b.author_id = a.id " +
                "WHERE LOWER(b.title) LIKE ? " +
                "ORDER BY b.id";
        String searchPattern = "%" + title.toLowerCase() + "%";
        return jdbcTemplate.query(sql, new BookWithAuthorRowMapper(), searchPattern);
    }

    public List<Book> findBooksByAuthorId(Long authorId) {
        String sql = "SELECT b.id, b.title, b.year, a.name as author_name " +
                 "FROM books b " +
                 "LEFT JOIN authors a ON b.author_id = a.id " +
                 "WHERE b.author_id = ? " +
                 "ORDER BY b.id";
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
