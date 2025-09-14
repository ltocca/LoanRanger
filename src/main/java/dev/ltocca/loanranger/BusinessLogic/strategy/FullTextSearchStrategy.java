package dev.ltocca.loanranger.BusinessLogic.strategy;

import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FullTextSearchStrategy implements BookCopySearchStrategy {
    @Override
    public List<BookCopy> search(String query, BookCopiesDAO bookCopiesDAO) {
        if (query == null || query.trim().isEmpty()) {
            System.err.println("Error: the query is null or empty");
            return List.of();
        }
        // added to be sure that the query could have results
        String refinedQuery = query.trim().toLowerCase();
        try {
            List<BookCopy> titleSearchResults = bookCopiesDAO.searchByTitle(refinedQuery);
            List<BookCopy> authorSearchResults = bookCopiesDAO.searchByAuthor(refinedQuery);
            List<BookCopy> isbnSearchResults = bookCopiesDAO.searchByIsbn(refinedQuery);

            List<BookCopy> allResults = new ArrayList<>();
            allResults.addAll(titleSearchResults);
            allResults.addAll(authorSearchResults);
            allResults.addAll(isbnSearchResults);

            List<BookCopy> uniqueResults = new ArrayList<>();
            Set<Long> seenIds = new HashSet<>(); // using hashing for quick access and insertion

            for (BookCopy bookCopy : allResults) {
                if (!seenIds.contains(bookCopy.getCopyId())) {
                    seenIds.add(bookCopy.getCopyId());
                    uniqueResults.add(bookCopy);
                }
            }

            return uniqueResults;
        } catch (Exception e) {
            throw new RuntimeException("Error: full text search on book copies gone wrong!");
        }

    }

    @Override
    public int getMinQueryLength() {
        return BookCopySearchStrategy.super.getMinQueryLength();
    }
}
