package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.DomainModel.*;
import dev.ltocca.loanranger.ORM.AttendanceDAO;
import dev.ltocca.loanranger.ORM.EventDAO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class LibrarianEventController {
    private final Librarian librarian;
    private final EventDAO eventDAO;
    private final AttendanceDAO attendanceDAO;
    private final EmailService emailService;

    public LibrarianEventController(Librarian librarian) throws SQLException {
        this.librarian = librarian;
        this.eventDAO = new EventDAO();
        this.attendanceDAO = new AttendanceDAO();
        this.emailService = new EmailService(); // Simple email service implementation
    }

    public Event createEvent(String title, String description, EventType eventType,
                             LocalDateTime eventDate, String location, int maxCapacity) {
        try {
            validateEventParameters(title, eventDate, maxCapacity);

            if (eventDate.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Event date must be in the future");
            }

            Event newEvent = new Event();
            newEvent.setLibrary(librarian.getWorkLibrary());
            newEvent.setTitle(title);
            newEvent.setDescription(description);
            newEvent.setEventType(eventType);
            newEvent.setEventDate(eventDate);
            newEvent.setLocation(location);
            newEvent.setMaxCapacity(maxCapacity);

            Event createdEvent = eventDAO.createEvent(newEvent);
            System.out.println("Event created successfully: " + createdEvent.getId());

            return createdEvent;
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error creating event: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Error creating event: " + e.getMessage());
            throw new RuntimeException("Failed to create event", e);
        }
    }

    public boolean updateEvent(Long eventId, String description, EventType eventType,
                               LocalDateTime eventDate, String location, Integer maxCapacity) {
        try {
            Optional<Event> eventOpt = eventDAO.getEventById(eventId);
            if (eventOpt.isEmpty()) {
                System.err.println("Event not found: " + eventId);
                return false;
            }

            Event event = eventOpt.get();

            // Verify the librarian owns this event
            if (!event.getLibrary().getId().equals(librarian.getWorkLibrary().getId())) {
                System.err.println("Cannot update event: Not authorized");
                return false;
            }

            if (event.getEventDate().isBefore(LocalDateTime.now())) {
                System.err.println("Cannot update event: Event has already started");
                return false;
            }

            if (eventDate != null) {
                validateEventDate(eventDate);
                if (eventDate.isBefore(LocalDateTime.now())) {
                    throw new IllegalArgumentException("Event date must be in the future");
                }
            }
            if (maxCapacity != null) {
                validateCapacity(maxCapacity);

                List<Member> attendees = attendanceDAO.findEventAttendees(event);
                if (maxCapacity < attendees.size()) {
                    throw new IllegalArgumentException(
                            "Cannot reduce capacity below current attendance (" + attendees.size() + " attendees)");
                }
            }

            if (description != null) event.setDescription(description);
            if (eventType != null) event.setEventType(eventType);
            if (eventDate != null) event.setEventDate(eventDate);
            if (location != null) event.setLocation(location);
            if (maxCapacity != null) event.setMaxCapacity(maxCapacity);

            eventDAO.updateEvent(event);
            System.out.println("Event updated successfully: " + eventId);

            notifyAttendees(event, "Event Updated",
                    "The event '" + event.getTitle() + "' has been updated. Please check the new details.");

            return true;
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error updating event: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error updating event: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelEvent(Long eventId, String reason) {
        try {
            Optional<Event> eventOpt = eventDAO.getEventById(eventId);
            if (eventOpt.isEmpty()) {
                System.err.println("Event not found: " + eventId);
                return false;
            }

            Event event = eventOpt.get();

            if (!event.getLibrary().getId().equals(librarian.getWorkLibrary().getId())) {
                System.err.println("Cannot cancel event: Not authorized");
                return false;
            }

            // Archive the event instead of deleting (soft delete)
            event.setTitle("[CANCELLED] " + event.getTitle());
            eventDAO.updateEvent(event);

            System.out.println("Event cancelled successfully: " + eventId);

            String message = "The event '" + event.getTitle().replace("[CANCELLED] ", "") +
                    "' has been cancelled. Reason: " + reason;
            notifyAttendees(event, "Event Cancelled", message);

            return true;
        } catch (Exception e) {
            System.err.println("Error cancelling event: " + e.getMessage());
            return false;
        }
    }

    public List<Event> getLibraryEvents() {
        try {
            return eventDAO.findEventsByLibrary(librarian.getWorkLibrary());
        } catch (Exception e) {
            System.err.println("Error fetching library events: " + e.getMessage());
            return List.of();
        }
    }

    public List<Event> getUpcomingLibraryEvents() {
        try {
            return eventDAO.findUpcomingEventsByLibrary(librarian.getWorkLibrary().getId());
        } catch (Exception e) {
            System.err.println("Error fetching upcoming library events: " + e.getMessage());
            return List.of();
        }
    }

    public List<Event> searchAllEvents(String query) {
        try {
            EventSearchService eventSearchService = new EventSearchService();
            return eventSearchService.smartSearch(query);
        } catch (Exception e) {
            System.err.println("Error searching events: " + e.getMessage());
            return List.of();
        }
    }

    public List<Member> getEventAttendees(Long eventId) {
        try {
            Optional<Event> eventOpt = eventDAO.getEventById(eventId);
            if (eventOpt.isEmpty()) {
                System.err.println("Event not found: " + eventId);
                return List.of();
            }

            Event event = eventOpt.get();

            if (!event.getLibrary().getId().equals(librarian.getWorkLibrary().getId())) {
                System.err.println("Cannot view attendees: Not authorized");
                return List.of();
            }

            return attendanceDAO.findEventAttendees(event);
        } catch (Exception e) {
            System.err.println("Error fetching event attendees: " + e.getMessage());
            return List.of();
        }
    }

    public boolean addAttendee(Long eventId, Long memberId) {
        try {
            Optional<Event> eventOpt = eventDAO.getEventById(eventId);
            if (eventOpt.isEmpty()) {
                System.err.println("Event not found: " + eventId);
                return false;
            }

            Event event = eventOpt.get();

            if (!event.getLibrary().getId().equals(librarian.getWorkLibrary().getId())) {
                System.err.println("Cannot add attendee: Not authorized");
                return false;
            }

            // Check if event is at capacity
            List<Member> attendees = attendanceDAO.findEventAttendees(event);
            if (attendees.size() >= event.getMaxCapacity()) {
                System.err.println("Cannot add attendee: Event is at full capacity");
                return false;
            }

            Member member = new Member();
            member.setId(memberId);

            attendanceDAO.addAttendance(event, member);
            System.out.println("Attendee added successfully to event: " + eventId);

            return true;
        } catch (Exception e) {
            System.err.println("Error adding attendee: " + e.getMessage());
            return false;
        }
    }

    public boolean removeAttendee(Long eventId, Long memberId) {
        try {
            Optional<Event> eventOpt = eventDAO.getEventById(eventId);
            if (eventOpt.isEmpty()) {
                System.err.println("Event not found: " + eventId);
                return false;
            }

            Event event = eventOpt.get();

            if (!event.getLibrary().getId().equals(librarian.getWorkLibrary().getId())) {
                System.err.println("Cannot remove attendee: Not authorized");
                return false;
            }

            attendanceDAO.deleteAttendance(eventId, memberId);
            System.out.println("Attendee removed successfully from event: " + eventId);

            return true;
        } catch (Exception e) {
            System.err.println("Error removing attendee: " + e.getMessage());
            return false;
        }
    }

    public int getEventAttendanceCount(Long eventId) {
        try {
            Optional<Event> eventOpt = eventDAO.getEventById(eventId);
            if (eventOpt.isEmpty()) {
                System.err.println("Event not found: " + eventId);
                return 0;
            }

            Event event = eventOpt.get();

            if (!event.getLibrary().getId().equals(librarian.getWorkLibrary().getId())) {
                System.err.println("Cannot view attendance: Not authorized");
                return 0;
            }

            return attendanceDAO.findEventAttendees(event).size();
        } catch (Exception e) {
            System.err.println("Error fetching event attendance: " + e.getMessage());
            return 0;
        }
    }

    // Helper method to notify attendees via email
    private void notifyAttendees(Event event, String subject, String message) {
        try {
            List<Member> attendees = attendanceDAO.findEventAttendees(event);
            for (Member attendee : attendees) {
                emailService.sendEmail(
                        attendee.getEmail(),
                        subject,
                        "Dear " + attendee.getName() + ",\n\n" + message +
                                "\n\nBest regards,\n" + librarian.getWorkLibrary().getName() + " Library Team"
                );
            }
            System.out.println("Notifications sent to " + attendees.size() + " attendees");
        } catch (Exception e) {
            System.err.println("Error sending notifications: " + e.getMessage());
        }
    }

    private void validateEventParameters(String title, LocalDateTime eventDate, int maxCapacity) {
        validateTitle(title);
        validateEventDate(eventDate);
        validateCapacity(maxCapacity);
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Event title cannot be empty");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Event title cannot exceed 100 characters");
        }
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate == null) {
            throw new IllegalArgumentException("Event date cannot be null");
        }
    }

    private void validateCapacity(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Event capacity must be positive");
        }
        if (maxCapacity > 1000) {
            throw new IllegalArgumentException("Event capacity cannot exceed 1000");
        }
    }
}