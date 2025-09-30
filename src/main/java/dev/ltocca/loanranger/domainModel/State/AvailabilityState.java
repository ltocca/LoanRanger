package dev.ltocca.loanranger.domainModel.State;
/**
 *
 */

import dev.ltocca.loanranger.domainModel.BookCopy;

public interface AvailabilityState {
    void loan(BookCopy copy);

    void returnCopy(BookCopy copy);

    void reserve(BookCopy copy);

    void placeUnderMaintenance(BookCopy copy);

    void markAsAvailable(BookCopy copy);

    String getStatus();
}