package dev.ltocca.loanranger.DomainModel;

import dev.ltocca.loanranger.DomainModel.State.AvailableState;
import dev.ltocca.loanranger.DomainModel.State.LoanedState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor


public class Loan {
    private Long id;
    private BookCopy bookCopy; // the reference to the library is here
    private Member member;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate = null;

    public Loan(BookCopy bookCopy, Member member, LocalDate dueDate) {
        setLoanDate(LocalDate.now());
        this.bookCopy = bookCopy;
        this.bookCopy.loan();
        this.member = member;
        this.dueDate = dueDate;
    }

    public Loan(BookCopy bookCopy, Member member, LocalDate dueDate, LocalDate returnDate) {
        setLoanDate(LocalDate.now());
        this.bookCopy = bookCopy;
        this.member = member;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
    }

    public void renewLoan() {
        dueDate = LocalDate.now().plusDays(30);
    }

    public void renewLoan(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void renewLoan(int days) {
        dueDate = LocalDate.now().plusDays(days);
    }

    public void endLoan(){bookCopy.returnCopy();}

    public Boolean isExpired() {
        return !(getBookCopy().getState() instanceof LoanedState) || LocalDate.now().isAfter(getDueDate());
    }

    public int getRemainingDays() {
        if (isExpired()) {
            return 0;
        } else if (dueDate == null) { // This indicates a data integrity problem, which is an unwanted case.
            throw new IllegalStateException("ERROR: Due date is not set for this loan; possible data corruption");
        }
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, dueDate);
        return (int) days;

    }
}