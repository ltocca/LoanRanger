package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.DomainModel.*;

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
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, event.getId());
            pstmt.setLong(2, member.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding attendance: " + e.getMessage());
            throw new RuntimeException("Failed to add attendance", e);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    @Override
    public void deleteAttendance(Event event, Member member) {
        deleteAttendance(event.getId(), member.getId());
    }

    @Override
    public void deleteAttendance(Long eventId, Long memberId) {
        String sql = "DELETE FROM attendances WHERE event_id = ? AND member_id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, eventId);
            pstmt.setLong(2, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting attendance: " + e.getMessage());
            throw new RuntimeException("Failed to delete attendance", e);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isMemberAttending(Event event, Member member) {
        String sql = "SELECT COUNT(*) FROM attendances WHERE event_id = ? AND member_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isAttending = false;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, event.getId());
            pstmt.setLong(2, member.getId());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                isAttending = rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking attendance: " + e.getMessage());
            throw new RuntimeException("Failed to check attendance", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        return isAttending;
    }

    @Override
    public List<Member> findEventAttendees(Event event) {
        List<Member> attendees = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.name, u.email, u.password, u.role " +
                "FROM users u JOIN attendances a ON u.user_id = a.member_id " +
                "WHERE a.event_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, event.getId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                attendees.add(mapRowToMember(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding event attendees: " + e.getMessage());
            throw new RuntimeException("Failed to find attendees for event id " + event.getId(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        return attendees;
    }

    @Override
    public List<Event> findMemberPartecipation(Member member) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.event_id, e.title, e.description, e.event_type, e.event_date, e.location, e.max_capacity, " +
                "l.library_id, l.name AS library_name, l.address, l.phone, l.email " +
                "FROM events e " +
                "JOIN libraries l ON e.library_id = l.library_id " +
                "JOIN attendances a ON e.event_id = a.event_id " +
                "WHERE a.member_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, member.getId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                events.add(mapRowToEvent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding member participation: " + e.getMessage());
            throw new RuntimeException("Failed to find events for member id " + member.getId(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
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