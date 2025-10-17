
package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.domainModel.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BookDAOTest extends OrmIntegrationTestBase {

    @BeforeEach
    void setUp() throws Exception {
        executeSchemaScript();
    }

    @Test
    void createBook_ShouldCreateBookSuccessfully() {
        
        Book book = new Book("1234567890", "Test Book", "Test Author", 2024, "Fiction");

        
        Book createdBook = bookDAO.createBook(book);

        
        assertThat(createdBook).isNotNull();
        assertThat(createdBook.getIsbn()).isEqualTo("1234567890");
        assertThat(createdBook.getTitle()).isEqualTo("Test Book");
        assertThat(createdBook.getAuthor()).isEqualTo("Test Author");
        assertThat(createdBook.getPublicationYear()).isEqualTo(2024);
        assertThat(createdBook.getGenre()).isEqualTo("Fiction");
    }

    @Test
    void getBookByIsbn_ShouldReturnBookWhenExists() {
        
        Book book = createTestBook();

        
        Optional<Book> foundBook = bookDAO.getBookByIsbn(TEST_BOOK_ISBN);

        
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo(TEST_BOOK_TITLE);
        assertThat(foundBook.get().getAuthor()).isEqualTo(TEST_BOOK_AUTHOR);
    }

    @Test
    void getBookByIsbn_ShouldReturnEmptyWhenBookNotFound() {
        
        Optional<Book> foundBook = bookDAO.getBookByIsbn("0000000000");

        
        assertThat(foundBook).isEmpty();
    }

    @Test
    void getBookByTitle_ShouldReturnBookWhenTitleExists() {
        
        createTestBook();

        
        Optional<Book> foundBook = bookDAO.getBookByTitle(TEST_BOOK_TITLE);

        
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getIsbn()).isEqualTo(TEST_BOOK_ISBN);
    }

    @Test
    void getAllBooks_ShouldReturnAllBooks() {
        
        createTestBook();
        Book book2 = new Book("978-0596009205", "Head First Design Patterns", "Eric Freeman", 2004, "Programming");
        bookDAO.createBook(book2);

        
        List<Book> books = bookDAO.getAllBooks();

        
        assertThat(books).hasSize(2);
        assertThat(books).extracting(Book::getTitle)
                .containsExactlyInAnyOrder(TEST_BOOK_TITLE, "Head First Design Patterns");
    }

    @Test
    void findBooksByAuthor_ShouldReturnBooksByAuthor() {
        
        createTestBook();
        Book book2 = new Book("978-0131872486", "Java Concurrency in Practice", "Joshua Bloch", 2006, "Programming");
        bookDAO.createBook(book2);
        Book book3 = new Book("978-0201633610", "Design Patterns", "Erich Gamma", 1994, "Programming");
        bookDAO.createBook(book3);

        
        List<Book> joshuaBooks = bookDAO.findBooksByAuthor("Joshua Bloch");

        
        assertThat(joshuaBooks).hasSize(2);
        assertThat(joshuaBooks).allMatch(book -> book.getAuthor().equals("Joshua Bloch"));
    }

    @Test
    void findBooksByPublicationYear_ShouldReturnBooksByYear() {
        
        createTestBook();
        Book book2 = new Book("978-0134685992", "Another 2017 Book", "Some Author", 2017, "Fiction");
        bookDAO.createBook(book2);
        Book book3 = new Book("978-0134685993", "2020 Book", "Another Author", 2020, "Fiction");
        bookDAO.createBook(book3);

        
        List<Book> books2017 = bookDAO.findBooksByPublicationYear(2017);

        
        assertThat(books2017).hasSize(2);
        assertThat(books2017).allMatch(book -> book.getPublicationYear() == 2017);
    }

    @Test
    void findBookByIsbn_ShouldReturnBooksWithPartialIsbnMatch() {
        
        createTestBook();
        Book book2 = new Book("978-0134686000", "Another Book", "Some Author", 2020, "Fiction");
        bookDAO.createBook(book2);

        
        List<Book> foundBooks = bookDAO.findBookByIsbn("013468");

        
        assertThat(foundBooks).hasSize(2);
        assertThat(foundBooks).extracting(Book::getIsbn)
                .allMatch(isbn -> isbn.contains("013468"));
    }

    @Test
    void deleteBook_ShouldRemoveBookFromDatabase() {
        
        Book book = createTestBook();

        
        bookDAO.deleteBook(TEST_BOOK_ISBN);

        
        Optional<Book> deletedBook = bookDAO.getBookByIsbn(TEST_BOOK_ISBN);
        assertThat(deletedBook).isEmpty();
    }

    @Test
    void deleteBookByObject_ShouldRemoveBookFromDatabase() {
        
        Book book = createTestBook();

        
        bookDAO.deleteBook(book);

        
        Optional<Book> deletedBook = bookDAO.getBookByIsbn(TEST_BOOK_ISBN);
        assertThat(deletedBook).isEmpty();
    }
}