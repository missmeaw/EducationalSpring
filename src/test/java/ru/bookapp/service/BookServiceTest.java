package ru.bookapp.service;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    private List<Book> testBooks;

    @BeforeEach
    void setUp() {
        testBooks = Arrays.asList(
                new Book(1L, "Война и мир", "Лев Толстой", 1869),
                new Book(2L, "Анна Каренина", "Лев Толстой", 1877)
        );
    }

    @Test
    void addBook_Success() {
        Book book = new Book("Философия о тестах", "Неизвестный философ", 2026);
        // Arrange
        when(bookRepository.addBook(book)).thenReturn(3L);

        // Act
        bookService.addBook(book);

        // Assert
        verify(bookRepository, times(1)).addBook(book);
    }

    @Test
    void listBooks_Success() {
        // Arrange
        when(bookRepository.findAllBooks()).thenReturn(testBooks);

        // Act
        bookService.listBooks();

        // Assert
        verify(bookRepository, times(1)).findAllBooks();
    }

    @Test
    void listBooks_Empty_HandlesGracefully() {
        // Arrange
        when(bookRepository.findAllBooks()).thenReturn(List.of());

        // Act
        bookService.listBooks();

        // Assert
        verify(bookRepository, times(1)).findAllBooks();
    }

    @Test
    void findBooks_Success() {
        // Arrange
        String searchTerm = "война";
        when(bookRepository.findBooksByTitle(searchTerm)).thenReturn(
                List.of(new Book(1L, "Война и мир", "Лев Толстой", 1869))
        );

        // Act
        bookService.findBooks(searchTerm);

        // Assert
        verify(bookRepository, times(1)).findBooksByTitle(searchTerm);
    }

    @Test
    void findBooks_NotFound_HandlesGracefully() {
        // Arrange
        String searchTerm = "несуществующая книга";
        when(bookRepository.findBooksByTitle(searchTerm)).thenReturn(List.of());

        // Act
        bookService.findBooks(searchTerm);

        // Assert
        verify(bookRepository, times(1)).findBooksByTitle(searchTerm);
    }

    @Test
    void transferBook_Success() {
        // Arrange
        Long bookId = 1L;
        Long newAuthorId = 2L;
        when(bookRepository.bookExists(bookId)).thenReturn(true);
        when(authorRepository.authorNotExists(newAuthorId)).thenReturn(false);
        doNothing().when(bookRepository).updateBookAuthor(bookId, newAuthorId);

        // Act
        bookService.transferBook(bookId, newAuthorId);

        // Assert
        verify(bookRepository, times(1)).bookExists(bookId);
        verify(authorRepository, times(1)).authorNotExists(newAuthorId);
        verify(bookRepository, times(1)).updateBookAuthor(bookId, newAuthorId);
    }

    @Test
    void transferBook_BookNotFound_ThrowsException() {
        // Arrange
        Long bookId = 999L;
        Long newAuthorId = 2L;
        when(bookRepository.bookExists(bookId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookService.transferBook(bookId, newAuthorId));
        
        assertEquals("Книга с ID 999 не найдена", exception.getMessage());
        verify(authorRepository, never()).authorNotExists(anyLong());
        verify(bookRepository, never()).updateBookAuthor(anyLong(), anyLong());
    }

    @Test
    void transferBook_AuthorNotFound_ThrowsException() {
        // Arrange
        Long bookId = 1L;
        Long newAuthorId = 999L;
        when(bookRepository.bookExists(bookId)).thenReturn(true);
        when(authorRepository.authorNotExists(newAuthorId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookService.transferBook(bookId, newAuthorId));
        
        assertEquals("Автор с ID 999 не найден", exception.getMessage());
        verify(bookRepository, times(1)).bookExists(bookId);
        verify(authorRepository, times(1)).authorNotExists(newAuthorId);
        verify(bookRepository, never()).updateBookAuthor(anyLong(), anyLong());
    }
}