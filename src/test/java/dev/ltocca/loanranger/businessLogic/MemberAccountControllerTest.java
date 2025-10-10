package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.ORM.LoanDAO;
import dev.ltocca.loanranger.ORM.ReservationDAO;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberAccountControllerTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private LoanDAO loanDAO;

    @Mock
    private ReservationDAO reservationDAO;

    @InjectMocks
    private MemberAccountController memberAccountController;

    private Member member;
    private Loan activeLoan;
    private Reservation pendingReservation;

    @BeforeEach
    void setUp() {
        member = new Member(1L, "memberUser", "Member User", "member@example.com", "hashed_password");

        Library library = new Library(1L, "Main Library", "123 Main St", "555-0100", "main@library.com");
        Book book = new Book("978-0123456789", "Test Book", "Test Author");
        BookCopy bookCopy = new BookCopy(book, library, new dev.ltocca.loanranger.domainModel.State.LoanedState());
        bookCopy.setCopyId(100L);

        activeLoan = new Loan(bookCopy, member);
        activeLoan.setId(50L);

        pendingReservation = new Reservation(bookCopy, member);
        pendingReservation.setId(60L);
        pendingReservation.setStatus(ReservationStatus.PENDING);
    }

    @Test
    void changeUsername_withValidNewUsername_updatesSuccessfully() {
        String newUsername = "newMemberUser";
        when(userDAO.findUserByUsername(newUsername)).thenReturn(java.util.Optional.empty());

        memberAccountController.changeUsername(member, newUsername);

        verify(userDAO).updateUsername(member.getId(), newUsername);
        assertThat(member.getUsername()).isEqualTo(newUsername);
    }

    @Test
    void changeUsername_withNullUsername_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> memberAccountController.changeUsername(member, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: the new username cannot be null or empty");

        verify(userDAO, never()).updateUsername(anyLong(), anyString());
    }

    @Test
    void changeUsername_withSameUsername_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> memberAccountController.changeUsername(member, "memberUser"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: the new username cannot be the old one");

        verify(userDAO, never()).updateUsername(anyLong(), anyString());
    }

    @Test
    void changeUsername_withTakenUsername_throwsIllegalArgumentException() {
        String takenUsername = "takenUser";
        when(userDAO.findUserByUsername(takenUsername)).thenReturn(java.util.Optional.of(new Member()));

        assertThatThrownBy(() -> memberAccountController.changeUsername(member, takenUsername))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: the username already exists");

        verify(userDAO, never()).updateUsername(anyLong(), anyString());
    }

    @Test
    void changeEmail_withValidNewEmail_updatesSuccessfully() {
        String newEmail = "new.member@example.com";
        when(userDAO.getUserByEmail(newEmail)).thenReturn(java.util.Optional.empty());

        memberAccountController.changeEmail(member, newEmail);

        verify(userDAO).updateEmail(member.getId(), newEmail);
        assertThat(member.getEmail()).isEqualTo(newEmail);
    }

    @Test
    void changeEmail_withNullEmail_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> memberAccountController.changeEmail(member, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: the new email cannot be null or empty");

        verify(userDAO, never()).updateEmail(anyLong(), anyString());
    }

    @Test
    void changeEmail_withInvalidFormat_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> memberAccountController.changeEmail(member, "invalid-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error, invalid address: it must contain an @");

        verify(userDAO, never()).updateEmail(anyLong(), anyString());
    }

    @Test
    void changeEmail_withSameEmail_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> memberAccountController.changeEmail(member, "member@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: the new email cannot be the old one");

        verify(userDAO, never()).updateEmail(anyLong(), anyString());
    }

    @Test
    void changeEmail_withTakenEmail_throwsIllegalArgumentException() {
        String takenEmail = "taken@example.com";
        when(userDAO.getUserByEmail(takenEmail)).thenReturn(java.util.Optional.of(new Member()));

        assertThatThrownBy(() -> memberAccountController.changeEmail(member, takenEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: this email has already been used by a user");

        verify(userDAO, never()).updateEmail(anyLong(), anyString());
    }

    @Test
    void changePassword_withValidCredentials_updatesSuccessfully() {
        String currentPassword = "currentPassword";
        String newPassword = "newPassword123";

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check(currentPassword, "hashed_password")).thenReturn(true);
            mockedHasher.when(() -> PasswordHasher.hash(newPassword)).thenReturn("new_hashed_password");

            memberAccountController.changePassword(member, currentPassword, newPassword);

            verify(userDAO).updatePassword(member.getId(), "new_hashed_password");
        }
    }

    @Test
    void changePassword_withIncorrectCurrentPassword_throwsIllegalArgumentException() {
        String wrongPassword = "wrongPassword";
        String newPassword = "newPassword123";

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check(wrongPassword, "hashed_password")).thenReturn(false);

            assertThatThrownBy(() -> memberAccountController.changePassword(member, wrongPassword, newPassword))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Error: inserted incorrect current password");

            verify(userDAO, never()).updatePassword(anyLong(), anyString());
        }
    }

    @Test
    void changePassword_withShortNewPassword_throwsIllegalArgumentException() {
        String currentPassword = "currentPassword";
        String shortPassword = "short";

        assertThatThrownBy(() -> memberAccountController.changePassword(member, currentPassword, shortPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: the new password cannot be null or empty, at least 8 characters");

        verify(userDAO, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void changePassword_withSamePassword_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> memberAccountController.changePassword(member, "hashed_password", member.getPassword()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: the new password cannot be the old one");

        verify(userDAO, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void deleteAccount_withCorrectPasswordAndNoDependencies_succeeds() {
        String password = "correctPassword";

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check(password, "hashed_password")).thenReturn(true);
            when(loanDAO.findActiveLoansByMember(member)).thenReturn(List.of());
            when(reservationDAO.findMemberReservations(member)).thenReturn(List.of());

            memberAccountController.deleteAccount(member, password);

            verify(userDAO).deleteUser(member.getId());
        }
    }

    @Test
    void deleteAccount_withIncorrectPassword_fails() {
        String wrongPassword = "wrongPassword";

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check(wrongPassword, "hashed_password")).thenReturn(false);

            memberAccountController.deleteAccount(member, wrongPassword);

            verify(userDAO, never()).deleteUser(anyLong());
        }
    }

    @Test
    void deleteAccount_withActiveLoans_fails() {
        String password = "correctPassword";

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check(password, "hashed_password")).thenReturn(true);
            when(loanDAO.findActiveLoansByMember(member)).thenReturn(List.of(activeLoan));

            memberAccountController.deleteAccount(member, password);

            verify(userDAO, never()).deleteUser(anyLong());
        }
    }

    @Test
    void deleteAccount_withPendingReservations_fails() {
        String password = "correctPassword";

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check(password, "hashed_password")).thenReturn(true);
            when(loanDAO.findActiveLoansByMember(member)).thenReturn(List.of());
            when(reservationDAO.findMemberReservations(member)).thenReturn(List.of(pendingReservation));

            memberAccountController.deleteAccount(member, password);

            verify(userDAO, never()).deleteUser(anyLong());
        }
    }

    @Test
    void deleteAccount_withCancelledReservations_succeeds() {
        String password = "correctPassword";
        pendingReservation.setStatus(ReservationStatus.CANCELLED);

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.check(password, "hashed_password")).thenReturn(true);
            when(loanDAO.findActiveLoansByMember(member)).thenReturn(List.of());
            when(reservationDAO.findMemberReservations(member)).thenReturn(List.of(pendingReservation));

            memberAccountController.deleteAccount(member, password);

            verify(userDAO).deleteUser(member.getId());
        }
    }
}