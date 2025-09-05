package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.DomainModel.Loan;
import dev.ltocca.loanranger.DomainModel.Member;

import java.util.List;
import java.util.Optional;

public interface ILoanDAO {

    Loan createLoan(Loan loan);

    Optional<Loan> getLoanById(Long id);

    void updateLoan(Loan loan);

    List<Loan> findLoansByMember(Member member);

    List<Loan> findActiveLoansByMember(Member member);

    List<Loan> findOverdueLoans();
}