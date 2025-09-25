package dev.ltocca.loanranger.DomainModel.State;

import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.DomainModel.BookStatus;

public class LoanedState implements AvailabilityState {
    private final BookStatus bookStatus = BookStatus.LOANED;

    @Override
    public void loan(BookCopy copy) {
        System.err.println("Error: Book is already loaned out.");
    }

    @Override
    public void returnCopy(BookCopy copy) {
        copy.changeState(new AvailableState());
        copy.notifyAvailabilityToWatchers();
        System.out.println("Book returned successfully.");
    }

    @Override
    public void reserve(BookCopy copy) {
        System.err.println("Error: Cannot reserve a book that is already loaned out.");
    }

    @Override
    public void placeUnderMaintenance(BookCopy copy) {
        System.err.println("Error: Cannot place a loaned book under maintenance.");
    }

    @Override
    public void markAsAvailable(BookCopy copy) { // ONLY RETURNING CAN MARK AS AVAILABLE THE COPY
        System.err.println("Error: Cannot mark loaned book as available (after maintenance method).");
    }

    @Override
    public String getStatus() {
        return bookStatus.toString();
    }
}
