package dev.ltocca.loanranger.BusinessLogic.strategy;

import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;

import java.util.List;

public final class IsbnSearchStrategy implements BookCopySearchStrategy {
    @Override
    public List<BookCopy> search(String query, BookCopiesDAO bookCopiesDAO) {
        if (query == null || query.trim().isEmpty()){
            System.err.println("Error: the query is null or empty");
            return List.of();
        }
        try{
            return bookCopiesDAO.searchByIsbn(query.trim());
        } catch (Exception e) { // broad exception check
            throw new RuntimeException("Error searching book copies by isbn", e);
        }
    }

    @Override
    public int getMinQueryLength() {
        return BookCopySearchStrategy.super.getMinQueryLength();
    }
}
