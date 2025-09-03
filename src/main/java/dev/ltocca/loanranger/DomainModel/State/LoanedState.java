package dev.ltocca.loanranger.DomainModel.State;

import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.DomainModel.BookStatus;

public class LoanedState implements AvailabilityState {
    private final BookStatus bookStatus = BookStatus.LOANED;

    @Override
    public void loan(BookCopy copy) {
        System.out.println("Error: Book is already loaned out.");
    }

    @Override
    public void returnCopy(BookCopy copy) {
        System.out.println("Book returned successfully.");
        copy.changeState(new AvailableState());
    }

    @Override
    public void reserve(BookCopy copy) {
        System.out.println("Error: Cannot reserve a book that is already loaned out.");
    }

    @Override
    public void placeUnderMaintenance(BookCopy copy) {
        System.out.println("Error: Cannot place a loaned book under maintenance.");
    }

    @Override
    public String getStatus() {
        return bookStatus.toString();
    }
}
