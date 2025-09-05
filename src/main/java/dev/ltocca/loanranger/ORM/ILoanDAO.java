package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.DomainModel.BookCopy;
import dev.ltocca.loanranger.DomainModel.Loan;
import dev.ltocca.loanranger.DomainModel.Member;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ILoanDAO {

    Loan createLoan(Loan loan);

    Loan createLoan(BookCopy bookCopy, Member member);

    Optional<Loan> getLoanById(Long id);

    void updateDueDate(Long id);
    void updateDueDate(Long id, int days);
    void updateDueDate(Long id, LocalDate dueDate);

    List<Loan> findLoansByMember(Member member);

    List<Loan> findActiveLoansByMember(Member member);

    List<Loan> findOverdueLoans();
}