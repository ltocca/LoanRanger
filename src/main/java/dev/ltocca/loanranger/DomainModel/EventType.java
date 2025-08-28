package dev.ltocca.loanranger.DomainModel;

import lombok.Getter;

@Getter
public enum EventType {
    BOOK_PRESENTATION("Book Presentation"),
    POETRY_READING("Poetry Reading"),
    AUTHOR_TALK("Author Talk"),
    WORKSHOP("Workshop"),
    BOOK_CLUB("Book Club Meeting"),
    STORYTELLING("Storytelling Session"),
    LECTURE("Lecture"),
    EXHIBITION("Exhibition");

    private final String displayName;

    EventType(String displayName) {
        this.displayName = displayName;
    }

}
