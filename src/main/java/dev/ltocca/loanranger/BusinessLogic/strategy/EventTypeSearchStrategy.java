package dev.ltocca.loanranger.BusinessLogic.strategy;

import dev.ltocca.loanranger.DomainModel.Event;
import dev.ltocca.loanranger.DomainModel.EventType;
import dev.ltocca.loanranger.ORM.EventDAO;
import dev.ltocca.loanranger.Util.EventTypeParser;

import java.util.ArrayList;
import java.util.List;

public final class EventTypeSearchStrategy implements EventSearchStrategy {

    @Override
    public List<Event> search(String query, EventDAO eventDAO) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        try {
            EventType type = EventTypeParser.parseExactType(query.trim());
            return eventDAO.findEventsByEventType(type);

        } catch (IllegalArgumentException e) {// Fallback to partial match
            List<EventType> matches = EventTypeParser.parse(query.trim());
            List<Event> results = new ArrayList<>();
            for (EventType t : matches) {
                results.addAll(eventDAO.findEventsByEventType(t));
            }
            return results;
        }
    }

    @Override
    public String getDescription() {
        return "Search by event type (exact, display name, or partial match)";
    }

    @Override
    public int getMinQueryLength() {
        return 2;
    }
}
