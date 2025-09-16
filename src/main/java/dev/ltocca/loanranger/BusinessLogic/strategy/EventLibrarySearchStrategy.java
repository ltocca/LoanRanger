package dev.ltocca.loanranger.BusinessLogic.strategy;

import dev.ltocca.loanranger.DomainModel.Event;
import dev.ltocca.loanranger.ORM.EventDAO;

import java.util.List;

public final class EventLibrarySearchStrategy implements EventSearchStrategy {
    @Override
    public List<Event> search(String query, EventDAO eventDAO) {
        if (query == null || query.trim().isEmpty()) {
            System.err.println("Error: the query is null or empty");
            return List.of();
        }
        String loweredQuery = query.toLowerCase().trim();
        try {
            return eventDAO.findEventsByLibraryName(loweredQuery);
        } catch (Exception e) {
            throw new RuntimeException("Error searching events by library name %s", e);
        }
    }

    @Override
    public String getDescription() {
        return "Search by library name";
    }

    @Override
    public int getMinQueryLength() {
        return 2;
    }
}