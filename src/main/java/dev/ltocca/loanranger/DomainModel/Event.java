package dev.ltocca.loanranger.DomainModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Event {
    private Long id;
    private Library library;
    private String title;
    private String description;
    private EventType eventType;
    private LocalDateTime eventDate;
    private String location; // optional
    private int maxCapacity;

    public Event(Library library, String title, LocalDateTime eventDate) {
        this.library = library;
        this.title = title;
        this.eventDate = eventDate;
    }
}
