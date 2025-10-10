/*
package dev.ltocca.loanranger.service;

import dev.ltocca.loanranger.domainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookCopySearchServiceTest {

    @Mock
    private BookCopiesDAO bookCopiesDAO;

    @InjectMocks
    private BookCopySearchService searchService;

    private BookCopy testCopy;

    @BeforeEach
    void setUp() {
        testCopy = new BookCopy(); // A simple dummy object for list returns
    }

    @Test
    void search_byTitle_callsCorrectDaoMethod() {
        // Given
        String query = "Effective Java";
        when(bookCopiesDAO.searchByTitle(query)).thenReturn(Collections.singletonList(testCopy));

        // When
        List<BookCopy> results = searchService.search(query, BookCopySearchService.SearchType.TITLE);

        // Then
        assertThat(results).hasSize(1);
        verify(bookCopiesDAO).searchByTitle(query);
        verify(bookCopiesDAO, never()).searchByAuthor(anyString());
        verify(bookCopiesDAO, never()).searchByIsbn(anyString());
    }

    @Test
    void search_byAuthor_callsCorrectDaoMethod() {
        // Given
        String query = "Joshua Bloch";
        when(bookCopiesDAO.searchByAuthor(query)).thenReturn(Collections.singletonList(testCopy));

        // When
        List<BookCopy> results = searchService.search(query, BookCopySearchService.SearchType.AUTHOR);

        // Then
        assertThat(results).hasSize(1);
        verify(bookCopiesDAO).searchByAuthor(query);
        verify(bookCopiesDAO, never()).searchByTitle(anyString());
        verify(bookCopiesDAO, never()).searchByIsbn(anyString());
    }

    @Test
    void search_byIsbn_callsCorrectDaoMethodAndCleansQuery() {
        // Given
        String query = " 978-0134685991 ";
        String cleanedIsbn = "9780134685991"; // Strategy should clean this
        when(bookCopiesDAO.searchByIsbn(cleanedIsbn)).thenReturn(Collections.singletonList(testCopy));

        // When
        List<BookCopy> results = searchService.search(query, BookCopySearchService.SearchType.ISBN);

        // Then
        assertThat(results).hasSize(1);
        verify(bookCopiesDAO).searchByIsbn(cleanedIsbn);
        verify(bookCopiesDAO, never()).searchByTitle(anyString());
        verify(bookCopiesDAO, never()).searchByAuthor(anyString());
    }

    @Test
    void smartSearch_withIsbnLikeQuery_usesIsbnStrategy() {
        // Given
        String query = "9780134685991";
        when(bookCopiesDAO.searchByIsbn(query)).thenReturn(Collections.singletonList(testCopy));

        // When
        searchService.smartSearch(query);

        // Then
        verify(bookCopiesDAO).searchByIsbn(query);
    }

    @Test
    void smartSearch_withAuthorLikeQuery_usesAuthorStrategy() {
        // Given
        String query = "Bloch, Joshua";
        when(bookCopiesDAO.searchByAuthor(query)).thenReturn(Collections.singletonList(testCopy));

        // When
        searchService.smartSearch(query);

        // Then
        verify(bookCopiesDAO).searchByAuthor(query);
    }

    @Test
    void search_withQueryTooShort_throwsIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> searchService.search("a", BookCopySearchService.SearchType.TITLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The query length must be at least 2 characters");
    }

    @Test
    void search_withNullQuery_throwsIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> searchService.search(null, BookCopySearchService.SearchType.TITLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The query cannot be empty or null");
    }
}*/
package dev.ltocca.loanranger.service;

import dev.ltocca.loanranger.domainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;
import dev.ltocca.loanranger.service.strategy.BookCopySearchStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookCopySearchServiceTest {

    @Mock
    private BookCopiesDAO bookCopiesDAO;

    @InjectMocks
    private BookCopySearchService bookCopySearchService;

    private BookCopy bookCopy;

    @BeforeEach
    void setUp() {
        bookCopy = new BookCopy();
        bookCopy.setCopyId(100L);
    }

    @Test
    void search_byTitle_callsCorrectDaoMethod() {
        // Given
        String query = "Effective Java";
        when(bookCopiesDAO.searchByTitle(query)).thenReturn(Collections.singletonList(bookCopy));

        // When
        List<BookCopy> results = bookCopySearchService.search(query, BookCopySearchService.SearchType.TITLE);

        // Then
        assertThat(results).hasSize(1);
        verify(bookCopiesDAO).searchByTitle(query);
        verify(bookCopiesDAO, never()).searchByAuthor(anyString());
        verify(bookCopiesDAO, never()).searchByIsbn(anyString());
    }

    @Test
    void search_byAuthor_callsCorrectDaoMethod() {
        // Given
        String query = "Joshua Bloch";
        when(bookCopiesDAO.searchByAuthor(query)).thenReturn(Collections.singletonList(bookCopy));

        // When
        List<BookCopy> results = bookCopySearchService.search(query, BookCopySearchService.SearchType.AUTHOR);

        // Then
        assertThat(results).hasSize(1);
        verify(bookCopiesDAO).searchByAuthor(query);
        verify(bookCopiesDAO, never()).searchByTitle(anyString());
        verify(bookCopiesDAO, never()).searchByIsbn(anyString());
    }

    @Test
    void search_byIsbn_callsCorrectDaoMethodAndCleansQuery() {
        // Given
        String query = " 978-0134685991 ";
        String cleanedIsbn = "9780134685991"; // Strategy should clean this
        when(bookCopiesDAO.searchByIsbn(cleanedIsbn)).thenReturn(Collections.singletonList(bookCopy));

        // When
        List<BookCopy> results = bookCopySearchService.search(query, BookCopySearchService.SearchType.ISBN);

        // Then
        assertThat(results).hasSize(1);
        verify(bookCopiesDAO).searchByIsbn(cleanedIsbn);
        verify(bookCopiesDAO, never()).searchByTitle(anyString());
        verify(bookCopiesDAO, never()).searchByAuthor(anyString());
    }

    @Test
    void smartSearch_withIsbnLikeQuery_usesIsbnStrategy() {
        // Given
        String query = "9780134685991";
        when(bookCopiesDAO.searchByIsbn(query)).thenReturn(Collections.singletonList(bookCopy));

        // When
        bookCopySearchService.smartSearch(query);

        // Then
        verify(bookCopiesDAO).searchByIsbn(query);
    }

    @Test
    void smartSearch_withAuthorLikeQuery_usesAuthorStrategy() {
        // Given
        String query = "Bloch, Joshua";
        when(bookCopiesDAO.searchByAuthor(query)).thenReturn(Collections.singletonList(bookCopy));

        // When
        bookCopySearchService.smartSearch(query);

        // Then
        verify(bookCopiesDAO).searchByAuthor(query);
    }

    @Test
    void search_withQueryTooShort_throwsIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> bookCopySearchService.search("a", BookCopySearchService.SearchType.TITLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The query length must be at least 2 characters");
    }


    @Test
    void search_byTitle_callsDaoSearchByTitle() {
        List<BookCopy> searchResults = List.of(bookCopy);
        when(bookCopiesDAO.searchByTitle("Test Book")).thenReturn(searchResults);

        List<BookCopy> result = bookCopySearchService.search("Test Book", BookCopySearchService.SearchType.TITLE);

        verify(bookCopiesDAO).searchByTitle("Test Book");
        assertThat(result).isEqualTo(searchResults);
    }

    @Test
    void search_byAuthor_callsDaoSearchByAuthor() {
        List<BookCopy> searchResults = List.of(bookCopy);
        when(bookCopiesDAO.searchByAuthor("Test Author")).thenReturn(searchResults);

        List<BookCopy> result = bookCopySearchService.search("Test Author", BookCopySearchService.SearchType.AUTHOR);

        verify(bookCopiesDAO).searchByAuthor("Test Author");
        assertThat(result).isEqualTo(searchResults);
    }

    @Test
    void search_byIsbn_callsDaoSearchByIsbn() {
        List<BookCopy> searchResults = List.of(bookCopy);
        when(bookCopiesDAO.searchByIsbn("9780123456789")).thenReturn(searchResults);

        List<BookCopy> result = bookCopySearchService.search("9780123456789", BookCopySearchService.SearchType.ISBN);

        verify(bookCopiesDAO).searchByIsbn("9780123456789");
        assertThat(result).isEqualTo(searchResults);
    }

    @Test
    void search_withNullQuery_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> bookCopySearchService.search(null, BookCopySearchService.SearchType.TITLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The query cannot be empty or null");
    }

    @Test
    void search_withEmptyQuery_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> bookCopySearchService.search("", BookCopySearchService.SearchType.TITLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The query cannot be empty or null");
    }

    @Test
    void search_withQueryShorterThanMinimum_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> bookCopySearchService.search("a", BookCopySearchService.SearchType.TITLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The query length must be at least");
    }

    @Test
    void smartSearch_withIsbnLikeString_callsDaoSearchByIsbn() {
        List<BookCopy> searchResults = List.of(bookCopy);
        when(bookCopiesDAO.searchByIsbn("9780123456789")).thenReturn(searchResults);

        List<BookCopy> result = bookCopySearchService.smartSearch("9780123456789");

        verify(bookCopiesDAO).searchByIsbn("9780123456789");
        assertThat(result).isEqualTo(searchResults);
    }

    @Test
    void smartSearch_withAuthorLikeString_callsDaoSearchByAuthor() {
        List<BookCopy> searchResults = List.of(bookCopy);
        when(bookCopiesDAO.searchByAuthor("Lastname, Firstname")).thenReturn(searchResults);

        List<BookCopy> result = bookCopySearchService.smartSearch("Lastname, Firstname");

        verify(bookCopiesDAO).searchByAuthor("Lastname, Firstname");
        assertThat(result).isEqualTo(searchResults);
    }


    @Test
    void smartSearch_withComplexQuery_usesFullTextSearch() {
        String query = "Patterns";

        List<BookCopy> titleResults = List.of(bookCopy);
        List<BookCopy> authorResults = List.of();
        List<BookCopy> isbnResults = List.of();

        when(bookCopiesDAO.searchByTitle(query.toLowerCase())).thenReturn(titleResults);
        when(bookCopiesDAO.searchByAuthor(query.toLowerCase())).thenReturn(authorResults);
        when(bookCopiesDAO.searchByIsbn(query.toLowerCase())).thenReturn(isbnResults);

        List<BookCopy> result = bookCopySearchService.smartSearch(query);

        verify(bookCopiesDAO).searchByTitle(query.toLowerCase());
        verify(bookCopiesDAO).searchByAuthor(query.toLowerCase());
        verify(bookCopiesDAO).searchByIsbn(query.toLowerCase());
        assertThat(result).hasSize(1);
    }

    @Test
    void searchAvailableOnly_filtersOutUnavailableBooks() {
        BookCopy availableCopy = new BookCopy();
        availableCopy.setCopyId(100L);
        availableCopy.setState(new dev.ltocca.loanranger.domainModel.State.AvailableState());

        BookCopy unavailableCopy = new BookCopy();
        unavailableCopy.setCopyId(101L);
        unavailableCopy.setState(new dev.ltocca.loanranger.domainModel.State.LoanedState());

        List<BookCopy> mixedResults = List.of(availableCopy, unavailableCopy);
        when(bookCopiesDAO.searchByTitle("Test")).thenReturn(mixedResults);

        List<BookCopy> result = bookCopySearchService.searchAvailableOnly("Test", BookCopySearchService.SearchType.TITLE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCopyId()).isEqualTo(100L);
    }


    @Test
    void getAllStrategyDescriptions_returnsAllDescriptions() {
        var descriptions = bookCopySearchService.getAllStrategyDescriptions();

        assertThat(descriptions).isNotEmpty();
        assertThat(descriptions).containsKeys(
                BookCopySearchService.SearchType.TITLE,
                BookCopySearchService.SearchType.AUTHOR,
                BookCopySearchService.SearchType.ISBN,
                BookCopySearchService.SearchType.FULL_TEXT,
                BookCopySearchService.SearchType.AVAILABLE_ONLY
        );
    }
}