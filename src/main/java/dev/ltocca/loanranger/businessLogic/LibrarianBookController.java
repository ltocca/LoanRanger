package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.ORM.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibrarianBookController {
    private final Librarian librarian;
    private final LibraryFacade libraryFacade;
    private final LoanDAO loanDAO;
    private final ReservationDAO reservationDAO;
    private final BookCopiesDAO bookCopiesDAO;
    private final UserDAO userDAO;
    private final BookCopySearchService searchService;


    public LibrarianBookController(Librarian librarian) throws SQLException {
        this.librarian = librarian;
        this.libraryFacade = new LibraryFacade();
        this.loanDAO = new LoanDAO();
        this.reservationDAO = new ReservationDAO();
        this.bookCopiesDAO = new BookCopiesDAO();
        this.userDAO = new UserDAO();
        this.searchService = new BookCopySearchService();

    }

    public void addBookCopy(String isbn) throws SQLException {
        try {
            BookCopy newCopy = libraryFacade.createBookCopy(isbn, librarian.getWorkLibrary());
            if (newCopy != null) {
                System.out.printf("New copy added with ID %d for book ISBN %s.%n", newCopy.getCopyId(), isbn);
            } else {
                System.err.println("Failed to add new book copy.");
            }
        } catch (Exception e) {
            System.err.println("Error adding book copy: " + e.getMessage());
        }
    }

    public Boolean loanBookToMember(Long memberId, Long copyId, LocalDate dueDate) {
        User user = null;
        try {
            user = userDAO.getUserById(memberId).orElse(null); // "new" syntax, maybe update all the project
        } catch (Exception e) {
            System.err.println("Error fetching member: " + e.getMessage());
            return false;
        }
        if (!(user instanceof Member member)) {
            System.err.println("The user provided (" + memberId + ") is not a user! try again");
            return false;
        }

        BookCopy copy;
        try {
            copy = bookCopiesDAO.getCopyById(copyId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error fetching book copy: " + e.getMessage());
            return false;
        }
        if (copy == null) {
            System.err.println("No copy inserted! Try again.");
            return false;
        } else if (!checkCopyBelongsToLibrary(copy)) {
            System.err.printf("This book copy with id %d is not in this Library, but it is in %s!%n", copy.getCopyId(), copy.getLibrary().getName());
            return false;
        }

        if (dueDate == null) {
            return libraryFacade.borrowBook(member, copy);
        } else {
            return libraryFacade.borrowBook(member, copy, dueDate);
        }
    }

    public boolean processReturn(Long copyId) {
        if (copyId == null) {
            System.err.println("No copy inserted! Try again.");
            return false;

        }
        BookCopy copy;
        try {
            copy = bookCopiesDAO.getCopyById(copyId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error fetching book copy: " + e.getMessage());
            return false;
        }
        if (!checkCopyBelongsToLibrary(copy)) {
            System.err.printf("This book copy with id %d is has not been borrowed in this Library, but it is in %s!%n", copyId, copy.getLibrary().getName() + " id: " + copy.getLibrary().getId());
            return false;
        }
        return libraryFacade.returnBook(copyId);
    }

    public boolean renewLoan(Long loanId, Integer days) {
        Loan loan = null;
        try {
            loan = loanDAO.getLoanById(loanId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error fetching loan: " + e.getMessage());
            return false;
        }
        if (loan == null) {
            System.err.printf("Empty loan ID!%n");
            return false;
        }
        if (!checkLoanBelongsToLibrary(loan)) {
            System.err.printf("This loan with id %d is has not been processed in this Library, but it is from %s!%n", loanId, loan.getBookCopy().getLibrary().getName() + " id: " + loan.getBookCopy().getLibrary().getId());
            return false;
        }
        if (days == null) {
            return libraryFacade.renewLoan(loan);
        } else {
            return libraryFacade.renewLoan(loan, days);
        }
    }

    public boolean putCopyUnderMaintenance(Long copyId) {
        BookCopy copy;
        try {
            copy = bookCopiesDAO.getCopyById(copyId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error fetching book copy: " + e.getMessage());
            return false;
        }
        if (copy == null || !checkCopyBelongsToLibrary(copy)) return false;

        libraryFacade.putUnderMaintenance(copy);
        return true;
    }

    public boolean removeCopyFromMaintenance(Long copyId) {
        BookCopy copy;
        try {
            copy = bookCopiesDAO.getCopyById(copyId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error fetching book copy: " + e.getMessage());
            return false;
        }
        if (copy == null) {
            System.err.println("No copy inserted! Try again.");
            return false;
        } else if (!checkCopyBelongsToLibrary(copy)) {
            System.err.printf("This book copy with id %d is not in this Library, but it is in %s!%n", copy.getCopyId(), copy.getLibrary().getName());
            return false;
        }
        libraryFacade.removeFromMaintenance(copy);
        return true;
    }

    public List<Loan> getActiveLoans() {
        try {
            return loanDAO.findActiveLoansByLibrary(librarian.getWorkLibrary());
        } catch (Exception e) {
            System.err.println("Error fetching active loans: " + e.getMessage());
            return new java.util.ArrayList<Loan>();
        }
    }

    public List<Loan> getOverdueLoans() {
        java.util.List<Loan> overdueLoans = new java.util.ArrayList<Loan>();
        try {
            for (Loan loan : loanDAO.findOverdueLoans()) {
                if (checkLoanBelongsToLibrary(loan)) overdueLoans.add(loan);
            }
        } catch (Exception e) {
            System.err.println("Error fetching overdue loans: " + e.getMessage());
        }
        return overdueLoans;
    }

    public List<Reservation> getActiveReservations() throws SQLException {
        try {
            return reservationDAO.findActiveReservationsByLibrary(librarian.getWorkLibrary().getId());
        } catch (Exception e) {
            System.err.println("Error fetching active reservations: " + e.getMessage());
            return List.of();
        }
    }

    public List<Reservation> getPastReservations() throws SQLException {
        try {
            return reservationDAO.findPastReservationsByLibrary(librarian.getWorkLibrary().getId());
        } catch (Exception e) {
            System.err.println("Error fetching past reservations: " + e.getMessage());
            return List.of();
        }
    }

    public List<Reservation> getAllReservations() throws SQLException {
        try {
            return reservationDAO.findReservationsByLibrary(librarian.getWorkLibrary().getId());
        } catch (Exception e) {
            System.err.println("Error fetching all reservations: " + e.getMessage());
            return List.of();
        }
    }

    public List<BookCopy> searchBookCopies(String query) {
        List<BookCopy> results = searchService.smartSearch(query);
        return filterCopiesByLibrary(results);
    }

    public List<BookCopy> searchBookCopies(String query, BookCopySearchService.SearchType type) {
        List<BookCopy> results = searchService.search(query, type);
        return filterCopiesByLibrary(results);
    }

    public List<BookCopy> searchAvailableBookCopies(String query) {
        List<BookCopy> results = searchService.smartSearchAvailableOnly(query);
        return filterCopiesByLibrary(results);
    }

    public List<BookCopy> searchAvailableBookCopies(String query, BookCopySearchService.SearchType baseType) {
        List<BookCopy> results = searchService.searchAvailableOnly(query, baseType);
        return filterCopiesByLibrary(results);
    }

    public List<BookCopy> searchBookCopiesElsewhere(String query) {
        // skip library filter, search in all the system
        return searchService.smartSearch(query);
    }

    public List<BookCopy> searchBookCopiesElsewhere(String query, BookCopySearchService.SearchType type) {
        return searchService.search(query, type);
    }

    public List<BookCopy> searchAvailableBookCopiesElsewhere(String query) {
        return searchService.smartSearchAvailableOnly(query);
    }

    public List<BookCopy> searchAvailableBookCopiesElsewhere(String query,BookCopySearchService.SearchType baseType) {
        return searchService.searchAvailableOnly(query, baseType);
    }

    private List<BookCopy> filterCopiesByLibrary(List<BookCopy> copies) {
        List<BookCopy> filteredList = new java.util.ArrayList<>();
        for (BookCopy copy : copies) {
            if (copy.getLibrary().getId().equals(librarian.getWorkLibrary().getId())) {
                filteredList.add(copy);
            }
        }
        return filteredList;
    }

    private boolean checkCopyBelongsToLibrary(BookCopy copy) {
        return copy.getLibrary().getId().equals(librarian.getWorkLibrary().getId());
    }

    private boolean checkLoanBelongsToLibrary(Loan loan) {
        return checkCopyBelongsToLibrary(loan.getBookCopy());
    }

    public List<Loan> getAllLoans() {
        try {
            return loanDAO.listAllLoansByLibrary(librarian.getWorkLibrary());
        } catch (Exception e) {
            System.err.println("Error fetching all loans for the library: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
