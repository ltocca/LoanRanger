package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.domainModel.State.AvailableState;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;
import dev.ltocca.loanranger.ORM.LoanDAO;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberBookControllerTest {

    @Mock
    private LibraryFacade facade;

    @Mock
    private BookCopySearchService searchService;

    @Mock
    private BookCopiesDAO bookCopiesDAO;

    @Mock
    private LoanDAO loanDAO;

    @InjectMocks
    private MemberBookController memberBookController;

    private Member member;
    private BookCopy bookCopy;
    private Loan loan;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        member = new Member(1L, "memberUser", "Member User", "member@example.com", "hashed_password");

        Library library = new Library(1L, "Main Library", "123 Main St", "555-0100", "main@library.com");
        Book book = new Book("978-0123456789", "Test Book", "Test Author");

        bookCopy = new BookCopy(book, library, new AvailableState());
        bookCopy.setCopyId(100L);

        loan = new Loan(bookCopy, member);
        loan.setId(50L);

        reservation = new Reservation(bookCopy, member);
        reservation.setId(60L);
        reservation.setStatus(ReservationStatus.PENDING);
    }

    @Test
    void borrowBookCopy_withValidId_callsFacade() throws Exception {
        when(bookCopiesDAO.getCopyById(100L)).thenReturn(Optional.of(bookCopy));
        when(facade.borrowBook(member, bookCopy)).thenReturn(true);

        memberBookController.borrowBookCopy(member, 100L);

        verify(facade).borrowBook(member, bookCopy);
    }

    @Test
    void borrowBookCopy_withInvalidId_doesNotCallFacade() throws Exception {
        when(bookCopiesDAO.getCopyById(999L)).thenReturn(Optional.empty());

        memberBookController.borrowBookCopy(member, 999L);

        verify(facade, never()).borrowBook(any(), any());
    }

    @Test
    void borrowBookCopy_withBookCopyObject_callsFacade() throws Exception {
        when(bookCopiesDAO.getCopyById(100L)).thenReturn(Optional.of(bookCopy));
        when(facade.borrowBook(member, bookCopy)).thenReturn(true);

        memberBookController.borrowBookCopy(member, bookCopy);

        verify(facade).borrowBook(member, bookCopy);
    }

    @Test
    void borrowBookCopy_withNullBookCopy_doesNotCallFacade() throws Exception {
        memberBookController.borrowBookCopy(member, (BookCopy) null);

        verify(facade, never()).borrowBook(any(), any());
    }

    @Test
    void returnCopy_callsFacade() throws Exception {
        when(facade.returnBook(100L)).thenReturn(true);

        memberBookController.returnCopy(100L);

        verify(facade).returnBook(100L);
    }

    @Test
    void returnCopy_withNullCopyId_doesNotCallFacade() throws Exception {
        memberBookController.returnCopy(null);

        verify(facade, never()).returnBook(anyLong());
    }

    @Test
    void reserveBookCopy_withValidCopyId_callsFacade() {
        when(facade.placeReservation(1L, 100L)).thenReturn(true);

        memberBookController.reserveBookCopy(member, 100L);

        verify(facade).placeReservation(1L, 100L);
    }

    @Test
    void reserveBookCopy_withBookCopyObject_callsFacade() {
        when(facade.placeReservation(1L, 100L)).thenReturn(true);

        memberBookController.reserveBookCopy(member, bookCopy);

        verify(facade).placeReservation(1L, 100L);
    }

    @Test
    void reserveBookCopy_withNullCopyId_doesNotCallFacade() {
        memberBookController.reserveBookCopy(member, (Long) null);

        verify(facade, never()).placeReservation(anyLong(), anyLong());
    }

    @Test
    void cancelReservation_callsFacade() {
        when(facade.cancelReservation(60L, member)).thenReturn(true);

        boolean result = memberBookController.cancelReservation(member, 60L);

        verify(facade).cancelReservation(60L, member);
    }

    @Test
    void cancelReservation_withNullReservationId_doesNotCallFacade() {
        boolean result = memberBookController.cancelReservation(member, null);

        verify(facade, never()).cancelReservation(anyLong(), any());
    }

    @Test
    void getActiveReservations_returnsPendingReservations() {
        Reservation fulfilledReservation = new Reservation(bookCopy, member);
        fulfilledReservation.setId(61L);
        fulfilledReservation.setStatus(ReservationStatus.FULFILLED);

        List<Reservation> allReservations = List.of(reservation, fulfilledReservation);
        when(facade.getMemberReservations(member)).thenReturn(allReservations);

        List<Reservation> result = memberBookController.getActiveReservations(member);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void getAllReservations_callsFacade() {
        List<Reservation> reservations = List.of(reservation);
        when(facade.getMemberReservations(member)).thenReturn(reservations);

        List<Reservation> result = memberBookController.getAllReservations(member);

        verify(facade).getMemberReservations(member);
        assertThat(result).isEqualTo(reservations);
    }

    @Test
    void getActiveLoans_callsLoanDAO() {
        List<Loan> loans = List.of(loan);
        when(loanDAO.findActiveLoansByMember(member)).thenReturn(loans);

        List<Loan> result = memberBookController.getActiveLoans(member);

        verify(loanDAO).findActiveLoansByMember(member);
        assertThat(result).isEqualTo(loans);
    }

    @Test
    void getOverdueLoans_callsLoanDAO() {
        List<Loan> loans = List.of(loan);
        when(loanDAO.findMemberOverdueLoans(member.getId())).thenReturn(loans);

        List<Loan> result = memberBookController.getOverdueLoans(member);

        verify(loanDAO).findMemberOverdueLoans(member.getId());
        assertThat(result).isEqualTo(loans);
    }

    @Test
    void getAllLoans_callsLoanDAO() {
        List<Loan> loans = List.of(loan);
        when(loanDAO.findLoansByMember(member)).thenReturn(loans);

        List<Loan> result = memberBookController.getAllLoans(member);

        verify(loanDAO).findLoansByMember(member);
        assertThat(result).isEqualTo(loans);
    }

    @Test
    void searchBooksByTitle_callsSearchService() {
        List<BookCopy> searchResults = List.of(bookCopy);
        when(searchService.search("Test", BookCopySearchService.SearchType.TITLE)).thenReturn(searchResults);

        List<BookCopy> result = memberBookController.searchBooksByTitle("Test");

        verify(searchService).search("Test", BookCopySearchService.SearchType.TITLE);
        assertThat(result).isEqualTo(searchResults);
    }

    @Test
    void searchBooksByAuthor_callsSearchService() {
        List<BookCopy> searchResults = List.of(bookCopy);
        when(searchService.search("Author", BookCopySearchService.SearchType.AUTHOR)).thenReturn(searchResults);

        List<BookCopy> result = memberBookController.searchBooksByAuthor("Author");

        verify(searchService).search("Author", BookCopySearchService.SearchType.AUTHOR);
        assertThat(result).isEqualTo(searchResults);
    }

    @Test
    void searchBooksByIsbn_callsSearchService() {
        List<BookCopy> searchResults = List.of(bookCopy);
        when(searchService.search("978-0123456789", BookCopySearchService.SearchType.ISBN)).thenReturn(searchResults);

        List<BookCopy> result = memberBookController.searchBooksByIsbn("978-0123456789");

        verify(searchService).search("978-0123456789", BookCopySearchService.SearchType.ISBN);
        assertThat(result).isEqualTo(searchResults);
    }

    @Test
    void searchBookCopyGeneric_callsSearchService() {
        List<BookCopy> searchResults = List.of(bookCopy);
        when(searchService.smartSearch("test query")).thenReturn(searchResults);

        List<BookCopy> result = memberBookController.searchBookCopyGeneric("test query");

        verify(searchService).smartSearch("test query");
        assertThat(result).isEqualTo(searchResults);
    }

    @Test
    void searchBooksByType_withTitle_callsTitleSearch() {
        List<BookCopy> searchResults = List.of(bookCopy);
        when(searchService.search("Test", BookCopySearchService.SearchType.TITLE)).thenReturn(searchResults);

        List<BookCopy> result = memberBookController.searchBooksByType("Test", BookCopySearchService.SearchType.TITLE);

        verify(searchService).search("Test", BookCopySearchService.SearchType.TITLE);
        assertThat(result).isEqualTo(searchResults);
    }
}