package dev.ltocca.loanranger.ORM.DAOInterfaces;

import dev.ltocca.loanranger.domainModel.Book;

import java.util.List;
import java.util.Optional;

public interface IBookDAO {
    Book createBook(Book book);

    Optional<Book> getBookByIsbn(String isbn);

    Optional<Book> getBookByTitle(String title);

    List<Book> getAllBooks();

    List<Book> findBooksByAuthor(String author);

    List<Book> findBooksByPublicationYear(int publicationYear);

    List<Book> findBookByIsbn(String isbn); // parial isbn?

    void deleteBook(String isbn);
    void deleteBook(Book book);
}
