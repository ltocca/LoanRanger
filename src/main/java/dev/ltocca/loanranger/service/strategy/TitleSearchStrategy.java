package dev.ltocca.loanranger.service.strategy;

import dev.ltocca.loanranger.domainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;

import java.util.List;

public final class TitleSearchStrategy implements BookCopySearchStrategy {
    @Override
    public List<BookCopy> search(String query, BookCopiesDAO bookCopiesDAO) {
        if (query == null || query.trim().isEmpty()) {
            System.err.println("Error: the query is null or empty");
            return List.of();
        }

        try { // Use database-level search for better performance, instead of implementing less performant code.
            return bookCopiesDAO.searchByTitle(query.trim());
        } catch (Exception e) { // broad exception check
            throw new RuntimeException("Error searching book copies by title", e);
        }
    }

    @Override
    public String getDescription() {
        return "Search for book title";
    }


    @Override
    public int getMinQueryLength() {
        return 2;
    }
}
