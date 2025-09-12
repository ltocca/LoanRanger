package dev.ltocca.loanranger.BusinessLogic.strategy;

import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;

import java.util.List;

public sealed interface BookCopySearchStrategy
        permits TitleSearchStrategy, AuthorSearchStrategy, IsbnSearchStrategy, FullTextSearchStrategy { // suggested bu IDE

    List<BookCopy> search(String query, BookCopiesDAO bookCopiesDAO);

    default int getMinQueryLength() {
        return 1;
    }
}

// TODO: Probably need to implement more methods in the BookCopiesDAO