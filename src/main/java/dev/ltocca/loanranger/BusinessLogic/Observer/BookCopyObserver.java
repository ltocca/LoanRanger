package dev.ltocca.loanranger.BusinessLogic.Observer;

import dev.ltocca.loanranger.DomainModel.Book;

public interface BookObserver {
    void onBookAvailable(Book book);
}
