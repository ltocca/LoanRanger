package dev.ltocca.loanranger.service;

import dev.ltocca.loanranger.service.strategy.*;
import dev.ltocca.loanranger.domainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class BookCopySearchService {
    private final BookCopiesDAO bookCopiesDAO;
    private final Map<SearchType, BookCopySearchStrategy> searchStrategies;
    private BookCopySearchStrategy currentStrategy;

    @Autowired
    public BookCopySearchService(BookCopiesDAO bookCopiesDAO) throws SQLException {
        this.bookCopiesDAO = bookCopiesDAO;
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

    public List<BookCopy> searchAvailableOnly(String query, SearchType baseSearchType) {
        BookCopySearchStrategy baseStrategy = searchStrategies.get(baseSearchType);
        if (baseStrategy == null) {
            throw new IllegalArgumentException("Unsupported base search strategy: " + baseSearchType);
        }

        BookCopySearchStrategy availableOnlyStrategy = new AvailableOnlySearchStrategy(baseStrategy);

        validateQuery(query, availableOnlyStrategy);

        return availableOnlyStrategy.search(query, bookCopiesDAO);
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

    public List<BookCopy> smartSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        SearchType detectedType = detectSearchType(query.trim());
        return search(query, detectedType);
    }

    public List<BookCopy> smartSearchAvailableOnly(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        SearchType detectedType = detectSearchType(query.trim());
        return searchAvailableOnly(query, detectedType);
    }

    private SearchType detectSearchType(String query) {
        if (looksLikeISBN(query)) {
            return SearchType.ISBN;
        }
        if (looksLikeAuthorName(query)) {
            return SearchType.AUTHOR;
        }
        if (looksLikeTitle(query)) {
            return SearchType.TITLE;
        }
        return SearchType.FULL_TEXT; // fallback for complex or ambiguous queries
    }

    private boolean looksLikeISBN(String query) {
        String cleanQuery = query.replaceAll("[\\-\\s]", "");
        return cleanQuery.matches("^[0-9X]+$") && cleanQuery.length() > 6; // in this way the smart search can accept for example years as a query
    }

    private boolean looksLikeAuthorName(String query) {
        return query.contains(" ") || query.split("\\s+").length >= 2;
    }

    private boolean looksLikeTitle(String query) {
        return query.length() <= 50 && query.contains(" ");
    }

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

