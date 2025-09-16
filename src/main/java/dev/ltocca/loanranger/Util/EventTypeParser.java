package dev.ltocca.loanranger.Util;

import dev.ltocca.loanranger.DomainModel.EventType;

import java.util.ArrayList;
import java.util.List;

public final class EventTypeParser {

    private EventTypeParser() {}

    public static List<EventType> parse(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String lowerQuery = query.trim().toLowerCase();
        List<EventType> matches = new ArrayList<>();

        for (EventType type : EventType.values()) {
            String enumName = type.name().toLowerCase();
            String displayName = type.getDisplayName().toLowerCase();

            if (enumName.equals(lowerQuery) || displayName.equals(lowerQuery) || enumName.contains(lowerQuery) || displayName.contains(lowerQuery)) {
                matches.add(type);
            }
        }

        return matches;
    }

    public static EventType parseExactType(String query) {
        List<EventType> matches = parse(query);
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("No matching event type for query: " + query);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Multiple matching event types for query: " + query);
        }
        return matches.getFirst(); // executed only if one EventType found
    }
}
