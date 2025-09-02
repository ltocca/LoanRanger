package dev.ltocca.loanranger.DomainModel;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter

public class Reservation implements Observer {
    private Long id;
    private Long bookId;
    private Long userId;
    private Long libraryId; // Preferred library for pickup
    private LocalDate reservationDate;
    private boolean isActive;

    public Reservation() {
        this.reservationDate = LocalDate.now();
        this.isActive = true;
    }

    public Reservation(Long bookId, Long userId, Long libraryId) {
        this();
        this.bookId = bookId;
        this.userId = userId;
        this.libraryId = libraryId;
    }

    @Override
    public void update(Book book) {
        if (book.getId().equals(this.bookId) && this.isActive) {
            book.setAvailable(true);
            System.out.println("Notification: The book you requested: '" + book.getTitle() +
                    "' is now available for pickup at library ID: " + book.getLibraryId());
        }
    }
}
