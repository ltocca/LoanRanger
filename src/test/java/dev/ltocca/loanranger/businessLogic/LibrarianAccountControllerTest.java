/*
package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.Librarian;
import dev.ltocca.loanranger.domainModel.Library;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibrarianAccountControllerTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private LibrarianAccountController librarianAccountController;

    private Librarian librarian;
    private final String originalPasswordHash = "$2a$10$abcdefghijklmnopqrstuv";

    @BeforeEach
    void setUp() {
        Library library = new Library(1L, "Test Library", "Address", "Phone", "Email");
        librarian = new Librarian(1L, "libUser", "Librarian", "lib@example.com", originalPasswordHash, library);
    }

    @Test
    void changeUsername_whenUsernameIsAvailable_updatesSuccessfully() throws Exception {
        // Given
        String newUsername = "newLibUser";
        when(userDAO.findUserByUsername(newUsername)).thenReturn(Optional.empty());

        // When
        librarianAccountController.changeUsername(librarian, newUsername);

        // Then
        verify(userDAO).updateUsername(librarian.getId(), newUsername);
    }

    @Test
    void changeUsername_whenUsernameIsTaken_throwsIllegalArgumentException() throws Exception {
        // Given
        String newUsername = "takenUser";
        when(userDAO.findUserByUsername(newUsername)).thenReturn(Optional.of(new Librarian()));

        // When & Then
        assertThatThrownBy(() -> librarianAccountController.changeUsername(librarian, newUsername))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: This username is already taken by another user.");

        verify(userDAO, never()).updateUsername(anyLong(), anyString());
    }

    @Test
    void changePassword_withCorrectCurrentPassword_updatesSuccessfully() {
        // Given
        String currentPassword = "currentPassword";
        String newPassword = "newPassword123";

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check(currentPassword, originalPasswordHash)).thenReturn(true);
            mockedHasher.when(() -> PasswordHasher.hash(newPassword)).thenReturn("new_hashed_password");

            // When
            librarianAccountController.changePassword(librarian, currentPassword, newPassword);

            // Then
            verify(userDAO).updatePassword(librarian.getId(), "new_hashed_password");
        }
    }

    @Test
    void changePassword_withIncorrectCurrentPassword_throwsIllegalArgumentException() {
        // Given
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword123";

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check(currentPassword, originalPasswordHash)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> librarianAccountController.changePassword(librarian, currentPassword, newPassword))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Error: The current password you entered is incorrect.");

            verify(userDAO, never()).updatePassword(anyLong(), anyString());
        }
    }
}*/

package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.Librarian;
import dev.ltocca.loanranger.domainModel.Library;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibrarianAccountControllerTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private LibrarianAccountController librarianAccountController;

    private final String originalPasswordHash = "$2a$10$abcdefghijklmnopqrstuv";

    private Librarian librarian;
    private Library library;

    @BeforeEach
    void setUp() {
        library = new Library(1L, "Main Library", "123 Main St", "555-0100", "main@library.com");
        librarian = new Librarian(1L, "libUser", "Librarian User", "lib@example.com", "hashed_password", library);
    }

    @Test
    void changeUsername_whenUsernameIsAvailable_updatesSuccessfully() throws Exception {
        // Given
        String newUsername = "newLibUser";
        when(userDAO.findUserByUsername(newUsername)).thenReturn(Optional.empty());

        // When
        librarianAccountController.changeUsername(librarian, newUsername);

        // Then
        verify(userDAO).updateUsername(librarian.getId(), newUsername);
    }

    @Test
    void changeUsername_whenUsernameIsTaken_throwsIllegalArgumentException() throws Exception {
        // Given
        String newUsername = "takenUser";
        when(userDAO.findUserByUsername(newUsername)).thenReturn(Optional.of(new Librarian()));

        // When & Then
        assertThatThrownBy(() -> librarianAccountController.changeUsername(librarian, newUsername))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: This username is already taken by another user.");

        verify(userDAO, never()).updateUsername(anyLong(), anyString());
    }

    @Test
    void changePassword_withCorrectCurrentPassword_updatesSuccessfully() {
        // Given
        String currentPassword = "currentPassword";
        String newPassword = "newPassword123";

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check(currentPassword, "hashed_password")).thenReturn(true);
            mockedHasher.when(() -> PasswordHasher.hash(newPassword)).thenReturn("new_hashed_password");

            // When
            librarianAccountController.changePassword(librarian, currentPassword, newPassword);

            // Then
            verify(userDAO).updatePassword(librarian.getId(), "new_hashed_password");
        }
    }

    @Test
    void changeUsername_withValidNewUsername_updatesSuccessfully() {
        String newUsername = "newLibUser";
        when(userDAO.findUserByUsername(newUsername)).thenReturn(Optional.empty());

        librarianAccountController.changeUsername(librarian, newUsername);

        verify(userDAO).updateUsername(librarian.getId(), newUsername);
        assertThat(librarian.getUsername()).isEqualTo(newUsername);
    }

    @Test
    void changeUsername_withNullUsername_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> librarianAccountController.changeUsername(librarian, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: The new username cannot be null or empty.");

        verify(userDAO, never()).updateUsername(anyLong(), anyString());
    }

    @Test
    void changeUsername_withEmptyUsername_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> librarianAccountController.changeUsername(librarian, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: The new username cannot be null or empty.");

        verify(userDAO, never()).updateUsername(anyLong(), anyString());
    }

    @Test
    void changeUsername_withSameUsername_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> librarianAccountController.changeUsername(librarian, "libUser"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: The new username cannot be the same as the old one.");

        verify(userDAO, never()).updateUsername(anyLong(), anyString());
    }

    @Test
    void changeUsername_withTakenUsername_throwsIllegalArgumentException() {
        String takenUsername = "takenUser";
        when(userDAO.findUserByUsername(takenUsername)).thenReturn(Optional.of(new Librarian()));

        assertThatThrownBy(() -> librarianAccountController.changeUsername(librarian, takenUsername))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: This username is already taken by another user.");

        verify(userDAO, never()).updateUsername(anyLong(), anyString());
    }

    @Test
    void changeEmail_withValidNewEmail_updatesSuccessfully() {
        String newEmail = "new.lib@example.com";
        when(userDAO.getUserByEmail(newEmail)).thenReturn(Optional.empty());

        librarianAccountController.changeEmail(librarian, newEmail);

        verify(userDAO).updateEmail(librarian.getId(), newEmail);
        assertThat(librarian.getEmail()).isEqualTo(newEmail);
    }

    @Test
    void changeEmail_withNullEmail_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> librarianAccountController.changeEmail(librarian, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: The new email cannot be null or empty.");

        verify(userDAO, never()).updateEmail(anyLong(), anyString());
    }

    @Test
    void changeEmail_withInvalidFormat_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> librarianAccountController.changeEmail(librarian, "invalid-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: Invalid email address format. It must contain an '@'.");

        verify(userDAO, never()).updateEmail(anyLong(), anyString());
    }

    @Test
    void changeEmail_withSameEmail_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> librarianAccountController.changeEmail(librarian, "lib@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: The new email cannot be the same as the old one.");

        verify(userDAO, never()).updateEmail(anyLong(), anyString());
    }

    @Test
    void changeEmail_withTakenEmail_throwsIllegalArgumentException() {
        String takenEmail = "taken@example.com";
        when(userDAO.getUserByEmail(takenEmail)).thenReturn(Optional.of(new Librarian()));

        assertThatThrownBy(() -> librarianAccountController.changeEmail(librarian, takenEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: This email address is already in use by another account.");

        verify(userDAO, never()).updateEmail(anyLong(), anyString());
    }

    @Test
    void changePassword_withValidCredentials_updatesSuccessfully() {
        String currentPassword = "currentPassword";
        String newPassword = "newPassword123";

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check(currentPassword, "hashed_password")).thenReturn(true);
            mockedHasher.when(() -> PasswordHasher.hash(newPassword)).thenReturn("new_hashed_password");

            librarianAccountController.changePassword(librarian, currentPassword, newPassword);

            verify(userDAO).updatePassword(librarian.getId(), "new_hashed_password");
            assertThat(librarian.getPassword()).isEqualTo("new_hashed_password");
        }
    }

    @Test
    void changePassword_withIncorrectCurrentPassword_throwsIllegalArgumentException() {
        String wrongPassword = "wrongPassword";
        String newPassword = "newPassword123";

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check(wrongPassword, originalPasswordHash)).thenReturn(false);

            assertThatThrownBy(() -> librarianAccountController.changePassword(librarian, wrongPassword, newPassword))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Error: The current password you entered is incorrect.");

            verify(userDAO, never()).updatePassword(anyLong(), anyString());
        }
    }

    @Test
    void changePassword_withShortNewPassword_throwsIllegalArgumentException() {
        String currentPassword = "currentPassword";
        String shortPassword = "short";

        assertThatThrownBy(() -> librarianAccountController.changePassword(librarian, currentPassword, shortPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: The new password must be at least 6 characters long.");

        verify(userDAO, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void changePassword_withSamePassword_throwsIllegalArgumentException() {
        String samePassword = "samePassword";

        assertThatThrownBy(() -> librarianAccountController.changePassword(librarian, samePassword, samePassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: The new password cannot be the same as the old one.");

        verify(userDAO, never()).updatePassword(anyLong(), anyString());
    }
}