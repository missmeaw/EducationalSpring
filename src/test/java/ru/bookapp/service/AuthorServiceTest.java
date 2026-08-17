package ru.bookapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.bookapp.model.Author;
import ru.bookapp.model.Book;
import ru.bookapp.repository.AuthorRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    private Author testAuthor;
    private List<Book> testBooks;

    @BeforeEach
    void setUp() {
        testAuthor = new Author("Лев Толстой", "Русский писатель");
        testAuthor.setId(1L);

        testBooks = Arrays.asList(
                new Book("Война и мир", 1869),
                new Book("Анна Каренина", 1877)
        );
    }

    @Test
    void createAuthor_Success() {
        // Arrange
        String authorName = "Лев Толстой";
        String biography = "Русский писатель";
        when(authorRepository.authorExistsByName(authorName)).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);

        // Act
        Long authorId = authorService.createAuthor(authorName, biography);

        // Assert
        assertNotNull(authorId);
        assertEquals(1L, authorId);
        verify(authorRepository, times(1)).authorExistsByName(authorName);
        verify(authorRepository, times(1)).save(any(Author.class));
    }

    @Test
    void createAuthor_AlreadyExists_ThrowsException() {
        // Arrange
        String authorName = "Лев Толстой";
        when(authorRepository.authorExistsByName(authorName)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authorService.createAuthor(authorName, "Биография"));

        assertEquals("Автор с именем 'Лев Толстой' уже существует", exception.getMessage());
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void createAuthorWithBooks_Success() {
        // Arrange
        String authorName = "Лев Толстой";
        String biography = "Русский писатель";
        when(authorRepository.authorExistsByName(authorName)).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);

        // Act
        authorService.createAuthorWithBooks(authorName, biography, testBooks);

        // Assert
        verify(authorRepository, times(1)).authorExistsByName(authorName);
        verify(authorRepository, times(1)).save(any(Author.class));
        // Проверяем, что у автора установлены книги
        verify(authorRepository).save(argThat(author -> author.getBooks().size() == 2));
    }

    @Test
    void createAuthorWithBooks_AuthorExists_ThrowsException() {
        // Arrange
        String authorName = "Лев Толстой";
        when(authorRepository.authorExistsByName(authorName)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authorService.createAuthorWithBooks(authorName, "Биография", testBooks));

        assertEquals("Автор с именем 'Лев Толстой' уже существует", exception.getMessage());
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void createAuthorWithBooks_ErrorBook_ThrowsExceptionAndRollback() {
        // Arrange
        String authorName = "Тестовый Автор";
        String biography = "Тестовая биография";
        List<Book> booksWithError = Arrays.asList(
                new Book("Нормальная книга", 2000),
                new Book("ERROR", 2001)
        );
        when(authorRepository.authorExistsByName(authorName)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authorService.createAuthorWithBooks(authorName, biography, booksWithError));

        assertTrue(exception.getMessage().contains("Тестовая ошибка"));
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void findAuthorById_Success() {
        // Arrange
        Long authorId = 1L;
        when(authorRepository.findByIdWithBooks(authorId)).thenReturn(Optional.of(testAuthor));

        // Act
        Optional<Author> result = authorService.findAuthorById(authorId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Лев Толстой", result.get().getName());
        verify(authorRepository, times(1)).findByIdWithBooks(authorId);
    }

    @Test
    void findAuthorById_NotFound_ReturnsEmpty() {
        // Arrange
        Long authorId = 999L;
        when(authorRepository.findByIdWithBooks(authorId)).thenReturn(Optional.empty());

        // Act
        Optional<Author> result = authorService.findAuthorById(authorId);

        // Assert
        assertFalse(result.isPresent());
        verify(authorRepository, times(1)).findByIdWithBooks(authorId);
    }

    @Test
    void findAuthorByIdWithBooks_Success() {
        // Arrange
        Long authorId = 1L;
        when(authorRepository.findByIdWithBooks(authorId)).thenReturn(Optional.of(testAuthor));

        // Act
        Optional<Author> result = authorService.findAuthorByIdWithBooks(authorId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Лев Толстой", result.get().getName());
        verify(authorRepository, times(1)).findByIdWithBooks(authorId);
    }

    @Test
    void findAuthorLazy_Success() {
        // Arrange
        Long authorId = 1L;
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(testAuthor));

        // Act
        Optional<Author> result = authorService.findAuthorLazy(authorId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Лев Толстой", result.get().getName());
        verify(authorRepository, times(1)).findById(authorId);
    }

    @Test
    void deleteAuthor_Success() {
        // Arrange
        Long authorId = 1L;
        when(authorRepository.authorNotExists(authorId)).thenReturn(false);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(testAuthor));

        // Act
        authorService.deleteAuthor(authorId);

        // Assert
        verify(authorRepository, times(1)).authorNotExists(authorId);
        verify(authorRepository, times(1)).findById(authorId);
        verify(authorRepository, times(1)).delete(testAuthor);
    }

    @Test
    void deleteAuthor_NotFound_ThrowsException() {
        // Arrange
        Long authorId = 999L;
        when(authorRepository.authorNotExists(authorId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authorService.deleteAuthor(authorId));

        assertEquals("Автор с ID 999 не найден", exception.getMessage());
        verify(authorRepository, never()).delete(any(Author.class));
    }

    @Test
    void deleteAuthor_HasBooks_ThrowsException() {
        // Arrange
        Long authorId = 1L;
        Author authorWithBooks = new Author("Тестовый автор", "Биография");
        authorWithBooks.setId(1L);
        authorWithBooks.setBooks(List.of(new Book("Книга 1", 2000)));

        when(authorRepository.authorNotExists(authorId)).thenReturn(false);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(authorWithBooks));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authorService.deleteAuthor(authorId));

        assertTrue(exception.getMessage().contains("Нельзя удалить автора, у которого есть книги"));
        verify(authorRepository, never()).delete(any(Author.class));
    }

    @Test
    void getAllAuthors_Success() {
        // Arrange
        List<Author> authors = Arrays.asList(testAuthor, new Author("Федор Достоевский", "Русский писатель"));
        when(authorRepository.findAllWithBooks()).thenReturn(authors);

        // Act
        List<Author> result = authorService.getAllAuthors();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(authorRepository, times(1)).findAllWithBooks();
    }

    @Test
    void findAuthorByName_Success() {
        // Arrange
        String authorName = "Лев Толстой";
        when(authorRepository.findByNameWithBooks(authorName)).thenReturn(Optional.of(testAuthor));

        // Act
        Optional<Author> result = authorService.findAuthorByName(authorName);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(authorName, result.get().getName());
        verify(authorRepository, times(1)).findByNameWithBooks(authorName);
    }

    @Test
    void findAuthorByName_NotFound_ReturnsEmpty() {
        // Arrange
        String authorName = "Неизвестный автор";
        when(authorRepository.findByNameWithBooks(authorName)).thenReturn(Optional.empty());

        // Act
        Optional<Author> result = authorService.findAuthorByName(authorName);

        // Assert
        assertFalse(result.isPresent());
        verify(authorRepository, times(1)).findByNameWithBooks(authorName);
    }

    @Test
    void updateAuthorBiography_Success() {
        // Arrange
        Long authorId = 1L;
        String newBiography = "Новая биография";
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(testAuthor));

        // Act
        authorService.updateAuthorBiography(authorId, newBiography);

        // Assert
        assertEquals(newBiography, testAuthor.getBiography());
        verify(authorRepository, times(1)).findById(authorId);
        // save не вызывается - dirty checking
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void updateAuthorBiography_AuthorNotFound_ThrowsException() {
        // Arrange
        Long authorId = 999L;
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authorService.updateAuthorBiography(authorId, "Новая биография"));

        assertEquals("Автор с ID 999 не найден", exception.getMessage());
        verify(authorRepository, never()).save(any(Author.class));
    }
}