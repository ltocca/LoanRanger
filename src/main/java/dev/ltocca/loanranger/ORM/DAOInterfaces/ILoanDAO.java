package dev.ltocca.loanranger.ORM.DAOInterfaces;

import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.DomainModel.Library;
import dev.ltocca.loanranger.DomainModel.Loan;
import dev.ltocca.loanranger.DomainModel.Member;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ILoanDAO {

    Loan createLoan(Loan loan);

    Loan createLoan(BookCopy bookCopy, Member member);

    Optional<Loan> getLoanById(Long id);

    Optional<Loan> getLoanByBookCopy(BookCopy bookCopy);

    Optional<Loan> getLoanByBookCopyId(Long bookCopyId);

    void updateLoan(Loan loan);

    void updateDueDate(Long id);

    void updateDueDate(Long id, int days);

    void updateDueDate(Long id, LocalDate dueDate);

    List<Loan> findLoansByMember(Member member);

    List<Loan> findActiveLoansByMember(Member member);

    List<Loan> findActiveLoansByLibrary(Library library);

    List<Loan> findOverdueLoans();

    List<Loan> findMemberOverdueLoans(Long id);

    List<Loan> listAllLoansByLibrary(Library workLibrary);

    void deleteLoan(Long id);

    void deleteLoan(Loan loan);

}