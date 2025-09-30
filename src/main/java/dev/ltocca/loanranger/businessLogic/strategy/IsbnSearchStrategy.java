package dev.ltocca.loanranger.businessLogic.strategy;

import dev.ltocca.loanranger.domainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;

import java.util.List;

public final class IsbnSearchStrategy implements BookCopySearchStrategy {
    @Override
    public List<BookCopy> search(String query, BookCopiesDAO bookCopiesDAO) {
        if (query == null || query.trim().isEmpty()){
            System.err.println("Error: the query is null or empty");
            return List.of();
        }
        // Regex added to remove all characters apart from the numbers and the X for ISBN10
        String cleanQuery = query.trim().replaceAll("[^0-9X]", "");
        try{
            return bookCopiesDAO.searchByIsbn(cleanQuery);
        } catch (Exception e) { // broad exception check
            throw new RuntimeException("Error searching book copies by isbn", e);
        }
    }

    @Override
    public String getDescription() {
        return "Search for book ISBN code";
    }


    @Override
    public int getMinQueryLength() {
        return 6;
    }
}
