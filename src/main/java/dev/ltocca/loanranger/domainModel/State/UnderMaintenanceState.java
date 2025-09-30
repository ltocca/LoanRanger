package dev.ltocca.loanranger.domainModel.State;

import dev.ltocca.loanranger.domainModel.BookCopy;
import dev.ltocca.loanranger.domainModel.BookStatus;

public class UnderMaintenanceState implements AvailabilityState {
    private final BookStatus bookStatus = BookStatus.UNDER_MAINTENANCE;

    @Override
    public void loan(BookCopy copy) {
        System.out.println("Error: Can't loan this copy because it is under maintenance.");
    }

    @Override
    public void returnCopy(BookCopy copy) {
        System.err.println("Error. This copy is not loaned.");
    }

    @Override
    public void reserve(BookCopy copy) {
        System.out.println("Error: Can't reserve this copy because is under maintenance.");
    }

    @Override
    public void placeUnderMaintenance(BookCopy copy) {
        System.out.println("Book is already under maintenance.");
    }

    @Override
    public void markAsAvailable(BookCopy copy) {
        copy.changeState(new AvailableState());
        copy.notifyAvailabilityToWatchers();
        System.out.println("Book maintenance complete. Now the copy is available.");
    }

    @Override
    public String getStatus() {
        return bookStatus.toString();
    }
}
