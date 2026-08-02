package ru.bookapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.bookapp.model.Author;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Читает/пишет в таблицу Авторов
 */
@Repository
public class AuthorRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AuthorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Создание таблицы при старте
    public void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS authors (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL UNIQUE, " +
                "biography TEXT" +
                ")";
        jdbcTemplate.execute(sql);
    }

    public Long addAuthor(Author author) {
        String sql = "INSERT INTO authors (name, biography) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"}); // Явно указываем колонку
            ps.setString(1, author.getName());
            ps.setString(2, author.getBiography());
            return ps;
        }, keyHolder);

        // Получаем ключ
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys == null || keys.isEmpty()) {
            throw new RuntimeException("Не удалось получить ID автора");
        }
        return ((Number) keys.get("id")).longValue();
    }

    public Optional<Author> findAuthorById(Long id) {
        String sql = "SELECT id, name, biography FROM authors WHERE id = ?";
        List<Author> authors = jdbcTemplate.query(sql, new AuthorRowMapper(), id);

        if (authors.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(authors.getFirst());
    }

    public List<Author> findAllAuthors() {
        String sql = "SELECT id, name, biography FROM authors ORDER BY name";
        return jdbcTemplate.query(sql, new AuthorRowMapper());
    }

    public void deleteAuthor(Long authorId) {
        String sql = "DELETE FROM authors WHERE id = ?";
        jdbcTemplate.update(sql, authorId);
    }

    public boolean authorNotExists(Long authorId) {
        String sql = "SELECT COUNT(*) FROM authors WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, authorId);
        return count == null || count == 0;
    }

    public boolean authorExistsByName(String name) {
        String sql = "SELECT COUNT(*) FROM authors WHERE LOWER(name) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name);
        return count != null && count > 0;
    }

    public Optional<Author> findAuthorIdByName(String name) {
        String sql = "SELECT id, name, biography FROM authors WHERE name = ?";
        List<Author> authors = jdbcTemplate.query(sql, new AuthorRowMapper(), name);
        if (authors.isEmpty()) {
            return Optional.empty();
        }
        Author author = authors.getFirst();
        return Optional.of(author);
    }

    private static class AuthorRowMapper implements RowMapper<Author> {
        @Override
        public Author mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Author(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("biography")
            );
        }
    }
}
