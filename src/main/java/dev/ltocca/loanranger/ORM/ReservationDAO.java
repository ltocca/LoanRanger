package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.DomainModel.*;
import dev.ltocca.loanranger.ORM.DAOInterfaces.IReservationDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.*;

public class ReservationDAO implements IReservationDAO {
    private final Connection connection;

    private static final String RESERVATION_SELECT_SQL =
            "SELECT r.reservation_id, r.reservation_date, r.status, " +
                    "b.isbn, b.title, b.author, b.publication_year, b.genre, " +
                    "u.user_id, u.username, u.name, u.email, u.password, u.role " +
                    "FROM reservations r " +
                    "JOIN books b ON r.book_isbn = b.isbn " +
                    "JOIN users u ON r.member_id = u.user_id";

    public ReservationDAO() throws SQLException {
        this.connection = ConnectionManager.getInstance().getConnection();
    }

    @Override
    public Reservation createReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (book_isbn, member_id, reservation_date, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, reservation.getBook().getIsbn());
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
    public void updateReservation(Reservation reservation){
        String sql = "UPDATE reservations SET book_isbn = ?, member_id = ?, reservation_date = ?, status = ? WHERE reservation_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, reservation.getBook().getIsbn());
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
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting reservation with id " + id, e);
        }
    }

    @Override
    public List<Reservation> findMemberReservations(Member member) {
        if (member == null || member.getId() == null) {
            throw new IllegalArgumentException("Member or their ID cannot be null.");
        }
        return findMemberReservations(member.getId());
    }

    @Override
    public List<Reservation> findMemberReservations(Long memberId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = RESERVATION_SELECT_SQL + " WHERE r.member_id = ? ORDER BY r.reservation_date DESC"; // the more recent are shown first
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservations for member id " + memberId, e);
        }
        return reservations;    }

    @Override
    public List<Reservation> findBookPendingReservations(Book book) {
        if (book == null || book.getIsbn() == null) {
            throw new IllegalArgumentException("Book and its ISBN cannot be null.");
        }
        return findBookPendingReservations(book.getIsbn());
    }

    @Override
    public List<Reservation> findBookPendingReservations(String bookIsbn) {
        if (bookIsbn == null || bookIsbn.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ISBN cannot be null or empty.");
        }
        List<Reservation> reservations = new ArrayList<>();
        String sql = RESERVATION_SELECT_SQL + " WHERE r.book_isbn = ? AND r.status = 'PENDING' ORDER BY r.reservation_date ASC"; // in this case the order must show the first reservations
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, bookIsbn);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRowToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding pending reservations for book isbn " + bookIsbn, e);
        }
        return reservations;
    }

    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
        Book book = new Book(
                rs.getString("isbn"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getInt("publication_year"),
                rs.getString("genre")
        );

        Member member = new Member(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password")
        );


        Reservation reservation = new Reservation(book, member);
        reservation.setId(rs.getLong("reservation_id"));
        reservation.setReservationDate(rs.getDate("reservation_date").toLocalDate());
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("status")));

        reservation.setBook(book);
        reservation.setMember(member);

        return reservation;
    }
}
