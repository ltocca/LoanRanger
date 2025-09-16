package dev.ltocca.loanranger.BusinessLogic.strategy;

import dev.ltocca.loanranger.DomainModel.Event;
import dev.ltocca.loanranger.ORM.EventDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public final class EventFullTextStrategy implements EventSearchStrategy {
    @Override
    public List<Event> search(String query, EventDAO eventDAO) {
        if (query == null || query.trim().isEmpty()) {
            System.err.println("Error: the query is null or empty");
            return List.of();
        }

        String refinedQuery = query.trim().toLowerCase();

        try {
            List<Event> titleResults = eventDAO.findEventsByTitle(refinedQuery);
            List<Event> descriptionResults = eventDAO.findEventsByDescription(refinedQuery);
            List<Event> libraryResults = eventDAO.findEventsByLibraryName(refinedQuery);

            List<Event> allResults = new ArrayList<>();
            allResults.addAll(titleResults);
            allResults.addAll(descriptionResults);
            allResults.addAll(libraryResults);

            List<Event> uniqueResults = new ArrayList<>();
            Set<Long> seenIds = new HashSet<>();

            for (Event event : allResults) {
                if (!seenIds.contains(event.getId())) {
                    seenIds.add(event.getId());
                    uniqueResults.add(event);
                }
            }

            return uniqueResults;

        } catch (Exception e) {
            throw new RuntimeException("Error during full-text event search", e);
        }
    }

    @Override
    public String getDescription() {
        return "Full-text search across event titles, descriptions, and library names";
    }

    @Override
    public int getMinQueryLength() {
        return 2;
    }
}