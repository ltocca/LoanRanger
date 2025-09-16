package dev.ltocca.loanranger.BusinessLogic.strategy;

import dev.ltocca.loanranger.DomainModel.Event;
import dev.ltocca.loanranger.ORM.EventDAO;
import java.util.List;

public final class EventTitleSearchStrategy implements EventSearchStrategy {
    public List<Event> search(String query, EventDAO eventDAO) {
        if (query == null || query.trim().isEmpty()) {
            System.err.println("Error: the query is null or empty");
            return List.of();
        }

        try {
            return eventDAO.findEventsByTitle(query.trim());
        } catch (Exception e) {
            throw new RuntimeException("Error searching events by title", e);
        }
    }

    public String getDescription() {
        return "Search by event title";
    }

    public int getMinQueryLength() {
        return 2;
    }
}