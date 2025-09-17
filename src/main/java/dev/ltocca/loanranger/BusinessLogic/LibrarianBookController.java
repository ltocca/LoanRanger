package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.DomainModel.*;
import dev.ltocca.loanranger.ORM.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public boolean loanBookToMember(Long memberId, Long copyId, LocalDate dueDate) {
        User user = null;
        try {
            user = userDAO.getUserById(memberId).orElse(null); // "new" syntax, maybe update all the project
        } catch (Exception e) {
            System.err.println("Error fetching member: " + e.getMessage());
            return false;
        }
        if (!(user instanceof Member member)) return false;

        BookCopy copy;
        try {
            copy = bookCopiesDAO.getCopyById(copyId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error fetching book copy: " + e.getMessage());
            return false;
        }
        if (copy == null || !checkCopyBelongsToLibrary(copy)) return false;

        if (dueDate == null) {
            return libraryFacade.borrowBook(member, copy);
        } else {
            return libraryFacade.borrowBook(member, copy, dueDate);
        }
    }

    public boolean processReturn(Long copyId) {
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
        if (loan == null || !checkLoanBelongsToLibrary(loan)) return false;

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
        if (copy == null || !checkCopyBelongsToLibrary(copy)) return false;

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

    public List<Reservation> getActiveReservations() {
        java.util.List<Reservation> result = new java.util.ArrayList<Reservation>();
        try {
            for (BookCopy copy : bookCopiesDAO.findLibraryCopies(librarian.getWorkLibrary())) {
                for (Reservation r : reservationDAO.findCopyReservation(copy)) {
                    if (r.getStatus() == ReservationStatus.PENDING) {
                        result.add(r);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching reservations: " + e.getMessage());
        }
        return result;
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
        List<BookCopy> filteredList = new java.util.ArrayList<BookCopy>();
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
}
