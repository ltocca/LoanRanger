package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.DomainModel.Member;
import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.ORM.BookCopiesDAO;

import java.sql.SQLException;
import java.util.Optional;

public class MemberBookController {
    private final Member member;
    private final LibraryFacade facade;

    public MemberBookController(Member member, LibraryFacade facade) {
        this.member = member;
        this.facade = facade;
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
}
