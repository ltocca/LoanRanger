package dev.ltocca.loanranger.BusinessLogic.strategy;

import dev.ltocca.loanranger.DomainModel.Event;
import dev.ltocca.loanranger.ORM.EventDAO;
import dev.ltocca.loanranger.Util.DateRangeParser;

import java.time.LocalDateTime;
import java.util.List;

public final class EventDateRangeSearchStrategy implements EventSearchStrategy {
    @Override
    public List<Event> search(String query, EventDAO eventDAO) {
        if (query == null || query.trim().isEmpty()) {
            System.err.println("Error: the query is null or empty");
            return List.of();
        }

        try {
            DateRangeParser.DateRange dateRange = DateRangeParser.parse(query.trim());
            return eventDAO.findEventsByDateRange(dateRange.start(), dateRange.end());
        } catch (Exception e) {
            throw new RuntimeException("Error searching events by date range", e);
        }
    }



    @Override
    public String getDescription() {
        return "Search by date ('today', 'this week', '2024-12', '2024-12-25')";
    }

    @Override
    public int getMinQueryLength() {
        return 4;
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {}
}