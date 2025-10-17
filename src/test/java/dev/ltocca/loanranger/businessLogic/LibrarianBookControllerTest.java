package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.domainModel.State.AvailableState;
import dev.ltocca.loanranger.domainModel.State.LoanedState;
import dev.ltocca.loanranger.domainModel.State.UnderMaintenanceState;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;
import dev.ltocca.loanranger.ORM.LoanDAO;
import dev.ltocca.loanranger.ORM.ReservationDAO;
import dev.ltocca.loanranger.ORM.UserDAO;
import dev.ltocca.loanranger.service.BookCopySearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibrarianBookControllerTest {

    @Mock
    private LibraryFacade libraryFacade;

    @Mock
    private LoanDAO loanDAO;

    @Mock
    private ReservationDAO reservationDAO;

    @Mock
    private BookCopiesDAO bookCopiesDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private BookCopySearchService searchService;

    @InjectMocks
    private LibrarianBookController librarianBookController;

    private Librarian librarian;
    private Library library;
    private Library otherLibrary;
    private Member member;
    private Book book;
    private BookCopy bookCopy;
    private BookCopy otherLibraryCopy;
    private Loan loan;

    @BeforeEach
    void setUp() {
        library = new Library(1L, "Main Library", "123 Main St", "555-0100", "main@library.com");
        otherLibrary = new Library(2L, "Other Library", "456 Other St", "555-0200", "other@library.com");
        librarian = new Librarian(1L, "libUser", "Librarian User", "lib@example.com", "hashed_password", library);
        member = new Member(2L, "memberUser", "Member User", "member@example.com", "hashed_password");
        book = new Book("978-0123456789", "Test Book", "Test Author");
        bookCopy = new BookCopy(book, library, new AvailableState());
        bookCopy.setCopyId(100L);
        otherLibraryCopy = new BookCopy(book, otherLibrary, new AvailableState());
        otherLibraryCopy.setCopyId(200L);
        loan = new Loan(bookCopy, member);
        loan.setId(50L);
    }

    @Test
    void addBookCopy_forExistingBook_createsCopyInLibrariansLibrary() throws Exception {
        when(libraryFacade.createBookCopy("978-0123456789", library)).thenReturn(bookCopy);

        librarianBookController.addBookCopy(librarian, "978-0123456789");

        verify(libraryFacade).createBookCopy("978-0123456789", library);
    }

    @Test
    void addBookCopy_forNonExistentBook_failsToCreateCopy() throws Exception {
        when(libraryFacade.createBookCopy("999-0123456789", library)).thenReturn(null);

        librarianBookController.addBookCopy(librarian, "999-0123456789");

        verify(libraryFacade).createBookCopy("999-0123456789", library);
    }

    @Test
    void loanBookToMember_onBookInOwnLibrary_succeeds() {
        when(userDAO.getUserById(2L)).thenReturn(Optional.of(member));
        when(bookCopiesDAO.getCopyById(100L)).thenReturn(Optional.of(bookCopy));
        when(libraryFacade.borrowBook(member, bookCopy)).thenReturn(true);

        Boolean result = librarianBookController.loanBookToMember(librarian, 2L, 100L, null);

        assertThat(result).isTrue();
        verify(libraryFacade).borrowBook(member, bookCopy);
    }

    @Test
    void loanBookToMember_onBookInAnotherLibrary_fails() {
        when(userDAO.getUserById(2L)).thenReturn(Optional.of(member));
        when(bookCopiesDAO.getCopyById(200L)).thenReturn(Optional.of(otherLibraryCopy));

        Boolean result = librarianBookController.loanBookToMember(librarian, 2L, 200L, null);

        assertThat(result).isFalse();
        verify(libraryFacade, never()).borrowBook(any(), any());
    }

    @Test
    void loanBookToMember_withInvalidMemberId_fails() {
        when(userDAO.getUserById(999L)).thenReturn(Optional.empty());

        Boolean result = librarianBookController.loanBookToMember(librarian, 999L, 100L, null);

        assertThat(result).isFalse();
        verify(libraryFacade, never()).borrowBook(any(), any());
    }

    @Test
    void loanBookToMember_withInvalidCopyId_fails() {
        when(userDAO.getUserById(2L)).thenReturn(Optional.of(member));
        when(bookCopiesDAO.getCopyById(999L)).thenReturn(Optional.empty());

        Boolean result = librarianBookController.loanBookToMember(librarian, 2L, 999L, null);

        assertThat(result).isFalse();
        verify(libraryFacade, never()).borrowBook(any(), any());
    }

    @Test
    void processReturn_onBookInOwnLibrary_succeeds() {
        when(bookCopiesDAO.getCopyById(100L)).thenReturn(Optional.of(bookCopy));
        when(libraryFacade.returnBook(100L)).thenReturn(true);

        Boolean result = librarianBookController.processReturn(librarian, 100L);

        assertThat(result).isTrue();
        verify(libraryFacade).returnBook(100L);
    }

    @Test
    void processReturn_onBookInAnotherLibrary_fails() {
        when(bookCopiesDAO.getCopyById(200L)).thenReturn(Optional.of(otherLibraryCopy));

        Boolean result = librarianBookController.processReturn(librarian, 200L);

        assertThat(result).isFalse();
        verify(libraryFacade, never()).returnBook(anyLong());
    }

    @Test
    void processReturn_withInvalidCopyId_fails() {
        when(bookCopiesDAO.getCopyById(999L)).thenReturn(Optional.empty());

        Boolean result = librarianBookController.processReturn(librarian, 999L);

        assertThat(result).isFalse();
        verify(libraryFacade, never()).returnBook(anyLong());
    }

    @Test
    void renewLoan_onBookInOwnLibrary_succeeds() {
        when(loanDAO.getLoanById(50L)).thenReturn(Optional.of(loan));
        when(libraryFacade.renewLoan(loan)).thenReturn(true);

        Boolean result = librarianBookController.renewLoan(librarian, 50L, null);

        assertThat(result).isTrue();
        verify(libraryFacade).renewLoan(loan);
    }

    @Test
    void renewLoan_onBookInAnotherLibrary_fails() {
        Loan otherLibraryLoan = new Loan(otherLibraryCopy, member);
        otherLibraryLoan.setId(60L);
        when(loanDAO.getLoanById(60L)).thenReturn(Optional.of(otherLibraryLoan));

        Boolean result = librarianBookController.renewLoan(librarian, 60L, null);

        assertThat(result).isFalse();
        verify(libraryFacade, never()).renewLoan(any());
    }

    @Test
    void renewLoan_withInvalidLoanId_fails() {
        when(loanDAO.getLoanById(999L)).thenReturn(Optional.empty());

        Boolean result = librarianBookController.renewLoan(librarian, 999L, null);

        assertThat(result).isFalse();
        verify(libraryFacade, never()).renewLoan(any());
    }

    @Test
    void putCopyUnderMaintenance_onBookInOwnLibrary_succeeds() {
        when(bookCopiesDAO.getCopyById(100L)).thenReturn(Optional.of(bookCopy));
        doNothing().when(libraryFacade).putUnderMaintenance(bookCopy);

        Boolean result = librarianBookController.putCopyUnderMaintenance(librarian, 100L);

        assertThat(result).isTrue();
        verify(libraryFacade).putUnderMaintenance(bookCopy);
    }

    @Test
    void putCopyUnderMaintenance_onBookInAnotherLibrary_fails() {
        when(bookCopiesDAO.getCopyById(200L)).thenReturn(Optional.of(otherLibraryCopy));

        Boolean result = librarianBookController.putCopyUnderMaintenance(librarian, 200L);

        assertThat(result).isFalse();
        verify(libraryFacade, never()).putUnderMaintenance(any());
    }

    @Test
    void putCopyUnderMaintenance_whenCopyNotInCorrectState_fails() {
        bookCopy.loan(); // Change state to Loaned
        when(bookCopiesDAO.getCopyById(100L)).thenReturn(Optional.of(bookCopy));

        Boolean result = librarianBookController.putCopyUnderMaintenance(librarian, 100L);

        assertThat(result).isFalse();
        verify(libraryFacade, never()).putUnderMaintenance(any());
    }

    @Test
    void removeCopyFromMaintenance_onBookInOwnLibrary_succeeds() {
        bookCopy.placeUnderMaintenance(); // Change state to Under Maintenance
        when(bookCopiesDAO.getCopyById(100L)).thenReturn(Optional.of(bookCopy));
        doNothing().when(libraryFacade).removeFromMaintenance(bookCopy);


        Boolean result = librarianBookController.removeCopyFromMaintenance(librarian, 100L);

        assertThat(result).isTrue();
        verify(libraryFacade).removeFromMaintenance(bookCopy);
    }

    @Test
    void removeCopyFromMaintenance_onBookInAnotherLibrary_fails() {
        when(bookCopiesDAO.getCopyById(200L)).thenReturn(Optional.of(otherLibraryCopy));

        Boolean result = librarianBookController.removeCopyFromMaintenance(librarian, 200L);

        assertThat(result).isFalse();
        verify(libraryFacade, never()).removeFromMaintenance(any());
    }

    @Test
    void searchBookCopies_returnsOnlyLocalResults() {
        List<BookCopy> searchResults = List.of(bookCopy, otherLibraryCopy);
        when(searchService.smartSearch("test")).thenReturn(searchResults);

        List<BookCopy> result = librarianBookController.searchBookCopies(librarian, "test");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCopyId()).isEqualTo(100L);
    }

    @Test
    void searchBookCopiesElsewhere_returnsGlobalResults() {
        List<BookCopy> searchResults = List.of(bookCopy, otherLibraryCopy);
        when(searchService.smartSearch("test")).thenReturn(searchResults);

        List<BookCopy> result = librarianBookController.searchBookCopiesElsewhere("test");

        assertThat(result).hasSize(2);
    }

    @Test
    void getActiveLoans_returnsLoansFromOwnLibrary() {
        List<Loan> loans = List.of(loan);
        when(loanDAO.findActiveLoansByLibrary(library)).thenReturn(loans);

        List<Loan> result = librarianBookController.getActiveLoans(librarian);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(50L);
    }

    @Test
    void getOverdueLoans_returnsOverdueLoansFromOwnLibrary() {
        List<Loan> overdueLoans = List.of(loan);
        when(loanDAO.findOverdueLoans()).thenReturn(overdueLoans);

        List<Loan> result = librarianBookController.getOverdueLoans(librarian);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(50L);
    }

    @Test
    void getAllLoans_returnsAllLoansFromOwnLibrary() {
        List<Loan> loans = List.of(loan);
        when(loanDAO.listAllLoansByLibrary(library)).thenReturn(loans);

        List<Loan> result = librarianBookController.getAllLoans(librarian);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(50L);
    }
}