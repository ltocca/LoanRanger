package dev.ltocca.loanranger.ORM;
import dev.ltocca.loanranger.DomainModel.*;
import dev.ltocca.loanranger.ORM.DAOInterfaces.IAttendanceDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
public class AttendanceDAO implements IAttendanceDAO {
    private final Connection connection;
    public AttendanceDAO() throws SQLException {
        this.connection = ConnectionManager.getInstance().getConnection();
    }
    @Override
    public void addAttendance(Event event, Member member) {
        String sql = "INSERT INTO attendances (event_id, member_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, event.getId());
            pstmt.setLong(2, member.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding attendance: " + e.getMessage());
            throw new RuntimeException("Failed to add attendance", e);
        }
    }

    @Override
    public Attendance createAttendance(Event event, Member member) {
        String sql = "INSERT INTO attendances (event_id, member_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, event.getId());
            pstmt.setLong(2, member.getId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long attendanceId = rs.getLong(1);
                        return new Attendance(attendanceId, event, member);
                    }
                }
            }
            throw new RuntimeException("Failed to create attendance record for event: " + event.getId() + " and member: " + member.getId());
        } catch (SQLException e) {
            System.err.println("Error creating attendance: " + e.getMessage());
            throw new RuntimeException("Failed to create attendance", e);
        }
    }

    @Override
    public void deleteAttendance(Event event, Member member) {
        deleteAttendance(event.getId(), member.getId());
    }
    @Override
    public void deleteAttendance(Long eventId, Long memberId) {
        String sql = "DELETE FROM attendances WHERE event_id = ? AND member_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, eventId);
            pstmt.setLong(2, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting attendance: " + e.getMessage());
            throw new RuntimeException("Failed to delete attendance", e);
        }
    }
    @Override
    public boolean isMemberAttending(Event event, Member member) {
        String sql = "SELECT COUNT(*) FROM attendances WHERE event_id = ? AND member_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, event.getId());
            pstmt.setLong(2, member.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking attendance: " + e.getMessage());
            throw new RuntimeException("Failed to check attendance", e);
        }
        return false;
    }
    @Override
    public List<Member> findEventAttendees(Event event) {
        List<Member> attendees = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.name, u.email, u.password, u.role " +
                "FROM users u JOIN attendances a ON u.user_id = a.member_id " +
                "WHERE a.event_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, event.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attendees.add(mapRowToMember(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding event attendees: " + e.getMessage());
            throw new RuntimeException("Failed to find attendees for event id " + event.getId(), e);
        }
        return attendees;
    }
    @Override
    public List<Event> findMemberParticipation(Member member) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.event_id, e.title, e.description, e.event_type, e.event_date, e.location, e.max_capacity, " +
                "l.library_id, l.name AS library_name, l.address, l.phone, l.email " +
                "FROM events e " +
                "JOIN libraries l ON e.library_id = l.library_id " +
                "JOIN attendances a ON e.event_id = a.event_id " +
                "WHERE a.member_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, member.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding member participation: " + e.getMessage());
            throw new RuntimeException("Failed to find events for member id " + member.getId(), e);
        }
        return events;
    }

    private Member mapRowToMember(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getLong("user_id"));
        member.setUsername(rs.getString("username"));
        member.setName(rs.getString("name"));
        member.setEmail(rs.getString("email"));
        member.setPassword(rs.getString("password"));
        member.setRole(UserRole.valueOf(rs.getString("role")));
        return member;
    }
    private Event mapRowToEvent(ResultSet rs) throws SQLException {
        Library library = new Library(
                rs.getLong("library_id"),
                rs.getString("library_name"),
                rs.getString("address"),
                rs.getString("phone"),
                rs.getString("email")
        );
        Event event = new Event();
        event.setId(rs.getLong("event_id"));
        event.setTitle(rs.getString("title"));
        event.setDescription(rs.getString("description"));
        event.setEventType(EventType.valueOf(rs.getString("event_type")));
        event.setEventDate(rs.getTimestamp("event_date").toLocalDateTime());
        event.setLocation(rs.getString("location"));
        event.setMaxCapacity(rs.getInt("max_capacity"));
        event.setLibrary(library);
        return event;
    }
}