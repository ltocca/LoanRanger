package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.BusinessLogic.strategy.*;
import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;


import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public class BookCopySearchService {
    private final BookCopiesDAO bookCopiesDAO;
    private final Map<SearchType, BookCopySearchStrategy> searchStrategies;
    private BookCopySearchStrategy currentStrategy;

    public BookCopySearchService() throws SQLException {
        this.bookCopiesDAO = new BookCopiesDAO();
        this.searchStrategies = Map.of(
                SearchType.TITLE, new TitleSearchStrategy(),
                SearchType.AUTHOR, new AuthorSearchStrategy(),
                SearchType.ISBN, new IsbnSearchStrategy(),
                SearchType.FULL_TEXT, new FullTextSearchStrategy(),
                SearchType.AVAILABLE_ONLY, new AvailableOnlySearchStrategy(new FullTextSearchStrategy())
        );
        this.currentStrategy = searchStrategies.get(SearchType.FULL_TEXT);
    }

    public List<BookCopy> search(String query) {
        if (validateQuery(query, currentStrategy)) {
            return currentStrategy.search(query, bookCopiesDAO);
        }
        return List.of();
    }

    public List<BookCopy> search(String query, SearchType searchType) {
        this.setSearchStrategy(searchType);
        if (validateQuery(query, currentStrategy)) {
            return search(query);
        }
        return List.of();
    }

    public void setSearchStrategy(SearchType searchType) {
        if (searchType == null) {
            throw new IllegalArgumentException("Search Type cannot be null.");
        }
        this.currentStrategy = searchStrategies.get(searchType);
        if (currentStrategy == null) {
            throw new IllegalArgumentException("Unsupported search strategy!");
        }
    }

    public Map<SearchType, String> getAllStrategyDescriptions() {
        return Map.of(
                SearchType.TITLE, searchStrategies.get(SearchType.TITLE).getDescription(),
                SearchType.AUTHOR, searchStrategies.get(SearchType.AUTHOR).getDescription(),
                SearchType.ISBN, searchStrategies.get(SearchType.ISBN).getDescription(),
                SearchType.FULL_TEXT, searchStrategies.get(SearchType.FULL_TEXT).getDescription(),
                SearchType.AVAILABLE_ONLY, searchStrategies.get(SearchType.AVAILABLE_ONLY).getDescription()
        );
    }

    //TODO implement smart search that detects the type

    private boolean validateQuery(String query, BookCopySearchStrategy searchStrategy) {
        boolean valid = true;
        if (query == null || query.trim().isEmpty()) {
            valid = false;
            throw new IllegalArgumentException("The query cannot be empty or null");
        }
        if (query.trim().length() < searchStrategy.getMinQueryLength()) {
            valid = false;
            throw new IllegalArgumentException(String.format("The query length must be at least %d characters for %s", searchStrategy.getMinQueryLength(), searchStrategy.getDescription()));
        }
        return valid;
    }

    public enum SearchType {
        TITLE, AUTHOR, ISBN, FULL_TEXT, AVAILABLE_ONLY
    }

}

