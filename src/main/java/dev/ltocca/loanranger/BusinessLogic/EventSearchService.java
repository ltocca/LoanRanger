package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.Util.EventTypeParser;
import dev.ltocca.loanranger.BusinessLogic.strategy.*;
import dev.ltocca.loanranger.DomainModel.Event;
import dev.ltocca.loanranger.DomainModel.EventType;
import dev.ltocca.loanranger.ORM.EventDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class EventSearchService {

    private final EventDAO eventDAO;
    private final Map<SearchType, EventSearchStrategy> searchStrategies;

    public EventSearchService() throws SQLException {
        this.eventDAO = new EventDAO();
        this.searchStrategies = Map.of(
                SearchType.TITLE, new EventTitleSearchStrategy(),
                SearchType.TYPE, new EventTypeSearchStrategy(),
                SearchType.DATE_RANGE, new EventDateRangeSearchStrategy(),
                SearchType.LIBRARY, new EventLibrarySearchStrategy(),
                SearchType.UPCOMING, new UpcomingEventsSearchStrategy(),
                SearchType.FULL_TEXT, new EventFullTextStrategy()
        );
    }

    public List<Event> search(String query, SearchType searchType) {
        EventSearchStrategy strategy = searchStrategies.get(searchType);
        if (strategy == null) throw new IllegalArgumentException("Unsupported search type: " + searchType);
        validateQuery(query, strategy);
        return strategy.search(query, eventDAO);
    }

    public List<Event> smartSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return search("", SearchType.UPCOMING);
        }

        SearchType type = detectSearchType(query.trim());
        return search(query, type);
    }

    private SearchType detectSearchType(String query) {
        if (looksLikeDate(query)) return SearchType.DATE_RANGE;
        if (looksLikeEventType(query)) return SearchType.TYPE;
        if (looksLikeLibraryName(query)) return SearchType.LIBRARY;
        return SearchType.FULL_TEXT; // fallback
    }

    private boolean looksLikeDate(String query) {
        String lower = query.toLowerCase();
        return lower.matches("today|tomorrow|this week|this month|\\d{4}-\\d{2}(-\\d{2})?|\\d{1,2} [a-z]+");
    }

    private boolean looksLikeEventType(String query) {
        try {
            EventTypeParser.parseExactType(query);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean looksLikeLibraryName(String query) {
        // simplistic check: assume library names are longer than 2 characters
        return query.length() >= 3;
    }


    private void validateQuery(String query, EventSearchStrategy strategy) {
        if (strategy instanceof UpcomingEventsSearchStrategy) return;

        if (query == null || query.trim().isEmpty())
            throw new IllegalArgumentException("Query cannot be null or empty for this search type.");

        if (query.trim().length() < strategy.getMinQueryLength())
            throw new IllegalArgumentException(
                    String.format("Query must be at least %d characters for %s",
                            strategy.getMinQueryLength(), strategy.getDescription())
            );
    }

    public enum SearchType {
        TITLE,
        TYPE,
        DATE_RANGE,
        LIBRARY,
        UPCOMING,
        FULL_TEXT
    }
}
