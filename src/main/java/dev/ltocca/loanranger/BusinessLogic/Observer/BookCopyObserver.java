package dev.ltocca.loanranger.BusinessLogic.Observer;

import dev.ltocca.loanranger.DomainModel.BookCopy;

public interface BookCopyObserver {
    void onBookCopyAvailable(BookCopy bookCopy);
}
