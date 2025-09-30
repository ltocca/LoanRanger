package dev.ltocca.loanranger.domainModel;

import dev.ltocca.loanranger.businessLogic.observer.BookCopyObserver;
import dev.ltocca.loanranger.domainModel.State.AvailabilityState;
import dev.ltocca.loanranger.domainModel.State.AvailableState;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor

public class BookCopy {
    private Long copyId;
    private Book book;
    private Library library;
    private AvailabilityState state;
    private List<BookCopyObserver> observers = new ArrayList<>();

    public BookCopy(Book book, Library library, AvailabilityState state) {
        this.book = book;
        this.library = library;
        this.state = new AvailableState(); // when the copy is created the book is available
    }

    // State pattern methods

    public void changeState(AvailabilityState newState) {
        this.state = newState;
    }

    public void loan() {
        state.loan(this);
    }

    public void returnCopy() {
        state.returnCopy(this);
    }

    public void reserve() {
        state.reserve(this);
    }

    public void placeUnderMaintenance() {
        state.placeUnderMaintenance(this);
    }

    public void markAsAvailable() {
        state.markAsAvailable(this);
    }

    // observer pattern methods

    public void addObserver(BookCopyObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(BookCopyObserver observer) {
        observers.remove(observer);
    }

    // Notify all *observers of this specific copy* that it is available
    public void notifyAvailabilityToWatchers() {
        //List<BookCopyObserver> observersSnapshot = new ArrayList<>(this.observers); // first idea was to save the observes
        for (BookCopyObserver observer : observers) {
            observer.onBookCopyAvailable(this);
            removeObserver(observer);
        }
    }



}