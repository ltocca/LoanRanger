package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.domainModel.State.AvailabilityState;
import dev.ltocca.loanranger.domainModel.State.AvailableState;
import dev.ltocca.loanranger.domainModel.State.LoanedState;
import dev.ltocca.loanranger.domainModel.State.ReservedState;
import dev.ltocca.loanranger.domainModel.State.UnderMaintenanceState;
import dev.ltocca.loanranger.ORM.DAOInterfaces.IReservationDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationDAO implements IReservationDAO {
    private final DataSource dataSource;
    private static final String RESERVATION_SELECT_SQL =
            "SELECT r.reservation_id, r.reservation_date, r.status AS reservation_status, " +
                    "       u.user_id, u.username, u.name AS user_name, u.email AS user_email, u.password, u.role, " +
                    "       bc.copy_id, bc.status AS book_copy_status, " +
                    "       b.isbn, b.title, b.author, b.publication_year, b.genre, " +
                    "       l.library_id, l.name AS library_name, l.address AS library_address, " +
                    "       l.phone AS library_phone, l.email AS library_email " +
                    "FROM reservations r " +
                    "JOIN users u ON r.member_id = u.user_id " +
                    "JOIN book_copies bc ON r.copy_id = bc.copy_id " +
                    "JOIN books b ON bc.isbn = b.isbn " +
                    "JOIN libraries l ON bc.library_id = l.library_id";

    @Autowired
    public ReservationDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Reservation createReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (copy_id, member_id, reservation_date, status) VALUES (?, ?, ?, ?::reservation_status)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, reservation.getBookCopy().getCopyId());
            pstmt.setLong(2, reservation.getMember().getId());
            pstmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
            pstmt.setString(4, reservation.getStatus().name());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        reservation.setId(rs.getLong("reservation_id"));
                    }
                }
            }
            return reservation;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating reservation", e);
        }
    }

    @Override
    public Optional<Reservation> getReservationById(Long id) {
        String sql = RESERVATION_SELECT_SQL + " WHERE r.reservation_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching reservation with id " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Reservation> getReservationMemberBook(Member member, BookCopy bookCopy) {
        String sql = RESERVATION_SELECT_SQL + " WHERE r.member_id = ? AND r.copy_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, member.getId());
            pstmt.setLong(2, bookCopy.getCopyId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching reservation with member id: " + member.getId() + " and book copy id: " + bookCopy.getCopyId(), e);
        }
        System.err.println("Reservation with member id: " + member.getId() + " and book copy id: " + bookCopy.getCopyId() + " not found!"); // FIXME, maybe it needs to be deleted
        return Optional.empty();
    }

    @Override
    public void updateReservation(Reservation reservation) {
        String sql = "UPDATE reservations SET copy_id = ?, member_id = ?, reservation_date = ?, status = ?::reservation_status WHERE reservation_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, reservation.getBookCopy().getCopyId());
            pstmt.setLong(2, reservation.getMember().getId());
            pstmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
            pstmt.setString(4, reservation.getStatus().name());
            pstmt.setLong(5, reservation.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating reservation with id " + reservation.getId(), e);
        }
    }

    @Override
    public void updateStatus(Reservation reservation, ReservationStatus status) {
        if (reservation == null || reservation.getId() == null) {
            throw new IllegalArgumentException("Reservation and its ID cannot be null.");
        }
        updateStatus(reservation.getId(), status);
    }

    @Override
    public void updateStatus(Long id, ReservationStatus status) {
        String sql = "UPDATE reservations SET status = ?::reservation_status WHERE reservation_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating status for reservation id " + id, e);
        }
    }

    @Override
    public void deleteReservation(Reservation reservation) {
        if (reservation == null || reservation.getId() == null) {
            throw new IllegalArgumentException("Reservation and its ID cannot be null.");
        }
        deleteReservation(reservation.getId());
    }

    @Override
    public void deleteReservation(Long id) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting reservation with id " + id, e);
        }
    }

    @Override
    public List<Reservation> findMemberReservations(Member member) {
        if (member == null || member.getId() == null) {
            throw new IllegalArgumentException("Member and its ID cannot be null.");
        }
        return findMemberReservations(member.getId());
    }

    @Override
    public List<Reservation> findMemberReservations(Long memberId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = RESERVATION_SELECT_SQL + " WHERE r.member_id = ? ORDER BY r.reservation_date DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservations for member id " + memberId, e);
        }
        return reservations;
    }

    @Override
    public List<Reservation> findCopyReservation(Long copyId){
        List<Reservation> reservations = new ArrayList<>();
        String sql = RESERVATION_SELECT_SQL + " WHERE r.copy_id = ? ORDER BY r.reservation_date DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, copyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservations for member id " + copyId, e);
        }
        return reservations;
    }

    @Override
    public List<Reservation> findCopyReservation(BookCopy bookCopy){
        if (bookCopy == null || bookCopy.getCopyId() == null) {
            throw new IllegalArgumentException("The book copy and its ID cannot be null.");
        }
        return findCopyReservation(bookCopy.getCopyId());
    }

    @Override
    public List<Reservation> findCopyWaitingReservation(Long copyId) {
        if (copyId == null) {
            throw new IllegalArgumentException("Copy ID cannot be null.");
        }
        List<Reservation> reservations = new ArrayList<>();
        String sql = RESERVATION_SELECT_SQL +
                " WHERE r.copy_id = ? AND r.status = 'WAITING'" +
                " ORDER BY r.reservation_date ASC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, copyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding 'waiting' reservations for copy ID " + copyId, e);
        }
        return reservations;
    }

    @Override
    public List<Reservation> findCopyPendingReservation(Long copyId) {
        if (copyId == null) {
            throw new IllegalArgumentException("Copy ID cannot be null.");
        }
        List<Reservation> reservations = new ArrayList<>();
        String sql = RESERVATION_SELECT_SQL +
                " WHERE r.copy_id = ? AND r.status = 'PENDING'" +
                " ORDER BY r.reservation_date ASC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, copyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding 'pending' reservations for copy ID " + copyId, e);
        }
        return reservations;
    }

    @Override
    public List<Reservation> findReservationsByLibrary(Long libraryId) {
        if (libraryId == null) {
            throw new IllegalArgumentException("libraryId ID cannot be null.");
        }
        List<Reservation> reservations = new ArrayList<>();
        String sql = RESERVATION_SELECT_SQL + " WHERE l.library_id = ? ORDER BY r.reservation_date ASC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, libraryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservations for this library ID " + libraryId, e);
        }
        return reservations;

    }

    @Override
    public List<Reservation> findActiveReservationsByLibrary(Long libraryId) {
        if (libraryId == null) {
            throw new IllegalArgumentException("libraryId cannot be null.");
        }
        List<Reservation> reservations = new ArrayList<>();
        String sql = RESERVATION_SELECT_SQL + " WHERE l.library_id = ? AND r.status IN ('PENDING', 'WAITING') ORDER BY r.reservation_date ASC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, libraryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding active reservations for library ID " + libraryId, e);
        }
        return reservations;
    }

    @Override
    public List<Reservation> findPastReservationsByLibrary(Long libraryId) {
        if (libraryId == null) {
            throw new IllegalArgumentException("libraryId cannot be null.");
        }
        List<Reservation> reservations = new ArrayList<>();
        String sql = RESERVATION_SELECT_SQL + " WHERE l.library_id = ? AND r.status IN ('CANCELLED', 'FULFILLED') ORDER BY r.reservation_date ASC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, libraryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding past reservations for library ID " + libraryId, e);
        }
        return reservations;
    }


    // used to find if there is at least another pending reservation (cancel reservation)
    @Override
    public boolean hasOtherPendingReservations(Long copyId, Long reservationIdToExclude) {
        String sql = "SELECT 1 FROM reservations WHERE copy_id = ? AND status = 'PENDING' AND reservation_id != ? LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, copyId);
            pstmt.setLong(2, reservationIdToExclude);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking for other pending reservations for copy ID " + copyId, e);
        }
    }

    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
        Member member = new Member(
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getString("user_name"),
                rs.getString("user_email"),
                rs.getString("password")
        );

        Library library = new Library(
                rs.getLong("library_id"), rs.getString("library_name"),
                rs.getString("library_address"), rs.getString("library_phone"),
                rs.getString("library_email")
        );

        Book book = new Book(
                rs.getString("isbn"), rs.getString("title"),
                rs.getString("author"), rs.getInt("publication_year"),
                rs.getString("genre")
        );

        BookCopy bookCopy = new BookCopy();
        bookCopy.setCopyId(rs.getLong("copy_id"));
        bookCopy.setBook(book);
        bookCopy.setLibrary(library);
        bookCopy.setState(mapStatusToState(rs.getString("book_copy_status")));

        Reservation reservation = new Reservation(
                rs.getLong("reservation_id"),
                bookCopy,
                member,
                rs.getDate("reservation_date").toLocalDate()
        );
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("reservation_status")));

        return reservation;
    }

    // needed to build the BookCopy object
    private AvailabilityState mapStatusToState(String status) {
        if (status == null) return new AvailableState();
        switch (BookStatus.valueOf(status.toUpperCase())) {
            case LOANED:
                return new LoanedState();
            case RESERVED:
                return new ReservedState();
            case UNDER_MAINTENANCE:
                return new UnderMaintenanceState();
            case AVAILABLE:
            default:
                return new AvailableState();
        }
    }
}