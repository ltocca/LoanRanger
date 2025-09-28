package dev.ltocca.loanranger.DomainModel;

import dev.ltocca.loanranger.BusinessLogic.Observer.BookCopyObserver;
import dev.ltocca.loanranger.BusinessLogic.EmailService;
import lombok.NoArgsConstructor;

@NoArgsConstructor

public class Member extends User implements BookCopyObserver {

    public Member(String username, String email, String password) {
        super(username, password);
        this.setEmail(email);
        this.setRole(UserRole.MEMBER);
    }

    public Member(String username, String name, String email, String password) {
        super(username, password);
        this.setEmail(email);
        this.setName(name);
        this.setRole(UserRole.MEMBER);
    }

    public Member(Long id, String username, String name, String email, String password) {
        super(id, username, password);
        this.setEmail(email);
        this.setName(name);
        this.setRole(UserRole.MEMBER);
    }

    @Override
    public void onBookCopyAvailable(BookCopy bookCopy) {
        EmailService emailService = new EmailService();
        String subject = "Book Copy Available - " + bookCopy.getBook().getTitle();
        String message = "Dear " + this.getName() + ",\n" +
                "The book '" + bookCopy.getBook().getTitle() + "' by " + bookCopy.getBook().getAuthor() +
                " (Copy ID: " + bookCopy.getCopyId() + ") that you reserved is now available.\n" +
                "Please visit the library " + bookCopy.getLibrary().getName() + " soon to borrow it!";
        emailService.sendEmail(this.getEmail(), subject, message);
    }
}