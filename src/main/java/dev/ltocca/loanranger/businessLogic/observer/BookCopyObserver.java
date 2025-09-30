package dev.ltocca.loanranger.businessLogic.observer;

import dev.ltocca.loanranger.domainModel.BookCopy;

public interface BookCopyObserver {
    void onBookCopyAvailable(BookCopy bookCopy);
}
