/*
package dev.ltocca.loanranger;

import dev.ltocca.loanranger.businessLogic.LoginController;
import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.ORM.UserDAO;
import dev.ltocca.loanranger.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private UserDAO userDAO;

    private LoginController loginController;
    private Member testMember;
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        loginController = new LoginController(userDAO);
        hashedPassword = PasswordHasher.hash("password123");
        testMember = new Member("testuser", "Test User", "test@example.com", hashedPassword);
        testMember.setId(1L);
    }

    @Test
    void login_withValidCredentials_shouldReturnUser() throws SQLException {

        try (MockedConstruction<UserDAO> mockedConstruction = Mockito.mockConstruction(
                UserDAO.class,
                (mock, context) -> {
                    when(mock.getUserByEmail(anyString())).thenReturn(Optional.of(testMember));
                }
        )) {
            // Now, when LoginController.login() calls 'new UserDAO()', it gets the mocked instance
            Optional<User> result = loginController.login("test@example.com", "password123");

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        }
    }

    @Test
    void login_withInvalidPassword_shouldReturnEmpty() throws SQLException {
        try (MockedConstruction<UserDAO> mockedConstruction = Mockito.mockConstruction(
                UserDAO.class,
                (mock, context) -> {
                    when(mock.getUserByEmail("test@example.com")).thenReturn(Optional.of(testMember));
                }
        ); MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check("wrongpassword", testMember.getPassword())).thenReturn(false);

            Optional<User> result = loginController.login("test@example.com", "wrongpassword");

            assertThat(result).isEmpty();
        }
    }

    @Test
    void login_withNonExistentEmail_shouldReturnEmpty() throws SQLException {
        try (MockedConstruction<UserDAO> mockedConstruction = Mockito.mockConstruction(
                UserDAO.class,
                (mock, context) -> {
                    // Configure the mock UserDAO to return Optional.empty for any email
                    when(mock.getUserByEmail(anyString())).thenReturn(Optional.empty());
                }
        )) {
            Optional<User> result = loginController.login("nonexistent@example.com", "password123");

            assertThat(result).isEmpty();
        }
    }

    @Test
    void login_withNullEmail_shouldReturnEmpty() throws SQLException {
        Optional<User> result = loginController.login(null, "password");

        assertThat(result).isEmpty();
    }

    @Test
    void login_withNullPassword_shouldReturnEmpty() throws SQLException {
        Optional<User> result = loginController.login("test@example.com", null);

        assertThat(result).isEmpty();
    }

    @Test
    void register_withValidMemberData_shouldCreateUser() throws SQLException {
        try (MockedConstruction<UserDAO> mockedConstruction = Mockito.mockConstruction(
                UserDAO.class,
                (mock, context) -> {
                    when(mock.createUser(any(User.class))).thenReturn(testMember);
                }
        )) {
            User result = loginController.register(UserRole.MEMBER, "newuser", "New User",
                    "new@example.com", "password123", null);

            assertThat(result).isNotNull();
            UserDAO mockUserDAO = mockedConstruction.constructed().get(0);
            verify(mockUserDAO).createUser(any(Member.class));
        }
    }

    @Test
    void register_withEmptyUsername_shouldThrowException() {
        assertThatThrownBy(() ->
                loginController.register(UserRole.MEMBER, "", "Name", "email@test.com", "password", null))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_withInvalidEmail_shouldThrowException() {
        assertThatThrownBy(() ->
                loginController.register(UserRole.MEMBER, "user", "Name", "invalidemail", "password", null))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_withShortPassword_shouldThrowException() {
        assertThatThrownBy(() ->
                loginController.register(UserRole.MEMBER, "user", "Name", "email@test.com", "short", null))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_librarianWithoutLibrary_shouldThrowException() {
        assertThatThrownBy(() ->
                loginController.register(UserRole.LIBRARIAN, "user", "Name", "email@test.com", "password", null))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}*/
package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.ORM.UserDAO;
import dev.ltocca.loanranger.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private LoginController loginController;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // We don't need a real hash for this unit test
        testMember = new Member("testuser", "Test User", "test@example.com", "hashed_password");
        testMember.setId(1L);
    }

    @Test
    void login_withValidCredentials_shouldReturnUser() throws SQLException {
        // Given
        when(userDAO.getUserByEmail("test@example.com")).thenReturn(Optional.of(testMember));
        // We must mock the static PasswordHasher.check method
        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check("password123", "hashed_password")).thenReturn(true);

            // When
            Optional<User> result = loginController.login("test@example.com", "password123");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testMember);
        }
    }

    @Test
    void login_withInvalidPassword_shouldReturnEmpty() throws SQLException {
        // Given
        when(userDAO.getUserByEmail("test@example.com")).thenReturn(Optional.of(testMember));
        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check("wrongpassword", "hashed_password")).thenReturn(false);

            // When
            Optional<User> result = loginController.login("test@example.com", "wrongpassword");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Test
    void register_withValidMemberData_shouldCreateAndReturnUser() throws SQLException {
        // Given
        User newUser = new Member("newuser", "New User", "new@example.com", "new_password");
        when(userDAO.createUser(any(Member.class))).thenReturn(newUser);

        // When
        User result = loginController.register(UserRole.MEMBER, "newuser", "New User", "new@example.com", "password123", null);

        // Then
        assertThat(result).isEqualTo(newUser);
        verify(userDAO).createUser(any(Member.class));
    }

    @Test
    void register_withInvalidEmail_shouldThrowException() {
        assertThatThrownBy(() -> loginController.register(UserRole.MEMBER, "user", "Name", "invalid-email", "password123", null))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email address");
    }

    @Test
    void register_librarianWithoutLibrary_shouldThrowException() {
        assertThatThrownBy(() -> loginController.register(UserRole.LIBRARIAN, "lib", "Lib", "lib@a.com", "password123", null))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Librarian must be assigned to a library");
    }
}