package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.ORM.*;
import dev.ltocca.loanranger.service.BookCopySearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LibrarianBookController {
    private final LibraryFacade libraryFacade;
    private final LoanDAO loanDAO;
    private final ReservationDAO reservationDAO;
    private final BookCopiesDAO bookCopiesDAO;
    private final UserDAO userDAO;
    private final BookCopySearchService searchService;

    @Autowired
    public LibrarianBookController(LibraryFacade libraryFacade, LoanDAO loanDAO, ReservationDAO reservationDAO,
                                   BookCopiesDAO bookCopiesDAO, UserDAO userDAO, BookCopySearchService searchService) {
        this.libraryFacade = libraryFacade;
        this.loanDAO = loanDAO;
        this.reservationDAO = reservationDAO;
        this.bookCopiesDAO = bookCopiesDAO;
        this.userDAO = userDAO;
        this.searchService = searchService;
    }

    @Transactional
    public void addBookCopy(Librarian librarian, String isbn) throws SQLException {
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

    @Transactional
    public Boolean loanBookToMember(Librarian librarian, Long memberId, Long copyId, LocalDate dueDate) {
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
        } else if (!checkCopyBelongsToLibrary(librarian, copy)) {
            System.err.printf("This book copy with id %d is not in this Library, but it is in %s!%n", copy.getCopyId(), copy.getLibrary().getName());
            return false;
        }

        if (dueDate == null) {
            return libraryFacade.borrowBook(member, copy);
        } else {
            return libraryFacade.borrowBook(member, copy, dueDate);
        }
    }

    @Transactional
    public boolean processReturn(Librarian librarian, Long copyId) {
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
        if (copy == null) {
            System.err.printf("No book copy found with id %d%n", copyId);
            return false;
        }        if (!checkCopyBelongsToLibrary(librarian, copy)) {
            System.err.printf("This book copy with id %d is has not been borrowed in this Library, but it is in %s!%n", copyId, copy.getLibrary().getName() + " id: " + copy.getLibrary().getId());
            return false;
        }
        return libraryFacade.returnBook(copyId);
    }

    @Transactional
    public boolean renewLoan(Librarian librarian, Long loanId, Integer days) {
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
        if (!checkLoanBelongsToLibrary(librarian, loan)) {
            System.err.printf("This loan with id %d is has not been processed in this Library, but it is from %s!%n", loanId, loan.getBookCopy().getLibrary().getName() + " id: " + loan.getBookCopy().getLibrary().getId());
            return false;
        }
        if (days == null) {
            return libraryFacade.renewLoan(loan);
        } else {
            return libraryFacade.renewLoan(loan, days);
        }
    }

    @Transactional
    public boolean putCopyUnderMaintenance(Librarian librarian, Long copyId) {
        BookCopy copy;
        try {
            copy = bookCopiesDAO.getCopyById(copyId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error fetching book copy: " + e.getMessage());
            return false;
        }

        if (copy == null || !checkCopyBelongsToLibrary(librarian, copy)) return false;

        if (!(copy.getState() instanceof dev.ltocca.loanranger.domainModel.State.AvailableState)) {
            System.err.printf("Book copy %d is not available, so it cannot be placed under maintenance.%n", copyId);
            return false;
        }

        libraryFacade.putUnderMaintenance(copy);
        return true;
    }

    @Transactional
    public boolean removeCopyFromMaintenance(Librarian librarian, Long copyId) {
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
        } else if (!checkCopyBelongsToLibrary(librarian, copy)) {
            System.err.printf("This book copy with id %d is not in this Library, but it is in %s!%n", copy.getCopyId(), copy.getLibrary().getName());
            return false;
        }
        libraryFacade.removeFromMaintenance(copy);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Loan> getActiveLoans(Librarian librarian) {
        try {
            return loanDAO.findActiveLoansByLibrary(librarian.getWorkLibrary());
        } catch (Exception e) {
            System.err.println("Error fetching active loans: " + e.getMessage());
            return new java.util.ArrayList<Loan>();
        }
    }

    @Transactional(readOnly = true)
    public List<Loan> getOverdueLoans(Librarian librarian) {
        java.util.List<Loan> overdueLoans = new java.util.ArrayList<Loan>();
        try {
            for (Loan loan : loanDAO.findOverdueLoans()) {
                if (checkLoanBelongsToLibrary(librarian, loan)) overdueLoans.add(loan);
            }
        } catch (Exception e) {
            System.err.println("Error fetching overdue loans: " + e.getMessage());
        }
        return overdueLoans;
    }

    @Transactional(readOnly = true)
    public List<Reservation> getActiveReservations(Librarian librarian) throws SQLException {
        try {
            return reservationDAO.findActiveReservationsByLibrary(librarian.getWorkLibrary().getId());
        } catch (Exception e) {
            System.err.println("Error fetching active reservations: " + e.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<Reservation> getPastReservations(Librarian librarian) throws SQLException {
        try {
            return reservationDAO.findPastReservationsByLibrary(librarian.getWorkLibrary().getId());
        } catch (Exception e) {
            System.err.println("Error fetching past reservations: " + e.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations(Librarian librarian) throws SQLException {
        try {
            return reservationDAO.findReservationsByLibrary(librarian.getWorkLibrary().getId());
        } catch (Exception e) {
            System.err.println("Error fetching all reservations: " + e.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<BookCopy> searchBookCopies(Librarian librarian, String query) {
        List<BookCopy> results = searchService.smartSearch(query);
        return filterCopiesByLibrary(librarian, results);
    }

    @Transactional(readOnly = true)
    public List<BookCopy> searchBookCopies(Librarian librarian, String query, BookCopySearchService.SearchType type) {
        List<BookCopy> results = searchService.search(query, type);
        return filterCopiesByLibrary(librarian, results);
    }

    @Transactional(readOnly = true)
    public List<BookCopy> searchAvailableBookCopies(Librarian librarian, String query) {
        List<BookCopy> results = searchService.smartSearchAvailableOnly(query);
        return filterCopiesByLibrary(librarian, results);
    }

    @Transactional(readOnly = true)
    public List<BookCopy> searchAvailableBookCopies(Librarian librarian, String query, BookCopySearchService.SearchType baseType) {
        List<BookCopy> results = searchService.searchAvailableOnly(query, baseType);
        return filterCopiesByLibrary(librarian, results);
    }

    @Transactional(readOnly = true)
    public List<BookCopy> searchBookCopiesElsewhere(String query) {
        // skip library filter, search in all the system
        return searchService.smartSearch(query);
    }

    @Transactional(readOnly = true)
    public List<BookCopy> searchBookCopiesElsewhere(String query, BookCopySearchService.SearchType type) {
        return searchService.search(query, type);
    }

    @Transactional(readOnly = true)
    public List<BookCopy> searchAvailableBookCopiesElsewhere(String query) {
        return searchService.smartSearchAvailableOnly(query);
    }

    @Transactional(readOnly = true)
    public List<BookCopy> searchAvailableBookCopiesElsewhere(String query,BookCopySearchService.SearchType baseType) {
        return searchService.searchAvailableOnly(query, baseType);
    }

    private List<BookCopy> filterCopiesByLibrary(Librarian librarian, List<BookCopy> copies) {
        List<BookCopy> filteredList = new java.util.ArrayList<>();
        for (BookCopy copy : copies) {
            if (copy.getLibrary().getId().equals(librarian.getWorkLibrary().getId())) {
                filteredList.add(copy);
            }
        }
        return filteredList;
    }

    private boolean checkCopyBelongsToLibrary(Librarian librarian, BookCopy copy) {
        return copy.getLibrary().getId().equals(librarian.getWorkLibrary().getId());
    }

    private boolean checkLoanBelongsToLibrary(Librarian librarian, Loan loan) {
        return checkCopyBelongsToLibrary(librarian, loan.getBookCopy());
    }

    public List<Loan> getAllLoans(Librarian librarian) {
        try {
            return loanDAO.listAllLoansByLibrary(librarian.getWorkLibrary());
        } catch (Exception e) {
            System.err.println("Error fetching all loans for the library: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
