package dev.ltocca.loanranger.DomainModel;

import lombok.Getter;
import lombok.Setter;

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
}
