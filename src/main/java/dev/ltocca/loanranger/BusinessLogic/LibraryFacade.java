package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.BusinessLogic.Observer.BookCopyObserver;
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
    private final BookDAO bookDAO;

    public LibraryFacade() throws SQLException {
        this.userDAO = new UserDAO();
        this.bookCopiesDAO = new BookCopiesDAO();
        this.loanDAO = new LoanDAO();
        this.reservationDAO = new ReservationDAO();
        try {
            this.bookDAO = new BookDAO(); // Initialize BookDAO
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }

    public BookCopy createBookCopy(String isbn, Library library) {
        try {
            Optional<Book> bookOpt = bookDAO.getBookByIsbn(isbn);
            if (bookOpt.isEmpty()) {
                System.err.println("Cannot create copy: Book with ISBN " + isbn + " not found.");
                return null;
            }
            Book book = bookOpt.get();
            BookCopy newCopy = new BookCopy(book, library, new AvailableState());
            return bookCopiesDAO.createCopy(newCopy);
        } catch (Exception e) {
            System.err.println("Error creating book copy: " + e.getMessage());
            return null;
        }
    }

    /** Borrow a book copy */
    public boolean borrowBook(Member member, BookCopy bookCopy) {
        try {
            if (!isCopyBorrowable(member, bookCopy)) return false;

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
                System.out.printf("Book copy %d reserved to member %s marked as fulfilled.%n",
                        bookCopy.getCopyId(), member.getUsername());
            }

            System.out.printf("Book copy %d loaned to member %s%n", bookCopy.getCopyId(), member.getUsername());
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error borrowing book copy " + (bookCopy != null ? bookCopy.getCopyId() : "unknown"), e);
        }
    }

    public boolean borrowBook(Member member, BookCopy bookCopy, LocalDate dueDate) {
        try {
            if (!isCopyBorrowable(member, bookCopy)) return false;

            Loan loan = new Loan(bookCopy, member, dueDate);
            loanDAO.createLoan(loan);

            bookCopy.loan();
            bookCopiesDAO.updateCopyStatus(bookCopy);

            Optional<Reservation> reservationOptional = reservationDAO.getReservationMemberBook(member, bookCopy);
            if (reservationOptional.isPresent() && reservationOptional.get().getStatus() == ReservationStatus.PENDING) {
                Reservation reservation = reservationOptional.get();
                reservation.setStatus(ReservationStatus.FULFILLED);
                reservationDAO.updateReservation(reservation);
                System.out.printf("Book copy %d reserved to member %s marked as fulfilled.%n",
                        bookCopy.getCopyId(), member.getUsername());
            }

            System.out.printf("Book copy %d loaned to member %s%n", bookCopy.getCopyId(), member.getUsername());
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error borrowing book copy " + (bookCopy != null ? bookCopy.getCopyId() : "unknown"), e);
        }
    }

    public boolean renewLoan(Loan loan, int days) {
        try {
            LocalDate newDueDate = loan.getDueDate().plusDays(days);
            loan.setDueDate(newDueDate);
            loanDAO.updateDueDate(loan.getId(), newDueDate);
            return true;
        } catch (Exception e) {
            System.err.println("Error renewing loan: " + e.getMessage());
            return false;
        }
    }

    public boolean renewLoan(Loan loan) {
        return renewLoan(loan, 30);
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

            if (!(loan.getBookCopy().getState() instanceof LoanedState)) {
                System.out.println("This copy isn't Loaned");
                return false;
            }

            copy.returnCopy(); // FIXME: THIS METHOD IS CALLED TWO TIMES INSTEAD OF ONE

            loan.setReturnDate(LocalDate.now());
            //loan.endLoan();
            loanDAO.updateLoan(loan);

            bookCopiesDAO.updateCopyStatus(copy);

            System.out.printf("Book copy %d returned%n", copyId);
            notifyReservations(copy.getBook().getIsbn()); // FIXME: maybe is not required anymore

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error returning book copy " + copyId, e);
        }
    }

    /** Place a reservation for a book copy */
    public boolean placeReservation(Member member, BookCopy bookCopy) {
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
                System.err.printf("Book copy %d is under maintenance Copy now observed.%n", bookCopy.getCopyId());
                bookCopy.addObserver(member);
                System.out.printf("Member %s is now watching copy %d of '%s'.%n", member.getUsername(), bookCopy.getCopyId(), bookCopy.getBook().getTitle());
                return false;
            }
            if (bookCopy.getState() instanceof ReservedState) {
                System.err.printf("Book copy %d is already reserved.%n", bookCopy.getCopyId());
                bookCopy.addObserver(member);
                System.out.printf("Member %s is now watching copy %d of '%s'.%n", member.getUsername(), bookCopy.getCopyId(), bookCopy.getBook().getTitle());
                return false;
            }
            if (bookCopy.getState() instanceof LoanedState) {
                System.err.printf("Book copy %d is loaned right now.%n", bookCopy.getCopyId());
                bookCopy.addObserver(member); // Add observer
                System.out.printf("Member %s is now watching copy %d of '%s'.%n", member.getUsername(), bookCopy.getCopyId(), bookCopy.getBook().getTitle());
                return false;
            }

            Reservation reservation = new Reservation(bookCopy, member);
            reservationDAO.createReservation(reservation);

            bookCopy.reserve();
            bookCopiesDAO.updateCopyStatus(bookCopy);

            if (member instanceof BookCopyObserver observer) {
                //observer.onBookCopyAvailable(bookCopy);
                System.out.printf("Member %s observing availability of book %s%n",
                        member.getUsername(), bookCopy.getBook().getTitle());
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error placing reservation for member " +
                    (member != null ? member.getId() : "unknown"));
            return false;
        }
    }

    public boolean cancelReservation(Long reservationId, Member member) {
        try {
            Optional<Reservation> resOpt = reservationDAO.getReservationById(reservationId);
            if (resOpt.isEmpty()) {
                System.err.println("Reservation with ID " + reservationId + " not found.");
                return false;
            }

            Reservation reservation = resOpt.get();

            if (!reservation.getMember().getId().equals(member.getId())) {
                System.err.println("Error: You do not have permission to cancel this reservation.");
                return false;
            }

            if (reservation.getStatus() != ReservationStatus.PENDING) {
                System.err.println("Error: This reservation cannot be cancelled (Status: " + reservation.getStatus() + ").");
                return false;
            }

            // Update the reservation status to CANCELLED
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationDAO.updateStatus(reservationId, ReservationStatus.CANCELLED);

            // Update the associated BookCopy's state if it's no longer reserved
            BookCopy bookCopy = reservation.getBookCopy();
            List<Reservation> otherReservations = reservationDAO.findCopyReservation(bookCopy.getCopyId());

            // Check if there are any other PENDING reservations for this copy
            boolean isStillReserved = otherReservations.stream()
                    .anyMatch(r -> r.getStatus() == ReservationStatus.PENDING && !r.getId().equals(reservationId));

            if (!isStillReserved && bookCopy.getState() instanceof ReservedState) {
                bookCopy.markAsAvailable();
                bookCopiesDAO.updateCopyStatus(bookCopy);
                System.out.println("Book copy " + bookCopy.getCopyId() + " is now available.");
            }

            System.out.println("Reservation " + reservationId + " cancelled successfully.");
            return true;

        } catch (Exception e) {
            System.err.println("An error occurred while cancelling the reservation: " + e.getMessage());
            return false;
        }
    }


    public boolean placeReservation(long memberId, long copyId) {
        Optional<User> memberOpt = userDAO.getUserById(memberId);
        if (memberOpt.isEmpty() || !(memberOpt.get() instanceof Member member)) {
            System.err.println("No member found with ID: " + memberId);
            return false;
        }

        Optional<BookCopy> bookCopyOpt = bookCopiesDAO.getCopyById(copyId);
        if (bookCopyOpt.isEmpty()) {
            System.err.println("No book copy found with ID: " + copyId);
            return false;
        }

        return placeReservation(member, bookCopyOpt.get());
    }


    public void putUnderMaintenance(BookCopy bookCopy) {
        try {
            if (bookCopy.getState() instanceof ReservedState) {
                System.err.printf("Book copy %d is reserved. Must be available.%n", bookCopy.getCopyId());
                return;
            }
            if (bookCopy.getState() instanceof LoanedState) {
                System.err.printf("Book copy %d is already loaned. Must be available.%n", bookCopy.getCopyId());
                return;
            }
            if (bookCopy.getState() instanceof UnderMaintenanceState) {
                System.err.printf("Book copy %d is already under maintenance.%n", bookCopy.getCopyId());
                return;
            }
            bookCopy.placeUnderMaintenance();
            bookCopiesDAO.updateCopyStatus(bookCopy);
            System.out.printf("Book copy %d placed under maintenance.%n", bookCopy.getCopyId());
        } catch (Exception e) {
            throw new RuntimeException("Error placing book copy " + bookCopy.getCopyId() + " under maintenance", e);
        }
    }

    public void removeFromMaintenance(BookCopy bookCopy) {
        try {
            if (!(bookCopy.getState() instanceof UnderMaintenanceState)) {
                System.err.printf("Book copy %d is not under maintenance.%n", bookCopy.getCopyId());
                return;
            }
            bookCopy.markAsAvailable(); // ensure implemented in BookCopy
            bookCopiesDAO.updateCopyStatus(bookCopy);
            System.out.printf("Book copy %d removed from maintenance and available.%n", bookCopy.getCopyId());
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

    private void notifyReservations(String isbn) {
        try {
            System.out.println("Notifying members: book with ISBN " + isbn + " is available.");
        } catch (Exception e) {
            System.err.println("Error notifying observers for ISBN " + isbn);
        }
    }

    private boolean isCopyBorrowable(Member member, BookCopy bookCopy) {
        if (member == null) {
            System.err.println("No user found");
            return false;
        }
        if (bookCopy == null) {
            System.err.println("No book copy found");
            return false;
        }

        return switch (bookCopy.getState()) {
            case AvailableState s -> true;
            case ReservedState s -> isCopyReservedByThisMember(member, bookCopy);
            case LoanedState s -> {
                System.err.printf("Book copy %d is already on loan.%n", bookCopy.getCopyId());
                yield false;
            }
            case UnderMaintenanceState s -> {
                System.err.printf("Book copy %d is under maintenance.%n", bookCopy.getCopyId());
                yield false;
            }
            default -> {
                System.err.printf("Book copy %d is in an unknown state.%n", bookCopy.getCopyId());
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
    }
}
