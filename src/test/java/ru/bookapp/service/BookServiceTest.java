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
import ru.bookapp.repository.BookRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook1;
    private List<Book> testBooks;

    @BeforeEach
    void setUp() {
        Author testAuthor = new Author("Лев Толстой", "Русский писатель");
        testAuthor.setId(1L);

        testBook1 = new Book("Война и мир", 1869);
        testBook1.setId(1L);
        testBook1.setAuthor(testAuthor);

        Book testBook2 = new Book("Анна Каренина", 1877);
        testBook2.setId(2L);
        testBook2.setAuthor(testAuthor);

        testBooks = Arrays.asList(testBook1, testBook2);
    }

    @Test
    void addBook_Success() {
        // Arrange
        Long authorId = 1L;
        when(authorRepository.authorNotExists(authorId)).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook1);

        // Act
        bookService.addBook(testBook1);

        // Assert
        verify(authorRepository, times(1)).authorNotExists(authorId);
        verify(bookRepository, times(1)).save(testBook1);
    }

    @Test
    void addBook_AuthorNotFound_ThrowsException() {
        // Arrange
        Long authorId = 999L;
        testBook1.setAuthorId(authorId);
        when(authorRepository.authorNotExists(authorId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookService.addBook(testBook1));

        assertEquals("Автор с ID 999 не найден", exception.getMessage());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void addBook_WithoutAuthor_Success() {
        // Arrange
        Book bookWithoutAuthor = new Book("Книга без автора", 2000);
        bookWithoutAuthor.setId(3L);
        when(bookRepository.save(any(Book.class))).thenReturn(bookWithoutAuthor);

        // Act
        bookService.addBook(bookWithoutAuthor);

        // Assert
        verify(authorRepository, never()).authorNotExists(any());
        verify(bookRepository, times(1)).save(bookWithoutAuthor);
    }

    @Test
    void listBooks_Success() {
        // Arrange
        when(bookRepository.findAllWithAuthors()).thenReturn(testBooks);

        // Act
        List<Book> result = bookService.listBooks();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Война и мир", result.getFirst().getTitle());
        verify(bookRepository, times(1)).findAllWithAuthors();
    }

    @Test
    void listBooks_Empty_ReturnsEmptyList() {
        // Arrange
        when(bookRepository.findAllWithAuthors()).thenReturn(List.of());

        // Act
        List<Book> result = bookService.listBooks();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository, times(1)).findAllWithAuthors();
    }

    @Test
    void findBooks_Success() {
        // Arrange
        String searchTerm = "мир";
        when(bookRepository.findByTitleContainingIgnoreCase(searchTerm))
                .thenReturn(Collections.singletonList(testBook1));

        // Act
        List<Book> result = bookService.findBooks(searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Война и мир", result.getFirst().getTitle());
        verify(bookRepository, times(1)).findByTitleContainingIgnoreCase(searchTerm);
    }

    @Test
    void findBooks_NotFound_ReturnsEmptyList() {
        // Arrange
        String searchTerm = "несуществующая книга";
        when(bookRepository.findByTitleContainingIgnoreCase(searchTerm))
                .thenReturn(List.of());

        // Act
        List<Book> result = bookService.findBooks(searchTerm);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository, times(1)).findByTitleContainingIgnoreCase(searchTerm);
    }

    @Test
    void transferBook_Success() {
        // Arrange
        Long bookId = 1L;
        Long newAuthorId = 2L;
        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(authorRepository.authorNotExists(newAuthorId)).thenReturn(false);
        doNothing().when(bookRepository).updateBookAuthor(bookId, newAuthorId);

        // Act
        bookService.transferBook(bookId, newAuthorId);

        // Assert
        verify(bookRepository, times(1)).existsById(bookId);
        verify(authorRepository, times(1)).authorNotExists(newAuthorId);
        verify(bookRepository, times(1)).updateBookAuthor(bookId, newAuthorId);
    }

    @Test
    void transferBook_BookNotFound_ThrowsException() {
        // Arrange
        Long bookId = 999L;
        Long newAuthorId = 2L;
        when(bookRepository.existsById(bookId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookService.transferBook(bookId, newAuthorId));

        assertEquals("Книга с ID 999 не найдена", exception.getMessage());
        verify(authorRepository, never()).authorNotExists(any());
        verify(bookRepository, never()).updateBookAuthor(any(), any());
    }

    @Test
    void transferBook_AuthorNotFound_ThrowsException() {
        // Arrange
        Long bookId = 1L;
        Long newAuthorId = 999L;
        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(authorRepository.authorNotExists(newAuthorId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookService.transferBook(bookId, newAuthorId));

        assertEquals("Автор с ID 999 не найден", exception.getMessage());
        verify(bookRepository, times(1)).existsById(bookId);
        verify(authorRepository, times(1)).authorNotExists(newAuthorId);
        verify(bookRepository, never()).updateBookAuthor(any(), any());
    }

    @Test
    void transferBook_Transactional_ShouldBeCalled() {
        // Arrange
        Long bookId = 1L;
        Long newAuthorId = 2L;
        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(authorRepository.authorNotExists(newAuthorId)).thenReturn(false);

        // Act
        bookService.transferBook(bookId, newAuthorId);

        // Assert - проверяем, что метод вызван с правильными параметрами
        verify(bookRepository, times(1)).updateBookAuthor(bookId, newAuthorId);
    }

    @Test
    void findBooksByTitleAndAuthor_Success() {
        // Arrange
        String titlePart = "война";
        String authorName = "Толстой";
        when(bookRepository.findBooksByTitleAndAuthor(titlePart, authorName))
                .thenReturn(Collections.singletonList(testBook1));

        // Act
        List<Book> result = bookService.findBooksByTitleAndAuthor(titlePart, authorName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Война и мир", result.getFirst().getTitle());
        verify(bookRepository, times(1)).findBooksByTitleAndAuthor(titlePart, authorName);
    }

    @Test
    void findBooksByTitleAndAuthor_NotFound_ReturnsEmptyList() {
        // Arrange
        String titlePart = "несуществующая";
        String authorName = "Неизвестный";
        when(bookRepository.findBooksByTitleAndAuthor(titlePart, authorName))
                .thenReturn(List.of());

        // Act
        List<Book> result = bookService.findBooksByTitleAndAuthor(titlePart, authorName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository, times(1)).findBooksByTitleAndAuthor(titlePart, authorName);
    }

    @Test
    void findBooksByTitleAndAuthor_WithEmptyTitle_ReturnsEmptyList() {
        // Arrange
        String titlePart = "";
        String authorName = "Толстой";
        when(bookRepository.findBooksByTitleAndAuthor(titlePart, authorName))
                .thenReturn(List.of());

        // Act
        List<Book> result = bookService.findBooksByTitleAndAuthor(titlePart, authorName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository, times(1)).findBooksByTitleAndAuthor(titlePart, authorName);
    }

    @Test
    void findBooksByTitleAndAuthor_WithEmptyAuthor_ReturnsEmptyList() {
        // Arrange
        String titlePart = "война";
        String authorName = "";
        when(bookRepository.findBooksByTitleAndAuthor(titlePart, authorName))
                .thenReturn(List.of());

        // Act
        List<Book> result = bookService.findBooksByTitleAndAuthor(titlePart, authorName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository, times(1)).findBooksByTitleAndAuthor(titlePart, authorName);
    }
}