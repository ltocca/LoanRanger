package dev.ltocca.loanranger.BusinessLogic.strategy;

import dev.ltocca.loanranger.DomainModel.Event;
import dev.ltocca.loanranger.ORM.EventDAO;

import java.util.List;

public sealed interface EventSearchStrategy
        permits EventTitleSearchStrategy, EventTypeSearchStrategy,
        EventDateRangeSearchStrategy, EventLibrarySearchStrategy, UpcomingEventsSearchStrategy, EventFullTextStrategy{

    List<Event> search(String query, EventDAO eventDAO);

    String getDescription();

    default int getMinQueryLength() {
        return 1;
    }
}