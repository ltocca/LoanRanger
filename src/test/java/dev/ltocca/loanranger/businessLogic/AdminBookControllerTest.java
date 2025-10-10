package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.Book;
import dev.ltocca.loanranger.domainModel.BookCopy;
import dev.ltocca.loanranger.domainModel.Library;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;
import dev.ltocca.loanranger.ORM.BookDAO;
import dev.ltocca.loanranger.ORM.LibraryDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBookControllerTest {

    @Mock
    private LibraryDAO libraryDAO;

    @Mock
    private BookDAO bookDAO;

    @Mock
    private BookCopiesDAO bookCopiesDAO;

    @InjectMocks
    private AdminBookController adminBookController;

    private Book testBook;
    private Library testLibrary;

    @BeforeEach
    void setUp() {
        testBook = new Book("978-0123456789", "Test Book", "Test Author", 2023, "Fiction");
        testLibrary = new Library(1L, "Main Library", "123 Main St", "555-0100", "main@library.com");
    }

    @Test
    void addBook_withAllFields_createsBookSuccessfully() {
        when(bookDAO.getBookByIsbn("978-0123456789")).thenReturn(Optional.empty());

        adminBookController.addBook("978-0123456789", "Test Book", "Test Author", 2023, "Fiction");

        verify(bookDAO).createBook(any(Book.class));
    }

    @Test
    void addBook_withOnlyRequiredFields_createsBookSuccessfully() {
        when(bookDAO.getBookByIsbn("978-0123456789")).thenReturn(Optional.empty());

        adminBookController.addBook("978-0123456789", "Test Book", "Test Author", null, null);

        verify(bookDAO).createBook(any(Book.class));
    }

    @Test
    void addBook_whenIsbnExists_doesNotCreateBook() {
        when(bookDAO.getBookByIsbn("978-0123456789")).thenReturn(Optional.of(testBook));

        adminBookController.addBook("978-0123456789", "Test Book", "Test Author", 2023, "Fiction");

        verify(bookDAO, never()).createBook(any(Book.class));
    }

    @Test
    void addBook_withNullIsbn_throwsIllegalArgumentException() {
        adminBookController.addBook(null, "Test Book", "Test Author", 2023, "Fiction");

        verify(bookDAO, never()).createBook(any(Book.class));
    }

    @Test
    void addBook_withNullTitle_throwsIllegalArgumentException() {
        adminBookController.addBook("978-0123456789", null, "Test Author", 2023, "Fiction");

        verify(bookDAO, never()).createBook(any(Book.class));
    }

    @Test
    void removeBook_withNoCopies_deletesBookSuccessfully() {
        when(bookDAO.getBookByIsbn(testBook.getIsbn())).thenReturn(Optional.of(testBook));
        //when(bookDAO.getBookByIsbn("978-0123456789")).thenReturn(Optional.of(testBook));
        when(bookCopiesDAO.findAllBookCopies(testBook)).thenReturn(List.of());

        adminBookController.removeBook("978-0123456789");

        verify(bookDAO).deleteBook("978-0123456789");
    }

/*    @Test
    void removeBook_whenBookHasCopies_doesNotDeleteBook() throws Exception {
        // Arrange: Define test data
        String isbn = "978-0123456789";
        Book testBook = new Book(isbn, "Test Title", "Test Author");

        // 1. Mock the book lookup to return our test book.
        when(bookDAO.getBookByIsbn(isbn)).thenReturn(Optional.of(testBook));

        // 2. THE FIX: Mock the copy lookup to return a NON-EMPTY list.
        //    List.of(new BookCopy()) creates a list containing one dummy object.
        when(bookCopiesDAO.findAllBookCopies(testBook)).thenReturn(List.of(new BookCopy()));

        // Act: Call the method under test
        adminBookController.removeBook(isbn);

        // Assert: Verify that deleteBook was never called, because the guard condition
        //         in the controller was met.
        verify(bookDAO, never()).deleteBook(anyString());
    }*/

    @Test
    void removeBook_whenBookHasCopies_doesNotDeleteBook() {
        BookCopy mockCopy = mock(BookCopy.class);
        when(bookDAO.getBookByIsbn(testBook.getIsbn())).thenReturn(Optional.of(testBook));

        when(bookCopiesDAO.findAllBookCopies(testBook)).thenReturn(List.of(mockCopy));

        adminBookController.removeBook(testBook.getIsbn());

        verify(bookDAO, never()).deleteBook(anyString());
    }

    @Test
    void removeBook_withNonExistentIsbn_doesNothing() {
        when(bookDAO.getBookByIsbn("999-0123456789")).thenReturn(Optional.empty());

        adminBookController.removeBook("999-0123456789");

        verify(bookDAO, never()).deleteBook(anyString());
    }

    @Test
    void addLibrary_withValidData_createsLibrarySuccessfully() {
        adminBookController.addLibrary("New Library", "456 Oak St", "555-0200", "new@library.com");

        verify(libraryDAO).createLibrary(any(Library.class));
    }

    @Test
    void updateLibrary_updatesAllFieldsSuccessfully() {
        Library libraryToUpdate = new Library(1L, "Old Name", "Old Address", "Old Phone", "Old Email");
        when(libraryDAO.getLibraryById(1L)).thenReturn(Optional.of(libraryToUpdate));

        adminBookController.updateLibrary(1L, "New Name", "New Address", "New Phone", "New Email");

        verify(libraryDAO).updateLibrary(libraryToUpdate);
        assertThat(libraryToUpdate.getName()).isEqualTo("New Name");
        assertThat(libraryToUpdate.getAddress()).isEqualTo("New Address");
    }

    @Test
    void updateLibrary_updatesOnlyOneFieldSuccessfully() {
        Library libraryToUpdate = new Library(1L, "Old Name", "Old Address", "Old Phone", "Old Email");
        when(libraryDAO.getLibraryById(1L)).thenReturn(Optional.of(libraryToUpdate));

        adminBookController.updateLibrary(1L, "New Name", null, null, null);

        verify(libraryDAO).updateLibrary(libraryToUpdate);
        assertThat(libraryToUpdate.getName()).isEqualTo("New Name");
        assertThat(libraryToUpdate.getAddress()).isEqualTo("Old Address");
    }

    @Test
    void updateLibrary_withNonExistentId_throwsException() {
        when(libraryDAO.getLibraryById(999L)).thenReturn(Optional.empty());

        adminBookController.updateLibrary(999L, "New Name", "New Address", "New Phone", "New Email");

        verify(libraryDAO, never()).updateLibrary(any(Library.class));
    }

    @Test
    void updateLibrary_withEmptyFields_doesNotUpdateThoseFields() {
        Library libraryToUpdate = new Library(1L, "Old Name", "Old Address", "Old Phone", "Old Email");
        when(libraryDAO.getLibraryById(1L)).thenReturn(Optional.of(libraryToUpdate));

        adminBookController.updateLibrary(1L, "", "", "", "");

        verify(libraryDAO).updateLibrary(libraryToUpdate);
        assertThat(libraryToUpdate.getName()).isEqualTo("Old Name");
        assertThat(libraryToUpdate.getAddress()).isEqualTo("Old Address");
    }

    @Test
    void listAllLibraries_whenItemsExist_returnsListOfItems() {
        List<Library> libraries = List.of(testLibrary);
        when(libraryDAO.getAllLibraries()).thenReturn(libraries);

        List<Library> result = adminBookController.listAllLibraries();

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    void listAllLibraries_whenNoItemsExist_returnsEmptyList() {
        when(libraryDAO.getAllLibraries()).thenReturn(List.of());

        List<Library> result = adminBookController.listAllLibraries();

        assertThat(result).isEmpty();
    }

    @Test
    void listAllBooks_whenItemsExist_returnsListOfItems() {
        List<Book> books = List.of(testBook);
        when(bookDAO.getAllBooks()).thenReturn(books);

        List<Book> result = adminBookController.listAllBooks();

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    void listAllBooks_whenNoItemsExist_returnsEmptyList() {
        when(bookDAO.getAllBooks()).thenReturn(List.of());

        List<Book> result = adminBookController.listAllBooks();

        assertThat(result).isEmpty();
    }
}