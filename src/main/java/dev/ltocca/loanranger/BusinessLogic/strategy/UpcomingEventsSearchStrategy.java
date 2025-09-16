package dev.ltocca.loanranger.BusinessLogic.strategy;

import dev.ltocca.loanranger.DomainModel.Event;
import dev.ltocca.loanranger.ORM.EventDAO;
import java.util.List;

public final class UpcomingEventsSearchStrategy implements EventSearchStrategy {
    @Override
    public List<Event> search(String query, EventDAO eventDAO) {
        // no need to use the query, all upcoming events
        try {
            return eventDAO.findUpcomingEvents();
        } catch (Exception e) {
            throw new RuntimeException("Error searching for upcoming events", e);
        }
    }

    @Override
    public String getDescription() {
        return "Lists all upcoming events";
    }

    @Override
    public int getMinQueryLength() {
        return 0;
    }
}