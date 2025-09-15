package dev.ltocca.loanranger.BusinessLogic.strategy;

import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;

import java.util.List;

public final class AuthorSearchStrategy implements BookCopySearchStrategy {
    @Override
    public List<BookCopy> search(String query, BookCopiesDAO bookCopiesDAO) {
        if (query == null || query.trim().isEmpty()) {
            System.err.println("Error: the query is null or empty");
            return List.of();
        }
        try {
            return bookCopiesDAO.searchByAuthor(query.trim());
        } catch (Exception e) { // broad exception check
            throw new RuntimeException("Error searching book copies by author", e);
        }
    }

    @Override
    public String getDescription() {
        return "Search for book author name";
    }

    @Override
    public int getMinQueryLength() {
        return 2;
    }
}
