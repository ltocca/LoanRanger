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
            if (parameterCheck(member, bookCopy)) return false;

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

    // helper method to check if parameters ar correct
    private boolean parameterCheck(Member member, BookCopy bookCopy) {
        if (member == null) {
            System.err.println("No user found");
            return true;
        }
        if (bookCopy == null) {
            System.err.println("No book copy found");
            return true;
        }

        if (bookCopy.getState() instanceof ReservedState) {
            System.err.println("Book copy " + bookCopy.getCopyId() + " is reserved for another member.");
            return true;
        } else if (!(bookCopy.getState() instanceof AvailableState)) {
            System.err.println("Book copy " + bookCopy.getCopyId() + " is not available.");
            return true;
        }
        return false;
    }

    public boolean borrowBook(Member member, BookCopy bookCopy, LocalDate dueDate) {
        try {
            if (parameterCheck(member, bookCopy)) return false;

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
            if (member == null) {
                System.err.println("No user found");
                return;
            }
            if (bookCopy == null) {
                System.err.println("No book copy found");
                return;
            }
            if (bookCopy.getState() instanceof UnderMaintenanceState) {
                System.err.println("Book copy " + bookCopy.getCopyId() + " is under maintenance.");
                return;
            }
            if (bookCopy.getState() instanceof AvailableState) {
                System.out.println("Book copy " + bookCopy.getCopyId() + " is available! You can directly proceed to loan the book.");
                return;
            }

            Reservation reservation = new Reservation(bookCopy, member);
            reservationDAO.createReservation(reservation);

            reservation.getBookCopy().reserve();

            // Register as observer for book availability
            if (member instanceof BookObserver) {
                System.out.println("Member " + member.getUsername() + " observing availability of book " + bookCopy.getBook().getTitle());
            }
        } catch (Exception e) {
            assert member != null; // suggested by ide -- Method invocation 'getId' may produce 'NullPointerException'
            throw new RuntimeException("Error placing reservation for member " + member.getId(), e);
        }
    }

    public void putUnderMaintenance(BookCopy bookCopy) {
        try{
            if (bookCopy.getState() instanceof ReservedState) {
                System.err.println("Book copy " + bookCopy.getCopyId() + " is reserved. The copy needs to be available");
                return;
            }
            if (bookCopy.getState() instanceof LoanedState) {
                System.err.println("Book copy " + bookCopy.getCopyId() + " is already loaned. The copy needs to be available");
                return;
            }
            if (bookCopy.getState() instanceof UnderMaintenanceState) {
                System.err.println("Book copy " + bookCopy.getCopyId() + " is already under maintenance.");
                return;
            }
            bookCopy.placeUnderMaintenance();
            bookCopiesDAO.updateCopyStatus(bookCopy);
            System.out.println("Book copy " + bookCopy.getCopyId() + " placed under maintenance.");
        } catch (Exception e) {
            throw new RuntimeException("Error placing book copy " + bookCopy.getCopyId()+ " under maintenance!", e);
        }
    }

    public void removeFromMaintenance(BookCopy bookCopy) {
        try {
            if (!(bookCopy.getState() instanceof UnderMaintenanceState)) {
                System.err.println("Book copy " + bookCopy.getCopyId() + " is not under maintenance.");
                return;
            }
            bookCopy.markAsAvailable(); // ipotetico metodo per tornare allo stato disponibile
            bookCopiesDAO.updateCopyStatus(bookCopy);
            System.out.println("Book copy " + bookCopy.getCopyId() + " removed from maintenance and is now available again.");
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error removing book copy %s from maintenance!", bookCopy.getCopyId()), e);

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
