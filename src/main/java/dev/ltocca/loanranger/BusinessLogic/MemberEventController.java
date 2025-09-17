package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.DomainModel.Event;
import dev.ltocca.loanranger.DomainModel.Member;
import dev.ltocca.loanranger.ORM.AttendanceDAO;
import dev.ltocca.loanranger.ORM.EventDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MemberEventController {
    private final Member member;
    private final EventSearchService eventSearchService;
    private final AttendanceDAO attendanceDAO;
    private final EventDAO eventDAO;

    public MemberEventController(Member member) throws SQLException {
        this.member = member;
        this.eventSearchService = new EventSearchService();
        this.attendanceDAO = new AttendanceDAO();
        this.eventDAO = new EventDAO();
    }


    public void registerForEvent(Long eventId) {
        try {
            Optional<Event> eventOpt = eventDAO.getEventById(eventId);
            if (eventOpt.isEmpty()) {
                System.err.println("Event not found with ID: " + eventId);
                return;
            }
            Event event = eventOpt.get();

            if (attendanceDAO.isMemberAttending(event, member)) {
                System.out.println("You are already registered for this event: " + event.getTitle());
                return;
            }

            List<Member> attendees = attendanceDAO.findEventAttendees(event);
            if (attendees.size() >= event.getMaxCapacity()) {
                System.err.println("Cannot register for event '" + event.getTitle() + "'. The event is full.");
                return;
            }

            attendanceDAO.addAttendance(event, member);
            System.out.println("Successfully registered for event: " + event.getTitle());
        } catch (Exception e) {
            System.err.println("Error registering for event ID " + eventId + ": " + e.getMessage());
        }
    }

    public void unregisterFromEvent(Long eventId) {
        try {
            attendanceDAO.deleteAttendance(eventId, member.getId());
            System.out.println("Successfully unregistered from event ID: " + eventId);
        } catch (Exception e) {
            System.err.println("Error unregistering from event ID " + eventId + ": " + e.getMessage());
        }
    }

    public List<Event> getMyEvents() {
        try {
            return attendanceDAO.findMemberParticipation(member);
        } catch (Exception e) {
            System.err.println("Error fetching your events: " + e.getMessage());
            return List.of();
        }
    }

    public List<Event> searchEvents(String query) {
        try {
            return eventSearchService.smartSearch(query);
        } catch (IllegalArgumentException e) {
            System.err.println("Event search error: " + e.getMessage());
            return List.of();
        }
    }


    public List<Event> getUpcomingEvents() {
        try {
            return eventSearchService.search("", EventSearchService.SearchType.UPCOMING);
        } catch (Exception e) {
            System.err.println("Error fetching upcoming events: " + e.getMessage());
            return List.of();
        }
    }
}