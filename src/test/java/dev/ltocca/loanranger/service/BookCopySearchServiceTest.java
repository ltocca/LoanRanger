package dev.ltocca.loanranger.service;

import dev.ltocca.loanranger.domainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
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
}