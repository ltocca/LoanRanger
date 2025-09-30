package dev.ltocca.loanranger.businessLogic.strategy;

import dev.ltocca.loanranger.domainModel.BookCopy;
import dev.ltocca.loanranger.domainModel.State.AvailableState;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;

import java.util.ArrayList;
import java.util.List;

public final class AvailableOnlySearchStrategy implements BookCopySearchStrategy {
    private final BookCopySearchStrategy strategy;

    public AvailableOnlySearchStrategy(BookCopySearchStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public List<BookCopy> search(String query, BookCopiesDAO bookCopiesDAO) {
        List<BookCopy> bookCopies = strategy.search(query, bookCopiesDAO);
        List<BookCopy> availableCopies = new ArrayList<>();

        for (BookCopy copy : bookCopies) {
            if (copy.getState() instanceof AvailableState)
                availableCopies.add(copy);
        }
        return availableCopies;
    }

    @Override
    public String getDescription() {
        return strategy.getDescription() + " (only available copies)";
    }
}
