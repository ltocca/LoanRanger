package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.BusinessLogic.Observer.BookObserver;
import dev.ltocca.loanranger.DomainModel.*;
import dev.ltocca.loanranger.DomainModel.State.*;
import dev.ltocca.loanranger.ORM.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class LibraryFacade {
    private final UserDAO userDAO;
    private final BookCopiesDAO bookCopiesDAO;
    private final LoanDAO loanDAO;
    private final ReservationDAO reservationDAO;

    public LibraryFacade() throws SQLException {
        this.userDAO = new UserDAO();
        this.bookCopiesDAO = new BookCopiesDAO();
        this.loanDAO = new LoanDAO();
        this.reservationDAO = new ReservationDAO();
    }

    /** Borrow a book copy */
    public boolean borrowBook(Member member, BookCopy bookCopy) {
        try {
            if (member == null){
                System.err.println("No user found");
                return false;
            }
            if (bookCopy ==  null) {
                System.err.println("No book copy found");
                return false;
            }

            if (bookCopy.getState() instanceof ReservedState) {
                System.err.println("Book copy " + bookCopy.getCopyId() + " is reserved for another member.");
                return false;
            } else if (!(bookCopy.getState() instanceof AvailableState)) {
                System.err.println("Book copy " + bookCopy.getCopyId() + " is not available.");
                return false;
            }


            // Create loan object
            Loan loan = new Loan(bookCopy, member);
            loanDAO.createLoan(loan);

            // Update DB table and the state of the book
            bookCopy.loan(); // now it is the BusinessLogic that manages the loan() method call
            bookCopiesDAO.updateCopyStatus(bookCopy);

            Optional<Reservation> reservationOptional = reservationDAO.getReservationMemberBook(member, bookCopy);
            if (reservationOptional.isPresent()) {
                System.out.println("Book copy " + bookCopy.getCopyId() + " reserved to member " + member.getUsername() + " is now ready to be Loaned!");
                reservationOptional.get().setStatus(ReservationStatus.FULFILLED);
            }

            System.out.println("Book copy " + bookCopy.getCopyId() + " loaned to member " + member.getUsername());
            return true;
        } catch (Exception e) {
            assert bookCopy != null;
            throw new RuntimeException("Error borrowing book copy " + bookCopy.getCopyId(), e);
        }
    }

    public boolean borrowBook(Member member, BookCopy bookCopy, LocalDate dueDate) {
        try {
            if (member == null){
                System.err.println("No user found");
                return false;
            }
            if (bookCopy ==  null) {
                System.err.println("No book copy found");
                return false;
            }

            if (bookCopy.getState() instanceof ReservedState) {
                System.err.println("Book copy " + bookCopy.getCopyId() + " is reserved for another member.");
                return false;
            } else if (!(bookCopy.getState() instanceof AvailableState)) {
                System.err.println("Book copy " + bookCopy.getCopyId() + " is not available.");
                return false;
            }

            // Create loan record
            Loan loan = new Loan(bookCopy, member, dueDate);
            loanDAO.createLoan(loan);

            // Update DB state
            bookCopy.loan();
            bookCopiesDAO.updateCopyStatus(bookCopy);

            Optional<Reservation> reservationOptional = reservationDAO.getReservationMemberBook(member, bookCopy);
            if (reservationOptional.isPresent()) {
                System.out.println("Book copy " + bookCopy.getCopyId() + " reserved to member " + member.getUsername() + " is now ready to be Loaned!");
                reservationOptional.get().setStatus(ReservationStatus.FULFILLED);
            }

            System.out.println("Book copy " + bookCopy.getCopyId() + " loaned to member " + member.getUsername());
            return true;
        } catch (Exception e) {
            assert bookCopy != null;
            throw new RuntimeException("Error borrowing book copy " + bookCopy.getCopyId(), e);
        }
    }

    /** Return a book copy */
    public boolean returnBook(Long copyId) {
        try {
            Optional<BookCopy> copyOpt = bookCopiesDAO.getCopyById(copyId);
            if (copyOpt.isEmpty()) {
                System.err.println("Book copy not found: " + copyId);
                return false;
            }
            BookCopy copy = copyOpt.get();

            Optional<Loan> loanOpt = loanDAO.getLoanByBookCopyId(copyId);
            if (loanOpt.isEmpty()) {
                System.err.println("No active loan found for copy " + copyId);
                return false;
            }
            Loan loan = loanOpt.get();

            // Update loan
            loan.setReturnDate(LocalDate.now());
            loan.endLoan();
            loanDAO.updateLoan(loan);

            // Update copy status
            bookCopiesDAO.updateCopyStatus(copy);

            System.out.println("Book copy " + copyId + " returned.");
            notifyReservations(copy.getBook().getIsbn());

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error returning book copy " + copyId, e);
        }
    }

    /** Place a reservation for a book, it will be a copy */
    public void placeReservation(Member member, BookCopy bookCopy) {
        try {
            Reservation reservation = new Reservation(bookCopy, member);
            reservationDAO.createReservation(reservation);

            //TODO(STATE:BookCopy) finish this method when fixed the DomainModel, DAO

            // Register as observer for book availability
            if (member instanceof BookObserver) {
                System.out.println("Member " + member.getUsername() + " observing availability of book " + bookCopy.getBook().getTitle());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error placing reservation for member " + member.getId(), e);
        }
    }

    public void putUnderMaintenance(BookCopy bookCopy) {
        try{
            bookCopy.placeUnderMaintenance();
            bookCopiesDAO.updateCopyStatus(bookCopy);
        } catch (Exception e) {
            throw new RuntimeException("Error placing book copy " + bookCopy.getCopyId()+ " under maintenance!", e);
        }
    }

    private void notifyReservations(String isbn) { // temporary implementation, to be improved
        try {
            // In a real system, we would fetch reservations and notify observers
            System.out.println("Notifying members: book with ISBN " + isbn + " is available.");
        } catch (Exception e) {
            System.err.println("Error notifying observers for ISBN " + isbn);
        }
    }
}
