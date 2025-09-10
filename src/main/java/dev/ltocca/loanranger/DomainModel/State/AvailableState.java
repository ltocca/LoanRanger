package dev.ltocca.loanranger.DomainModel.State;

import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.DomainModel.BookStatus;

public class AvailableState implements AvailabilityState {
    private final BookStatus bookStatus = BookStatus.AVAILABLE;

    @Override
    public void loan(BookCopy copy) {
        System.out.println("Book is available. Loaning...");
        copy.changeState(new LoanedState());
    }

    @Override
    public void returnCopy(BookCopy copy) {
        System.err.println("Error: Cannot return a book that is already available.");
    }

    @Override
    public void reserve(BookCopy copy) {
        System.out.println("Book is available. Reserving...");
        copy.changeState(new ReservedState());
    }

    @Override
    public void placeUnderMaintenance(BookCopy copy) {
        System.out.println("Placing book under maintenance.");
        copy.changeState(new UnderMaintenanceState());
    }

    @Override
    public void markAsAvailable(BookCopy copy) {
        System.err.println("Error: The copy is already available.");

    }

    @Override
    public String getStatus() {
        return bookStatus.toString();
    }
}