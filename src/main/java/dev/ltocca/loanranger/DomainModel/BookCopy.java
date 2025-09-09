package dev.ltocca.loanranger.DomainModel;

import dev.ltocca.loanranger.DomainModel.State.AvailabilityState;
import dev.ltocca.loanranger.DomainModel.State.AvailableState;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class BookCopy {
    private Long copyId;
    private Book book;
    private Library library;
    private AvailabilityState state;

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


}