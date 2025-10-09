package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.domainModel.State.AvailableState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) 
@Testcontainers 
@Import({BookDAO.class, BookCopiesDAO.class, LibraryDAO.class, UserDAO.class, LoanDAO.class, ReservationDAO.class})
@Transactional 

public abstract class OrmIntegrationTestBase {

    
    @Container
    protected static final PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>("postgres:17-alpine") 
                    .withDatabaseName("loanranger_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    
    @Autowired
    protected DataSource dataSource; 

    @Autowired
    protected JdbcTemplate jdbcTemplate; 

    @Autowired
    protected LibraryDAO libraryDAO;

    @Autowired
    protected UserDAO userDAO;

    @Autowired
    protected BookDAO bookDAO;

    @Autowired
    protected BookCopiesDAO bookCopiesDAO;

    @Autowired
    protected LoanDAO loanDAO;

    @Autowired
    protected ReservationDAO reservationDAO;

    
    
    protected static final String TEST_LIB_NAME = "Test Library";
    protected static final String TEST_LIB_PHONE = "555-1234";
    protected static final String TEST_LIB_EMAIL = "test@library.com";
    protected static final String TEST_LIB_ADDRESS = "123 Test St";

    protected static final String TEST_MEMBER_USERNAME = "test_member";
    protected static final String TEST_MEMBER_NAME = "Test Member";
    protected static final String TEST_MEMBER_EMAIL = "test@test.com";
    protected static final String TEST_MEMBER_PASSWORD = "password123";

    protected static final String TEST_LIBRARIAN_USERNAME = "test_librarian";
    protected static final String TEST_LIBRARIAN_NAME = "Test Librarian";
    protected static final String TEST_LIBRARIAN_EMAIL = "librarian@test.com";
    protected static final String TEST_LIBRARIAN_PASSWORD = "password123";

    protected static final String TEST_BOOK_ISBN = "978-0134685991";
    protected static final String TEST_BOOK_TITLE = "Effective Java";
    protected static final String TEST_BOOK_AUTHOR = "Joshua Bloch";
    protected static final int TEST_BOOK_YEAR = 2017;
    protected static final String TEST_BOOK_GENRE = "Programming";

    
    
    protected void executeSchemaScript() throws SQLException {
        try (Connection connection = dataSource.getConnection()) { 
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/schema.sql"));
            System.out.println("Schema loaded successfully using the main test DataSource.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute schema script: " + e.getMessage(), e);
        }
    }

    
    
    protected Library createTestLibrary() {
        Library library = new Library();
        library.setName(TEST_LIB_NAME);
        library.setAddress(TEST_LIB_ADDRESS);
        library.setPhone(TEST_LIB_PHONE);
        library.setEmail(TEST_LIB_EMAIL);
        return libraryDAO.createLibrary(library);
    }

    
    protected Member createTestMember() {
        Member member = new Member(TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_PASSWORD);
        member.setName(TEST_MEMBER_NAME);
        
        
        return (Member) userDAO.createUser(member);
    }

    
    protected Librarian createTestLibrarian(Library library) {
        Librarian librarian = new Librarian(TEST_LIBRARIAN_USERNAME, TEST_LIBRARIAN_PASSWORD, TEST_LIBRARIAN_EMAIL, TEST_LIBRARIAN_NAME, library);
        return (Librarian) userDAO.createUser(librarian);
    }

    
    protected Book createTestBook() {
        Book book = new Book(TEST_BOOK_ISBN, TEST_BOOK_TITLE, TEST_BOOK_AUTHOR);
        book.setPublicationYear(TEST_BOOK_YEAR);
        book.setGenre(TEST_BOOK_GENRE);
        return bookDAO.createBook(book);
    }

    
    protected BookCopy createTestBookCopy(Book book, Library library) {
        BookCopy bookCopy = new BookCopy(book, library, new AvailableState());
        return bookCopiesDAO.createCopy(bookCopy);
    }

    
    protected Loan createTestLoan(BookCopy bookCopy, Member member) {
        return loanDAO.createLoan(bookCopy, member);
    }

    
    protected Reservation createTestReservation(BookCopy bookCopy, Member member) {
        Reservation reservation = new Reservation(null, bookCopy, member, java.time.LocalDate.now());
        return reservationDAO.createReservation(reservation);
    }
}