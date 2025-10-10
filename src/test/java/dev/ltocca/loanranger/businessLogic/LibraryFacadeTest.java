/*
package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.domainModel.State.AvailableState;
import dev.ltocca.loanranger.domainModel.State.LoanedState;
import dev.ltocca.loanranger.ORM.*;
import dev.ltocca.loanranger.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibraryFacadeTest {

    @Mock
    private UserDAO userDAO;
    @Mock
    private BookCopiesDAO bookCopiesDAO;
    @Mock
    private LoanDAO loanDAO;
    @Mock
    private ReservationDAO reservationDAO;
    @Mock
    private BookDAO bookDAO;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private LibraryFacade facade;

    private Library library;
    private Book book;
    private BookCopy copy;
    private Member member;

    @BeforeEach
    void setUp() {
        userDAO = mock(UserDAO.class);
        bookCopiesDAO = mock(BookCopiesDAO.class);
        loanDAO = mock(LoanDAO.class);
        reservationDAO = mock(ReservationDAO.class);
        bookDAO = mock(BookDAO.class);
        emailService = mock(EmailService.class);

        facade = new LibraryFacade(userDAO, bookCopiesDAO, loanDAO, reservationDAO, bookDAO, emailService);

        library = new Library(1L, "Main", "addr", "000", "main@lib");
        book = new Book("9780000000001", "Title", "Author", 2020, "Genre");
        copy = new BookCopy(book, library, new AvailableState());
        copy.setCopyId(10L);
        member = new Member(2L, "user", "User Name", "u@ex.com", "hashedpwd");
    }

    @Test
    void borrowBook_availableCopy_createsLoanAndUpdatesState() {
        when(loanDAO.createLoan(any(Loan.class))).thenReturn(null);
        doNothing().when(bookCopiesDAO).updateCopyStatus(any());
        when(reservationDAO.getReservationMemberBook(member, copy)).thenReturn(Optional.empty());

        boolean ok = facade.borrowBook(member, copy);

        assertThat(ok).isTrue();
        // BookCopy should now be loaned
        assertThat(copy.getState()).isInstanceOf(LoanedState.class);
        verify(loanDAO, times(1)).createLoan(any(Loan.class));
        verify(bookCopiesDAO, times(1)).updateCopyStatus(copy);
    }

    @Test
    void borrowBook_nullMember_returnsFalse() {
        boolean ok = facade.borrowBook(null, copy);
        assertThat(ok).isFalse();
    }

    @Test
    void borrowBook_reservedByOtherMember_isNotAllowed() {
        // simulate a reservation for different member and PENDING status
        Reservation r = new Reservation(copy, new Member(99L, "other", "Other", "o@e", "p"));
        r.setStatus(ReservationStatus.PENDING);
        when(reservationDAO.getReservationMemberBook(member, copy)).thenReturn(Optional.empty());
        // put copy into ReservedState manually
        copy.reserve(); // transitions state
        boolean ok = facade.borrowBook(member, copy);
        assertThat(ok).isFalse();
    }

    @Test
    void returnBook_noCopy_returnsFalse() {
        when(bookCopiesDAO.getCopyById(999L)).thenReturn(Optional.empty());
        boolean ok = facade.returnBook(999L);
        assertThat(ok).isFalse();
    }

    @Test
    void returnBook_activeLoan_processesWaitingListAndUpdatesLoan() {
        // Setup: copy loaned and loanDAO returns loan for copy id
        copy.loan();
        when(bookCopiesDAO.getCopyById(copy.getCopyId())).thenReturn(Optional.of(copy));
        Loan loan = new Loan(copy, member);
        loan.setId(55L);
        when(loanDAO.getLoanByBookCopyId(copy.getCopyId())).thenReturn(Optional.of(loan));
        when(reservationDAO.findCopyWaitingReservation(copy.getCopyId())).thenReturn(java.util.List.of());
        doNothing().when(loanDAO).updateLoan(any(Loan.class));
        doNothing().when(bookCopiesDAO).updateCopyStatus(any());

        boolean ok = facade.returnBook(copy.getCopyId());

        assertThat(ok).isTrue();
        assertThat(loan.getReturnDate()).isNotNull();
        verify(loanDAO).updateLoan(loan);
        verify(bookCopiesDAO).updateCopyStatus(copy);
    }

    @Test
    void placeReservation_whenCopyAvailable_marksReserved() {
        // copy available
        when(reservationDAO.createReservation(any())).thenReturn(null);
        doNothing().when(bookCopiesDAO).updateCopyStatus(any());

        boolean ok = facade.placeReservation(member, copy);

        assertThat(ok).isTrue();
        assertThat(copy.getState()).isInstanceOf(dev.ltocca.loanranger.domainModel.State.ReservedState.class);
        verify(reservationDAO).createReservation(any(Reservation.class));
        verify(bookCopiesDAO).updateCopyStatus(copy);
    }
}
*/
package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.domainModel.State.AvailableState;
import dev.ltocca.loanranger.domainModel.State.LoanedState;
import dev.ltocca.loanranger.domainModel.State.ReservedState;
import dev.ltocca.loanranger.ORM.*;
import dev.ltocca.loanranger.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryFacadeTest {

    @Mock private UserDAO userDAO;
    @Mock private BookCopiesDAO bookCopiesDAO;
    @Mock private LoanDAO loanDAO;
    @Mock private ReservationDAO reservationDAO;
    @Mock private BookDAO bookDAO;
    @Mock private EmailService emailService;

    @InjectMocks
    private LibraryFacade libraryFacade;

    private Library library;
    private Book book;
    private BookCopy availableCopy;
    private Member member;
    private Member waitingMember;

    @BeforeEach
    void setUp() {
        library = new Library(1L, "Main Library", "123 Books St", "555-BOOK", "main@lib.com");
        book = new Book("978-0132350884", "Clean Code", "Robert C. Martin");
        availableCopy = new BookCopy(book, library, new AvailableState());
        availableCopy.setCopyId(101L);
        member = new Member(1L, "john.smith", "John Smith", "john.smith@email.com", "hashed_pwd");
        waitingMember = new Member(2L, "jane.doe", "Jane Doe", "jane.doe@email.com", "hashed_pwd");
    }

    @Test
    void borrowBook_whenCopyIsAvailable_createsLoanAndUpdatesState() {
        // Given
        when(reservationDAO.getReservationMemberBook(member, availableCopy)).thenReturn(Optional.empty());

        // When
        boolean success = libraryFacade.borrowBook(member, availableCopy);

        // Then
        assertThat(success).isTrue();
        assertThat(availableCopy.getState()).isInstanceOf(LoanedState.class);

        verify(loanDAO).createLoan(any(Loan.class));
        verify(bookCopiesDAO).updateCopyStatus(availableCopy);
    }

    @Test
    void returnBook_whenLoanExists_updatesLoanAndProcessesWaitingList() {
        // Given
        availableCopy.loan(); // Manually set state to Loaned
        Loan activeLoan = new Loan(availableCopy, member);
        activeLoan.setId(50L);

        Reservation waitingReservation = new Reservation(availableCopy, waitingMember);
        waitingReservation.setId(99L);
        waitingReservation.setStatus(ReservationStatus.WAITING);

        when(bookCopiesDAO.getCopyById(101L)).thenReturn(Optional.of(availableCopy));
        when(loanDAO.getLoanByBookCopyId(101L)).thenReturn(Optional.of(activeLoan));
        when(reservationDAO.findCopyWaitingReservation(101L)).thenReturn(List.of(waitingReservation));

        // When
        boolean success = libraryFacade.returnBook(101L);

        // Then
        assertThat(success).isTrue();

        // Verify loan is updated with a return date
        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
        verify(loanDAO).updateLoan(loanCaptor.capture());
        assertThat(loanCaptor.getValue().getReturnDate()).isNotNull();

        // Verify the waiting reservation is now PENDING
        verify(reservationDAO).updateStatus(99L, ReservationStatus.PENDING);

        // Verify the book copy is now RESERVED for the next person
        assertThat(availableCopy.getState()).isInstanceOf(ReservedState.class);
        verify(bookCopiesDAO).updateCopyStatus(availableCopy);

        // Verify an email is sent to the waiting member
        verify(emailService).sendEmail(eq("jane.doe@email.com"), anyString(), anyString());
    }

    @Test
    void returnBook_withNoWaitingList_makesCopyAvailable() {
        // Given
        availableCopy.loan();
        Loan activeLoan = new Loan(availableCopy, member);
        when(bookCopiesDAO.getCopyById(101L)).thenReturn(Optional.of(availableCopy));
        when(loanDAO.getLoanByBookCopyId(101L)).thenReturn(Optional.of(activeLoan));
        when(reservationDAO.findCopyWaitingReservation(101L)).thenReturn(Collections.emptyList());

        // When
        libraryFacade.returnBook(101L);

        // Then
        assertThat(availableCopy.getState()).isInstanceOf(AvailableState.class);
        verify(bookCopiesDAO).updateCopyStatus(availableCopy);
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}