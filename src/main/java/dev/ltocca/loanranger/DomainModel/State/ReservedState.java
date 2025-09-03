package dev.ltocca.loanranger.DomainModel.State;

import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.DomainModel.BookStatus;

public class ReservedState implements AvailabilityState {
    private final BookStatus bookStatus = BookStatus.RESERVED;

    @Override
    public void loan(BookCopy copy) {
        System.out.println("Fulfilling reservation and loaning book.");
        copy.changeState(new LoanedState());
    }

    @Override
    public void returnCopy(BookCopy copy) {
        System.out.println("Error: Cannot return a book that is reserved (it is not yet loaned).");
    }

    @Override
    public void reserve(BookCopy copy) {
        System.out.println("Error: This copy is already reserved.");
    }

    @Override
    public void placeUnderMaintenance(BookCopy copy) {
        System.out.println("Error: Cannot place a reserved book under maintenance.");
    }

    @Override
    public String getStatus() {
        return bookStatus.toString();
    }
}
