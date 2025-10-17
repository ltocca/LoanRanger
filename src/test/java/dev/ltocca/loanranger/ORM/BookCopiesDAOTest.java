
package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.domainModel.Book;
import dev.ltocca.loanranger.domainModel.BookCopy;
import dev.ltocca.loanranger.domainModel.Library;
import dev.ltocca.loanranger.domainModel.State.AvailableState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


class BookCopiesDAOTest extends OrmIntegrationTestBase {

    private Library testLibrary;
    private Book testBook;

    @BeforeEach
    void setUp() throws Exception { 
        executeSchemaScript(); 
        
        testBook = createTestBook(); 
        testLibrary = createTestLibrary(); 
    }

    @Test
    void createCopy_ShouldCreateBookCopySuccessfully() {
        
        BookCopy bookCopy = new BookCopy(testBook, testLibrary, new AvailableState());

        
        BookCopy createdCopy = bookCopiesDAO.createCopy(bookCopy);

        
        assertThat(createdCopy).isNotNull();
        assertThat(createdCopy.getCopyId()).isNotNull();
        assertThat(createdCopy.getBook().getIsbn()).isEqualTo(TEST_BOOK_ISBN);
        assertThat(createdCopy.getLibrary().getId()).isEqualTo(testLibrary.getId());
    }

    @Test
    void getCopyById_ShouldReturnBookCopyWhenExists() {
        
        BookCopy bookCopy = createTestBookCopy(testBook, testLibrary); 

        
        Optional<BookCopy> foundCopy = bookCopiesDAO.getCopyById(bookCopy.getCopyId());

        
        assertThat(foundCopy).isPresent();
        assertThat(foundCopy.get().getCopyId()).isEqualTo(bookCopy.getCopyId());
        assertThat(foundCopy.get().getBook().getTitle()).isEqualTo(TEST_BOOK_TITLE);
    }

    @Test
    void getCopyById_ShouldReturnEmptyWhenBookCopyNotFound() {
        
        Optional<BookCopy> foundCopy = bookCopiesDAO.getCopyById(999L);

        
        assertThat(foundCopy).isEmpty();
    }

    @Test
    void getAllBookCopies_ShouldReturnAllCopies() {
        
        createTestBookCopy(testBook, testLibrary); 
        Book book2 = new Book("978-0596009205", "Head First Design Patterns", "Eric Freeman");
        bookDAO.createBook(book2);
        createTestBookCopy(book2, testLibrary); 

        
        List<BookCopy> copies = bookCopiesDAO.getAllBookCopies();

        
        assertThat(copies).hasSize(2);
    }

    @Test
    void searchByTitle_ShouldReturnMatchingCopies() {
        
        Book book2 = new Book("978-0596009205", "Effective Python", "Brett Slatkin");
        bookDAO.createBook(book2);

        createTestBookCopy(testBook, testLibrary); 
        createTestBookCopy(book2, testLibrary); 

        
        List<BookCopy> foundCopies = bookCopiesDAO.searchByTitle("Effective");

        
        assertThat(foundCopies).hasSize(2);
    }
}