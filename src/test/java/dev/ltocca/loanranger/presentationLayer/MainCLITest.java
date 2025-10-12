package dev.ltocca.loanranger.presentationLayer;

import dev.ltocca.loanranger.businessLogic.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
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

    // Use @MockBean ONLY to prevent Spring from auto-running the CommandLineRunner.
    // We will not interact with this mock directly in the tests.
    @MockBean
    private MainCLI mainCliMockToPreventAutoRun;

    // Autowire all the real dependencies that MainCLI needs.
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

    @BeforeEach
    void setUp() {
        // Manually construct a real MainCLI instance using the autowired dependencies.
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

        // Standard setup for DB and I/O redirection using "production" values
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

    @Test
    void cli_adminLoginAndListBooks_succeeds() {
        String input = "1\n" +
                "super@library.org\n" +
                "password\n" +
                "7\n" +
                "17\n" +
                "3\n";
        provideInput(input);

        // Run the test on our manually created instance
        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login successful: welcome Super Admin!");
        assertThat(output).contains("--- Admin Menu ---");
        assertThat(output).contains("--- All Books ---");
        assertThat(output).contains("1984");
        assertThat(output).contains("Goodbye!");
    }

    @Test
    void cli_registerAndLoginNewMember_succeeds() {
        String input = "2\n" +
                "new_user\n" +
                "New User Name\n" +
                "new.user@example.com\n" +
                "password123\n" +
                "1\n" +
                "new.user@example.com\n" +
                "password123\n" +
                "10\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Registration successful! You can now log in.");
        assertThat(output).contains("Login successful: welcome New User Name!");
        assertThat(output).contains("--- Member Menu ---");
        assertThat(output).contains("Goodbye!");
    }

    @Test
    void cli_loginWithInvalidCredentials_showsErrorMessage() {
        String input = "1\n" +
                "admin@loanranger.com\n" +
                "wrongpassword\n" +
                "3\n";
        provideInput(input);

        cliTestInstance.run();

        String output = getOutput();
        assertThat(output).contains("Login failed. Please check your credentials.");
    }

}