package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.DomainModel.Book;
import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.DomainModel.Library;

import java.util.List;
import java.util.Optional;

public interface IBookCopiesDAO {
    BookCopy createCopy(BookCopy bookCopy);
    Optional<BookCopy> getCopyById(int id);
    void updateCopyStatus(BookCopy bookCopy);
    void deleteCopy(int id);

    List<BookCopy> findAllBookCopies(Book book);
    List<BookCopy> findLibraryCopies(Library library);
    List<BookCopy> findAvailableBookCopies(Book book);
}
