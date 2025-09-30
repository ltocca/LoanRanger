package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;
import dev.ltocca.loanranger.ORM.LoanDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberBookController {
    private final Member member;
    private final LibraryFacade facade;
    private final BookCopySearchService searchService;

    public MemberBookController(Member member, LibraryFacade facade) throws SQLException {
        this.member = member;
        this.facade = facade;
        this.searchService = new BookCopySearchService();
    }

    public void borrowBookCopy(Long copyId) throws SQLException {
        BookCopiesDAO bookCopiesDAO = new BookCopiesDAO();
        Optional<BookCopy> bookCopyOpt = bookCopiesDAO.getCopyById(copyId);

        if (bookCopyOpt.isEmpty()) {
            System.err.printf("There is no copy present with this id %s%n", copyId); // suggested by IntelliJ
            return;
        }
        boolean borrowSuccess = facade.borrowBook(this.member, bookCopyOpt.get());
        if (!borrowSuccess) {
            System.err.println("Copy borrowing failed.");
        }
    }

    public void borrowBookCopy(BookCopy copy) throws SQLException {
        if (copy == null) {
            System.err.println("BookCopy is null, cannot borrow.");
            return;
        }
        BookCopiesDAO bookCopiesDAO = new BookCopiesDAO();
        Optional<BookCopy> bookCopyOpt = bookCopiesDAO.getCopyById(copy.getCopyId());
        if (bookCopyOpt.isEmpty()) {
            System.err.printf("There is no copy present with this id %s%n", copy.getCopyId());
            return;
        }
        boolean borrowSuccess = facade.borrowBook(this.member, bookCopyOpt.get());
        if (!borrowSuccess) {
            System.err.println("Copy borrowing failed.");
        }

    }

    public void returnCopy(Long copyId) throws SQLException {
        if (copyId == null) {
            System.err.println("Copy id is null, cannot borrow.");
            return;
        }
        boolean success = facade.returnBook(copyId);
        if (!success) {
            System.out.println("Returning the book failed. Please check the copy ID.");
        }
    }

    public void reserveBookCopy(BookCopy copy) {
        if (copy == null) {
            System.err.println("BookCopy is null, cannot reserve.");
            return;
        }
        boolean success = facade.placeReservation(member.getId(), copy.getCopyId());
        if (!success) {
            System.err.println("Reservation failed for book copy ID " + copy.getCopyId());
        } else {
            System.out.println("Reservation successful for book copy ID " + copy.getCopyId());
        }
    }

    public void reserveBookCopy(Long copyId) {
        if (copyId == null) {
            System.err.println("Copy ID is null, cannot reserve.");
            return;
        }
        boolean success = facade.placeReservation(member.getId(), copyId);
        if (!success) {
            System.err.println("Reservation failed for book copy ID " + copyId);
        } else {
            System.out.println("Reservation successful for book copy ID " + copyId);
        }
    }

    public boolean cancelReservation(Long reservationId) {
        if (reservationId == null) {
            System.err.println("Reservation ID cannot be null.");
            return false;
        }
        return facade.cancelReservation(reservationId, this.member);
    }

    public List<Reservation> getActiveReservations() {
        try {
            List<Reservation> allReservations = facade.getMemberReservations(member);
            List<Reservation> activeReservations = new ArrayList<>();

            for (Reservation reservation : allReservations) {
                if (reservation.getStatus() == ReservationStatus.PENDING) {
                    activeReservations.add(reservation);
                }
            }

            return activeReservations;
        } catch (Exception e) {
            System.err.println("Error fetching active reservations: " + e.getMessage());
            return List.of();
        }
    }

    public List<Reservation> getAllReservations() {
        try {
            return facade.getMemberReservations(member);
        } catch (Exception e) {
            System.err.println("Error fetching reservation history: " + e.getMessage());
            return List.of();
        }
    }

    public List<Loan> getActiveLoans() {
        try {
            LoanDAO loanDAO = new LoanDAO();
            return loanDAO.findActiveLoansByMember(this.member);
        } catch (Exception e) {
            System.err.println("Error fetching active loans: " + e.getMessage());
            return List.of();
        }
    }

    public List<Loan> getOverdueLoans() {
        try {
            LoanDAO loanDAO = new LoanDAO();
            return loanDAO.findMemberOverdueLoans(this.member.getId());
        } catch (Exception e) {
            System.err.println("Error fetching overdue loans: " + e.getMessage());
            return List.of();
        }
    }

    public List<Loan> getAllLoans() {
        try {
            LoanDAO loanDAO = new LoanDAO();
            return loanDAO.findLoansByMember(this.member);
        } catch (Exception e) {
            System.err.println("Error fetching all loans: " + e.getMessage());
            return List.of();
        }
    }

    public List<BookCopy> searchBooksByTitle(String title) {
        try {
            return searchService.search(title, BookCopySearchService.SearchType.TITLE);
        } catch (IllegalArgumentException e) {
            System.err.println("Title search error: " + e.getMessage());
            return List.of();
        }
    }

    public List<BookCopy> searchBooksByAuthor(String author) {
        try {
            return searchService.search(author, BookCopySearchService.SearchType.AUTHOR);
        } catch (IllegalArgumentException e) {
            System.err.println("Author search error: " + e.getMessage());
            return List.of();
        }
    }

    public List<BookCopy> searchBooksByIsbn(String isbn) {
        try {
            return searchService.search(isbn, BookCopySearchService.SearchType.ISBN);
        } catch (IllegalArgumentException e) {
            System.err.println("ISBN search error: " + e.getMessage());
            return List.of();
        }
    }

    public List<BookCopy> searchBookCopyGeneric(String query) {
        try {
            return searchService.smartSearch(query);
        } catch (IllegalArgumentException e) {
            System.err.println("Error, can't find any books with this query: " + e.getMessage());
            return List.of();
        }
    }

    public List<BookCopy> searchBooksByType(String query, BookCopySearchService.SearchType type) {
        return switch (type) {
            case TITLE -> searchBooksByTitle(query);
            case AUTHOR -> searchBooksByAuthor(query);
            case ISBN -> searchBooksByIsbn(query);
            default -> searchBookCopyGeneric(query);
        };

    }

}
