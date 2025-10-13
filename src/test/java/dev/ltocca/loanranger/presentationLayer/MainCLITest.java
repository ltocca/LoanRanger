package dev.ltocca.loanranger.presentationLayer;

import dev.ltocca.loanranger.businessLogic.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = "app.cli.enabled=true")
public class MainCLITest {

    @Container
    static final PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("testdb_cli")
                    .withUsername("testuser")
                    .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
        registry.add("mail.enabled", () -> "false");
    }

    // --- Constants based on default.sql ---
    private static final String ADMIN_EMAIL = "admin@library.org";

    @Autowired private LoginController loginController;
    @Autowired private MemberBookController memberBookController;
    @Autowired private MemberAccountController memberAccountController;
    @Autowired private LibrarianBookController librarianBookController;
    @Autowired private LibrarianAccountController librarianAccountController;
    @Autowired private AdminBookController adminBookController;
    @Autowired private AdminUsersController adminUsersController;
    @Autowired private AdminDatabaseController adminDatabaseController;
    @Autowired private AdminAccountController adminAccountController;

    // manually constructed instance for testing.
    private MainCLI cliTestInstance;

    private final InputStream systemIn = System.in;
    private final PrintStream systemOut = System.out;
    private final PrintStream systemErr = System.err;
    private ByteArrayOutputStream testOut;
    private static final String LIBRARIAN_EMAIL = "jane@library.org"; // Jane Smith, Library 1
    private static final String MEMBER_EMAIL = "john@example.com"; // John Doe, ID 1
    private static final String MEMBER_WITH_LOAN_EMAIL = "lisa@example.com"; // Lisa Ray, ID 8, has active loan
    private static final String PASSWORD = "password"; // Common password for all users
    private static final String AVAILABLE_COPY_ID = "1"; // The Great Gatsby, Library 1, AVAILABLE
    private static final String ACTIVE_LOAN_ID = "2"; // Loan for Lisa Ray (ID 8)
    private static final String GATSBY_ISBN = "9781234567890"; // The Great Gatsby (casual isbn)
    private static final String NEW_GATSBY_ISBN = "97800000990"; // The Great Gatsby (casual isbn)
    // Use @MockBean ONLY to prevent Spring from auto-running the CommandLineRunner.
    // We will not interact with this mock directly in the tests.
    @MockitoBean
    private MainCLI mainCliMockToPreventAutoRun;

    @BeforeEach
    void setUp() {
        cliTestInstance = new MainCLI(
                loginController,
                memberBookController,
                memberAccountController,
                librarianBookController,
                librarianAccountController,
                adminBookController,
                adminUsersController,
                adminDatabaseController,
                adminAccountController
        );

        adminDatabaseController.recreateSchemaAndAdmin();
        adminDatabaseController.generateDefaultDatabase();
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
        System.setErr(new PrintStream(testOut));
    }

    @AfterEach
    void tearDown() {
        System.setIn(systemIn);
        System.setOut(systemOut);
        System.setErr(systemErr);
    }

    private void provideInput(String data) {
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        System.setIn(testIn);
    }

    private String getOutput() {
        return testOut.toString(StandardCharsets.UTF_8);
    }

    // I. Authentication and Pre-Login Scenarios

    @Test
    void cli_loginWithNonExistentEmail_showsErrorMessage() {
        String input = "1\n" +
                "nouser@exists.com\n" +
                PASSWORD + "\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login failed. Please check your credentials.");
        assertThat(output).contains("--- Welcome to LoanRanger ---");
    }

    @Test
    void cli_registerWithAlreadyTakenEmail_showsErrorMessage() {
        String input = "2\n" +
                "seconduser\n" +
                "Second User\n" +
                MEMBER_EMAIL + "\n" +
                "password123\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Registration failed");
        assertThat(output).contains("already exists");
    }

    @Test
    void cli_memberSearchAndViewBooks_succeeds() {
        // Login as existing member
        String input = "1\n" +
                MEMBER_EMAIL + "\n" +
                PASSWORD + "\n" +
                "1\n" +  // Search for Books
                "1\n" +  // Search by Title
                "Gatsby\n" +  // Search for existing book
                "10\n" + // Logout
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome John Doe!");
        assertThat(output).contains("Search Results");
        assertThat(output).contains("The Great Gatsby");
    }

    @Test
    void cli_memberReserveBook_succeeds() {
        String input = "1\n" +
                MEMBER_EMAIL + "\n" +
                PASSWORD + "\n" +
                "2\n" +
                AVAILABLE_COPY_ID + "\n" +
                "4\n" +
                "1\n" +
                "10\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome John Doe!");
        assertThat(output).contains("Reservation successful");
        assertThat(output).contains("Your Active Reservations");
    }

    @Test
    void cli_memberViewReservations_succeeds() {
        // Member with id n:5 (Alice Brown) has a PENDING reservation (ID 1) on Copy 3 (1984)
        String aliceEmail = "alice@example.com";

        String input = "1\n" +
                aliceEmail + "\n" +
                PASSWORD + "\n" +
                "4\n" +
                "1\n" +
                "10\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome Alice Brown!");
        assertThat(output).contains("Your Active Reservations");
        assertThat(output).contains("PENDING");
        assertThat(output).contains("1984");
    }

    @Test
    void cli_memberChangePassword_withIncorrectCurrentPassword_fails() {
        // Use existing member John Doe
        String input = "1\n" +
                MEMBER_EMAIL + "\n" +
                PASSWORD + "\n" +
                "8\n" +
                "wrong_current_pass\n" +  // Incorrect current password
                "new_secure_password\n" +
                "10\n" + // Logout
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Error: inserted incorrect current password");
    }

    @Test
    void cli_memberViewAllLoans_succeeds() {
        // Use existing member Lisa Ray (id 8) who has an active loan (id 2)
        String input = "1\n" +
                MEMBER_WITH_LOAN_EMAIL + "\n" +
                PASSWORD + "\n" +
                "3\n" +
                "1\n" +
                "10\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome Lisa Ray!");
        assertThat(output).contains("Your Active Loans");
        assertThat(output).contains(ACTIVE_LOAN_ID);
        assertThat(output).contains("Active");
    }

    @Test
    void cli_librarianLoginAndViewLoans_succeeds() {
        // Login as existing librarian Jane Smith from Library )
        String input = "1\n" +
                LIBRARIAN_EMAIL + "\n" +
                PASSWORD + "\n" +
                "4\n" +
                "1\n" +
                "13\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome Jane Smith!");
        assertThat(output).contains("Active Loans in This Library");
        // The loan with id 2 is in Library 2, while id 1 is in Library 2. Jane Smith is in Library 1.
        // The list should be empty or only contain loans that belong to Library 1.
        assertThat(output).contains("No loans found for this category.");
    }

    @Test
    void cli_librarianAddBookCopy_succeeds() {
        String input = "1\n" +
                LIBRARIAN_EMAIL + "\n" +
                PASSWORD + "\n" +
                "7\n" +
                GATSBY_ISBN + "\n" +  // ISBN for The Great Gatsby
                "13\n" + // Logout
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome Jane Smith!");
        assertThat(output).contains("New copy added with ID");
    }

    @Test
    void cli_adminFullLibraryLifecycle_succeeds() {
        String input = "1\n" +
                ADMIN_EMAIL + "\n" +
                PASSWORD + "\n" +
                "1\n" +
                "Test Library\n" +
                "123 Test St\n" +
                "555-TEST\n" +
                "test@library.com\n" +
                "4\n" +
                "17\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome Admin One!");
        assertThat(output).contains("Library 'Test Library' added successfully");
        assertThat(output).contains("Test Library");
    }

    @Test
    void cli_adminListAllUsers_succeeds() {
        String input = "1\n" +
                ADMIN_EMAIL + "\n" +
                PASSWORD + "\n" +
                "12\n" +
                "17\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome Admin One!");
        assertThat(output).contains("--- All Users ---");
        assertThat(output).contains("john_doe");
        assertThat(output).contains("jane_smith");
        assertThat(output).contains("admin1");
    }

    @Test
    void cli_adminViewBookDetails_succeeds() {
        String input = "1\n" +
                ADMIN_EMAIL + "\n" +
                PASSWORD + "\n" +
                "8\n" +
                GATSBY_ISBN + "\n" +
                "17\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome Admin One!");
        assertThat(output).contains("--- Book Details ---");
        assertThat(output).contains("The Great Gatsby");
    }

    @Test
    void cli_adminAddNewBook_succeeds() {
        String input = "1\n" +
                ADMIN_EMAIL + "\n" +
                PASSWORD + "\n" +
                "5\n" +
                "9789876543210\n" +
                "Test Book Title\n" +
                "Test Author\n" +
                "2024\n" +
                "Fiction\n" +
                "17\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome Admin One!");
        assertThat(output).contains("Test Book Title' added successfully");
    }

    @Test
    void cli_adminListAllBooks_succeeds() {
        String input = "1\n" +
                ADMIN_EMAIL + "\n" +
                PASSWORD + "\n" +
                "7\n" +
                "17\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome Admin One!");
        assertThat(output).contains("--- All Books ---");
        assertThat(output).contains("The Great Gatsby");
        assertThat(output).contains("1984");
    }

    @Test
    void cli_properLogoutReturnsToMainMenu() {
        String input = "1\n" +
                ADMIN_EMAIL + "\n" +
                PASSWORD + "\n" +
                "17\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome Admin One!");
        assertThat(output).contains("--- Welcome to LoanRanger ---");
        assertThat(output).contains("Goodbye!");
    }

    @Test
    void cli_basicMemberRegistrationAndLogin_succeeds() {
        String input = "2\n" +
                "basicuser\n" +
                "Basic User\n" +
                "basic.user@example.com\n" +
                "password123\n" +
                "1\n" +
                "basic.user@example.com\n" +
                "password123\n" +
                "10\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Registration successful! You can now log in.");
        assertThat(output).contains("Login successful: welcome Basic User!");
        assertThat(output).contains("--- Member Menu ---");
    }
}