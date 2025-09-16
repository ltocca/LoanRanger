package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.BusinessLogic.Observer.BookObserver;
import dev.ltocca.loanranger.DomainModel.*;
import dev.ltocca.loanranger.DomainModel.State.*;
import dev.ltocca.loanranger.ORM.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
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
            if (isCopyBorrowable(member, bookCopy)) return false;

            // Create loan object
            Loan loan = new Loan(bookCopy, member);
            loanDAO.createLoan(loan);

            // Update DB table and the state of the book
            bookCopy.loan(); // now it is the BusinessLogic that manages the loan() method call
            bookCopiesDAO.updateCopyStatus(bookCopy);

            Optional<Reservation> reservationOptional = reservationDAO.getReservationMemberBook(member, bookCopy);
            if (reservationOptional.isPresent() && reservationOptional.get().getStatus() == ReservationStatus.PENDING) {
                Reservation reservation = reservationOptional.get();
                reservation.setStatus(ReservationStatus.FULFILLED);
                reservationDAO.updateReservation(reservation);
                System.out.println("Book copy " + bookCopy.getCopyId() + " reserved to member " + member.getUsername() + " is now ready to be Loaned!");
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
            if (isCopyBorrowable(member, bookCopy)) return false;

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

            //System.out.println("Book copy " + copyId + " returned.");
            System.out.printf("Book copy %d returned %n", copyId);
            notifyReservations(copy.getBook().getIsbn());

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error returning book copy " + copyId, e);
        }
    }

    /** Place a reservation for a book, it will be a copy */
    public Boolean placeReservation(Member member, BookCopy bookCopy) {
        try {
            if (member == null) {
                System.err.println("No user found");
                return false;
            }
            if (bookCopy == null) {
                System.err.println("No book copy found");
                return false;
            }
            if (bookCopy.getState() instanceof UnderMaintenanceState) {
                System.err.println("Book copy " + bookCopy.getCopyId() + " is under maintenance.");
                return false;
            }
            if (bookCopy.getState() instanceof AvailableState) {
                System.out.println("Book copy " + bookCopy.getCopyId() + " is available! You can directly proceed to loan the book.");
                return false;
            }

            Reservation reservation = new Reservation(bookCopy, member);
            reservationDAO.createReservation(reservation);

            bookCopy.reserve();

            // Register as observer for book availability
            if (member instanceof BookObserver) {
                ((BookObserver) member).onBookAvailable(bookCopy.getBook());
                System.out.println("Member " + member.getUsername() + " observing availability of book " + bookCopy.getBook().getTitle());
            }

            return true; // Success

        } catch (Exception e) {
            assert member != null; // suggested by ide -- Method invocation 'getId' may produce 'NullPointerException'
            System.err.println("Error placing reservation for member " + member.getId());
            return false;
        }
    }

    public boolean placeReservation(long memberId, long copyId) {
        Optional<User> memberOpt = userDAO.getUserById(memberId);
        if (memberOpt.isEmpty()) {
            System.err.println("No member found with ID: " + memberId);
            return false;
        }

        Optional<BookCopy> bookCopyOpt = bookCopiesDAO.getCopyById(copyId);
        if (bookCopyOpt.isEmpty()) {
            System.err.println("No book copy found with ID: " + copyId);
            return false;
        }
        Member member = (Member) memberOpt.get();
        BookCopy bookCopy = bookCopyOpt.get();
        return placeReservation(member, bookCopy);
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

    public List<Reservation> getMemberReservations(Member member) {
        if (member == null || member.getId() == null) {
            throw new IllegalArgumentException("Member cannot be null.");
        }
        return reservationDAO.findMemberReservations(member.getId());
    }


    private void notifyReservations(String isbn) { // TODO: temporary implementation, to be improved
        try {
            // In a real system, we would fetch reservations and notify observers
            System.out.println("Notifying members: book with ISBN " + isbn + " is available.");
        } catch (Exception e) {
            System.err.println("Error notifying observers for ISBN " + isbn);
        }
    }

    // helper method to check if parameters ar correct
    private boolean isCopyBorrowable(Member member, BookCopy bookCopy) {
        if (member == null) {
            System.err.println("No user found");
            return false;
        }
        if (bookCopy == null) {
            System.err.println("No book copy found");
            return false;
        }

        return switch (bookCopy.getState()) { // enhanced switch proposed by Intellij
            case AvailableState s -> {
                System.out.println("Book is available, can be borrowed.");
                yield true;
            }
            case ReservedState s -> isCopyReservedByThisMember(member, bookCopy);
            case LoanedState s -> {
                System.err.printf("Book copy %s is already on loan.%n", bookCopy.getCopyId());
                yield false;
            }
            case UnderMaintenanceState s -> {
                System.err.printf("Book copy %s is under maintenance.%n", bookCopy.getCopyId());
                yield false;
            }
            default -> {
                System.err.println("Book copy " + bookCopy.getCopyId() + " is in an unknown state and cannot be borrowed.");
                yield false;
            }
        };
    }

    private boolean isCopyReservedByThisMember(Member member, BookCopy bookCopy) {
        Optional<Reservation> reservationOpt = reservationDAO.getReservationMemberBook(member, bookCopy);
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            if (reservation.getStatus() == ReservationStatus.PENDING) {
                System.out.println("Book is reserved by this member, so they are allowed to borrow it.");
                return true;
            }
        }
        System.err.println("Book copy " + bookCopy.getCopyId() + " is reserved for another member or the reservation is not pending.");
        return false;

        //TODO: maybe check if the copy has been reserved first by the member
    }
}
