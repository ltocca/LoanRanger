package dev.ltocca.loanranger.DomainModel.State;
/**
 *
 */

import dev.ltocca.loanranger.DomainModel.BookCopy;

public interface AvailabilityState {
    void loan(BookCopy copy);

    void returnCopy(BookCopy copy);

    void reserve(BookCopy copy);

    void placeUnderMaintenance(BookCopy copy);

    void markAsAvailable(BookCopy copy);

    String getStatus();
}