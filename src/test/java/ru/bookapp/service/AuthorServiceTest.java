package ru.bookapp.service;

import ru.bookapp.model.Author;
import ru.bookapp.model.Book;
import ru.bookapp.repository.AuthorRepository;
import ru.bookapp.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorService authorService;

    private Author testAuthor;
    private List<Book> testBooks;

    @BeforeEach
    void setUp() {
        testAuthor = new Author("Лев Толстой", "Русский писатель");
        testAuthor.setId(1L);

        testBooks = Arrays.asList(
                new Book("Война и мир", 1869, 1L),
                new Book("Анна Каренина", 1877, 1L)
        );
    }

    @Test
    void createAuthorWithBooks_Success() {
        // Arrange
        when(authorRepository.authorExistsByName("Лев Толстой")).thenReturn(false);
        when(authorRepository.addAuthor(any(Author.class))).thenReturn(1L);
        when(bookRepository.addBook(any(Book.class))).thenReturn(1L, 2L);

        // Act
        authorService.createAuthorWithBooks("Лев Толстой", "Русский писатель", testBooks);

        // Assert
        verify(authorRepository, times(1)).authorExistsByName("Лев Толстой");
        verify(authorRepository, times(1)).addAuthor(any(Author.class));
        verify(bookRepository, times(2)).addBook(any(Book.class));
    }

    @Test
    void createAuthorWithBooks_AuthorAlreadyExists_ThrowsException() {
        // Arrange
        when(authorRepository.authorExistsByName("Лев Толстой")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authorService.createAuthorWithBooks("Лев Толстой", "Русский писатель", testBooks));

        assertEquals("Автор с именем 'Лев Толстой' уже существует", exception.getMessage());
        verify(authorRepository, never()).addAuthor(any(Author.class));
        verify(bookRepository, never()).addBook(any(Book.class));
    }

    @Test
    void createAuthorWithBooks_ErrorBook_ThrowsExceptionAndRollback() {
        // Arrange
        when(authorRepository.authorExistsByName("Тестовый Автор")).thenReturn(false);
        when(authorRepository.addAuthor(any(Author.class))).thenReturn(1L);

        // Создаем книги, где вторая называется "ERROR"
        List<Book> booksWithError = Arrays.asList(
                new Book("Нормальная книга", 2000, 1L),
                new Book("ERROR", 2001, 1L)
        );

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authorService.createAuthorWithBooks("Тестовый Автор", "Тест", booksWithError));

        assertEquals("Тестовая ошибка при добавлении книги 'ERROR'", exception.getMessage());
        verify(authorRepository, times(1)).addAuthor(any(Author.class));
        verify(bookRepository, times(1)).addBook(any(Book.class)); // Только первая книга
    }

    @Test
    void findAuthorById_Success() {
        // Arrange
        when(authorRepository.findAuthorById(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.findBooksByAuthorId(1L)).thenReturn(testBooks);

        // Act
        Optional<Author> result = authorService.findAuthorById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Лев Толстой", result.get().getName());
        assertEquals(2, result.get().getBooks().size());
        verify(authorRepository, times(1)).findAuthorById(1L);
        verify(bookRepository, times(1)).findBooksByAuthorId(1L);
    }

    @Test
    void findAuthorById_NotFound_ReturnsEmpty() {
        // Arrange
        when(authorRepository.findAuthorById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Author> result = authorService.findAuthorById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(authorRepository, times(1)).findAuthorById(999L);
        verify(bookRepository, never()).findBooksByAuthorId(anyLong());
    }

    @Test
    void deleteAuthor_Success() {
        // Arrange
        when(authorRepository.authorNotExists(1L)).thenReturn(false);
        doNothing().when(authorRepository).deleteAuthor(1L);

        // Act
        authorService.deleteAuthor(1L);

        // Assert
        verify(authorRepository, times(1)).authorNotExists(1L);
        verify(authorRepository, times(1)).deleteAuthor(1L);
    }

    @Test
    void deleteAuthor_NotFound_ThrowsException() {
        // Arrange
        when(authorRepository.authorNotExists(999L)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authorService.deleteAuthor(999L));

        assertEquals("Автор с ID 999 не найден", exception.getMessage());
        verify(authorRepository, never()).deleteAuthor(anyLong());
    }

    @Test
    void deleteAuthor_HasBooks_ThrowsException() {
        // Arrange
        when(authorRepository.authorNotExists(1L)).thenReturn(false);
        when(bookRepository.findBooksByAuthorId(1L)).thenReturn(testBooks);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authorService.deleteAuthor(1L));

        assertEquals("Нельзя удалить автора, у которого есть книги. Сначала удалите все книги автора.", exception.getMessage());
        verify(authorRepository, never()).deleteAuthor(anyLong());
    }

    @Test
    void getAllAuthors_Success() {
        // Arrange
        when(authorRepository.findAllAuthors()).thenReturn(List.of(testAuthor));
        when(bookRepository.findBooksByAuthorId(1L)).thenReturn(testBooks);

        // Act
        List<Author> result = authorService.getAllAuthors();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testAuthor, result.getFirst());
        assertEquals(2, result.getFirst().getBooks().size());
        assertEquals(testBooks, result.getFirst().getBooks());
        verify(authorRepository, times(1)).findAllAuthors();
        verify(bookRepository, times(1)).findBooksByAuthorId(1L);
    }

    @Test
    void findAuthorByName_Success() {
        String name = testAuthor.getName();
        // Arrange
        when(authorRepository.findAuthorByName(name)).thenReturn(Optional.of(testAuthor));

        // Act
        Optional<Author> result = authorService.findAuthorByName(name);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Лев Толстой", result.get().getName());
        assertNull(result.get().getBooks());
        verify(authorRepository, times(1)).findAuthorByName(name);
    }
}