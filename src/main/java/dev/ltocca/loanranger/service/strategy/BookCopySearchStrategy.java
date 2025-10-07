package dev.ltocca.loanranger.service.strategy;

import dev.ltocca.loanranger.domainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;

import java.util.List;

public sealed interface BookCopySearchStrategy
        permits TitleSearchStrategy, AuthorSearchStrategy, IsbnSearchStrategy, FullTextSearchStrategy, AvailableOnlySearchStrategy { // suggested bu IDE

    List<BookCopy> search(String query, BookCopiesDAO bookCopiesDAO);

    String getDescription();

    default int getMinQueryLength() {
        return 1;
    }
}
