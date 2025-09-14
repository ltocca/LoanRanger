package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.BusinessLogic.strategy.BookCopySearchStrategy;
import dev.ltocca.loanranger.DomainModel.Member;
import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;
import dev.ltocca.loanranger.BusinessLogic.BookCopySearchService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MemberBookController {
    private final Member member;
    private final LibraryFacade facade;
    private final BookCopySearchService searchService;

    public MemberBookController(Member member, LibraryFacade facade, BookCopySearchService service) {
        this.member = member;
        this.facade = facade;
        this.searchService = service;
    }

    public void borrowBookCopy(Long copyId) throws SQLException {
        BookCopiesDAO bookCopiesDAO = new BookCopiesDAO();
        Optional<BookCopy> bookCopyOpt = bookCopiesDAO.getCopyById(copyId);

        if (bookCopyOpt.isEmpty()) {
            System.err.printf("There is no copy present with this id %s%n", copyId); // suggested by IntelliJ
            return;
        }
        boolean borrowSuccess = facade.borrowBook(this.member, bookCopyOpt.get());
        if (!borrowSuccess) {
            System.err.println("Copy borrowing failed.");
        }
    }

    public void borrowBookCopy(BookCopy copy) throws SQLException {
        BookCopiesDAO bookCopiesDAO = new BookCopiesDAO();
        Optional<BookCopy> bookCopyOpt = bookCopiesDAO.getCopyById(copy.getCopyId());

    }

    public void returnCopy(Long copyId) throws SQLException {
        boolean success = facade.returnBook(copyId);
        if (!success) {
            System.out.println("Returning the book failed. Please check the copy ID.");
        }
    }

    public List<BookCopy> searchBooksByTitle(String title) {
        try {
            return searchService.search(title, BookCopySearchService.SearchType.TITLE);
        } catch (IllegalArgumentException e) {
            System.err.println("Title search error: " + e.getMessage());
            return List.of();
        }
    }

    public List<BookCopy> searchBooksByAuthor(String author) {
        try {
            return searchService.search(author, BookCopySearchService.SearchType.AUTHOR);
        } catch (IllegalArgumentException e) {
            System.err.println("Author search error: " + e.getMessage());
            return List.of();
        }
    }

    public List<BookCopy> searchBooksByIsbn(String isbn) {
        try {
            return searchService.search(isbn, BookCopySearchService.SearchType.ISBN);
        } catch (IllegalArgumentException e) {
            System.err.println("ISBN search error: " + e.getMessage());
            return List.of();
        }
    }

}
