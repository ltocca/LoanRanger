package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.Admin;
import dev.ltocca.loanranger.ORM.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAccountControllerTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private AdminAccountController adminAccountController;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = new Admin(1L, "adminUser", "Admin", "admin@example.com", "password_hash");
    }

    @Test
    void changeEmail_whenNewEmailIsValidAndAvailable_updatesSuccessfully() throws Exception {
        String newEmail = "new.admin@example.com";
        when(userDAO.getUserByEmail(newEmail)).thenReturn(Optional.empty());

        adminAccountController.changeEmail(admin, newEmail);

        // Verify that the email was updated in the database
        verify(userDAO).updateEmail(admin.getId(), newEmail);
        // Verify the local object's state is also updated
        assertThat(admin.getEmail()).isEqualTo(newEmail);
    }

    @Test
    void changeEmail_whenEmailIsAlreadyTaken_throwsIllegalArgumentException() throws Exception {
        String newEmail = "taken.email@example.com";
        // Simulate that the new email already exists in the database
        when(userDAO.getUserByEmail(newEmail)).thenReturn(Optional.of(new Admin()));

        assertThatThrownBy(() -> adminAccountController.changeEmail(admin, newEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: this email has already been used by a user");

        // Ensure no update was attempted
        verify(userDAO, never()).updateEmail(anyLong(), anyString());
    }

    @Test
    void changeEmail_whenNewEmailIsSameAsOld_throwsIllegalArgumentException() {
        String sameEmail = "admin@example.com";

        assertThatThrownBy(() -> adminAccountController.changeEmail(admin, sameEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: the new email cannot be the old one");

        verify(userDAO, never()).updateEmail(anyLong(), anyString());
    }

    @Test
    void changeEmail_whenNewEmailIsInvalidFormat_throwsIllegalArgumentException() {
        String invalidEmail = "invalid-email";

        assertThatThrownBy(() -> adminAccountController.changeEmail(admin, invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error, invalid address: it must contain an @");

        verify(userDAO, never()).updateEmail(anyLong(), anyString());
    }
}