package dev.ltocca.loanranger.ORM;
import dev.ltocca.loanranger.DomainModel.Event;
import dev.ltocca.loanranger.DomainModel.EventType;
import dev.ltocca.loanranger.DomainModel.Library;
import dev.ltocca.loanranger.ORM.DAOInterfaces.IEventDAO;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class EventDAO implements IEventDAO {
    private static final String EVENT_SELECT_SQL =
            "SELECT e.event_id, e.title, e.description, e.event_type, e.event_date, e.location, e.max_capacity, " +
                    "l.library_id, l.name AS library_name, l.address, l.phone, l.email " +
                    "FROM events e " +
                    "JOIN libraries l ON e.library_id = l.library_id";
    private final Connection connection;
    public EventDAO() throws SQLException {
        this.connection = ConnectionManager.getInstance().getConnection();
    }
    @Override
    public Event createEvent(Event event) {
        String sql = "INSERT INTO events (library_id, title, description, event_type, event_date, location, max_capacity) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, event.getLibrary().getId());
            pstmt.setString(2, event.getTitle());
            pstmt.setString(3, event.getDescription());
            pstmt.setString(4, event.getEventType().name());
            pstmt.setTimestamp(5, Timestamp.valueOf(event.getEventDate()));
            pstmt.setString(6, event.getLocation());
            pstmt.setInt(7, event.getMaxCapacity());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        event.setId(rs.getLong("event_id"));
                    }
                }
            }
            return event;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating event: " + e.getMessage(), e);
        }
    }
    @Override
    public Optional<Event> getEventById(Long id) {
        String sql = EVENT_SELECT_SQL + " WHERE e.event_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching event with id " + id, e);
        }
        return Optional.empty();
    }
    @Override
    public void updateEvent(Event event) {
        String sql = "UPDATE events SET library_id = ?, title = ?, description = ?, event_type = ?, event_date = ?, location = ?, max_capacity = ? WHERE event_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, event.getLibrary().getId());
            pstmt.setString(2, event.getTitle());
            pstmt.setString(3, event.getDescription());
            pstmt.setString(4, event.getEventType().name());
            pstmt.setTimestamp(5, Timestamp.valueOf(event.getEventDate()));
            pstmt.setString(6, event.getLocation());
            pstmt.setInt(7, event.getMaxCapacity());
            pstmt.setLong(8, event.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating event with id " + event.getId(), e);
        }
    }
    @Override
    public void deleteEvent(Long id) {
        String sql = "DELETE FROM events WHERE event_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting event with id " + id, e);
        }
    }
    @Override
    public void editLibrary(Event event, Library library) {
        String sql = "UPDATE events SET library_id = ? WHERE event_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, library.getId());
            pstmt.setLong(2, event.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error editing library for event id " + event.getId(), e);
        }
    }
    @Override
    public List<Event> findEventsByLibrary(Library library) {
        List<Event> events = new ArrayList<>();
        String sql = EVENT_SELECT_SQL + " WHERE e.library_id = ? ORDER BY e.event_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, library.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding events for library id " + library.getId(), e);
        }
        return events;
    }
    @Override
    public List<Event> findUpcomingEvents() {
        List<Event> events = new ArrayList<>();
        String sql = EVENT_SELECT_SQL + " WHERE e.event_date >= CURRENT_TIMESTAMP ORDER BY e.event_date ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                events.add(mapRowToEvent(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding upcoming events", e);
        }
        return events;
    }
    @Override
    public List<Event> findEventsByTitle(String title) {
        List<Event> events = new ArrayList<>();
        String sql = EVENT_SELECT_SQL + " WHERE e.title ILIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + title + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding events by title", e);
        }
        return events;
    }

    @Override
    public List<Event> findEventsByEventType(EventType eventType) {
        List<Event> events = new ArrayList<>();
        String sql = EVENT_SELECT_SQL + " WHERE e.event_type = ? AND e.event_date >= CURRENT_TIMESTAMP ORDER BY e.event_date ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, eventType.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding events by type: " + eventType, e);
        }
        return events;
    }

    @Override
    public List<Event> findEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Event> events = new ArrayList<>();
        String sql = EVENT_SELECT_SQL + " WHERE e.event_date >= ? AND e.event_date < ? ORDER BY e.event_date ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding events by date range", e);
        }
        return events;
    }

    @Override
    public List<Event> findEventsByDateRange(LocalDateTime start) {
        List<Event> events = new ArrayList<>();
        String sql = EVENT_SELECT_SQL + " WHERE e.event_date >= ? ORDER BY e.event_date ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding events by start date", e);
        }
        return events;
    }

    @Override
    public List<Event> findEventsByLibraryName(String libraryName) {
        List<Event> events = new ArrayList<>();
        String sql = EVENT_SELECT_SQL + " WHERE l.name ILIKE ? AND e.event_date >= CURRENT_TIMESTAMP ORDER BY e.event_date ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + libraryName + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding events by library name", e);
        }
        return events;
    }
    @Override
    public List<Event> findUpcomingEvents(int limit) {
        List<Event> events = new ArrayList<>();
        String sql = EVENT_SELECT_SQL + " WHERE e.event_date >= CURRENT_TIMESTAMP ORDER BY e.event_date ASC LIMIT ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding upcoming events with limit", e);
        }
        return events;
    }

    @Override
    public List<Event> findEventsByDescription(String description) {
        List<Event> events = new ArrayList<>();
        String sql = EVENT_SELECT_SQL + " WHERE e.description ILIKE ? AND e.event_date >= CURRENT_TIMESTAMP ORDER BY e.event_date ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + description + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding events by description", e);
        }
        return events;
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