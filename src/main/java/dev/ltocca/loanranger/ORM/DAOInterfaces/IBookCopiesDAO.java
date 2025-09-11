package dev.ltocca.loanranger.ORM.DAOInterfaces;

import dev.ltocca.loanranger.DomainModel.Book;
import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.DomainModel.BookStatus;
import dev.ltocca.loanranger.DomainModel.Library;

import java.util.List;
import java.util.Optional;

public interface IBookCopiesDAO {
    BookCopy createCopy(BookCopy bookCopy);

    Optional<BookCopy> getCopyById(Long id);

    List<BookCopy> getAllBookCopies();

    List<BookCopy> searchByTitle(String titleFragment);

    List<BookCopy> searchByAuthor(String authorFragment);

    List<BookCopy> searchByIsbn(String isbnFragment);

    void updateCopyStatus(BookCopy bookCopy);

    void updateCopyStatus(Long copyId, BookStatus status);

    void deleteCopy(Long id);

    void deleteCopy(BookCopy bookCopy);

    List<BookCopy> findAllBookCopies(Book book);

    List<BookCopy> findLibraryCopies(Library library);

    List<BookCopy> findAvailableBookCopies(Book book);
}
