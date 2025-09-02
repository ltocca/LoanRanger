package dev.ltocca.loanranger.DomainModel;

import dev.ltocca.loanranger.Util.LoanDueException;
import lombok.Getter;
import lombok.Setter;

import java.time.temporal.ChronoUnit;

import java.time.LocalDate;

@Getter
@Setter


public class Loan {
    private Long id;
    private Long bookId;
    private Long userId;
    private Long libraryId; // Which library processed the loan
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private boolean isReturned;

    public Loan() {
        this.loanDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusDays(30);
        this.isReturned = false;
    }

    public Loan(Long bookId, Long userId, Long libraryId) {
        this();
        this.bookId = bookId;
        this.userId = userId;
        this.libraryId = libraryId;
    }

    public int getRemainingDays() {
        LocalDate today = LocalDate.now();

        if (isReturned) {
            throw new LoanDueException("Loan already returned.");
        }

        if (dueDate == null) {
            throw new LoanDueException("Due date is not set for this loan.");
        }

        long daysLeft = ChronoUnit.DAYS.between(today, dueDate);

        if (daysLeft < 0) {
            throw new LoanDueException("Loan is overdue by " + Math.abs(daysLeft) + " day(s).");
        }

        if (daysLeft == 0) {
            throw new LoanDueException("Loan is due today.");
        }

        return (int) daysLeft;
    }

}
