package dev.ltocca.loanranger.ORM.DAOInterfaces;

import dev.ltocca.loanranger.DomainModel.Event;
import dev.ltocca.loanranger.DomainModel.Library;

import java.util.List;
import java.util.Optional;

public interface IEventDAO {
    Event createEvent(Event event);
    Optional<Event> getEventById(Long id);
    void updateEvent(Event event);
    void deleteEvent(Long id);

    void editLibrary(Event event, Library library);

    List<Event> findEventsByLibrary(Library library);
    List<Event> findUpcomingEvents();
    List<Event> findEventsByTitle(String title);
}
