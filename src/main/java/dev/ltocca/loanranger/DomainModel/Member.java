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
    public void onBookCopyAvailable(BookCopy bookCopy) { // Implement new method
        System.out.println("Notification for Member " + getUsername() + ": The book copy '" + bookCopy.getBook().getTitle() + "' (ID: " + bookCopy.getCopyId() + ") you were watching is now available.");
        // Integrate EmailService
        EmailService emailService = new EmailService(); // Consider dependency injection or a singleton for EmailService
        String subject = "Book Copy Available - " + bookCopy.getBook().getTitle();
        String message = "Dear " + this.getName() + ",\n\n" +
                "The book '" + bookCopy.getBook().getTitle() + "' by " + bookCopy.getBook().getAuthor() + " (Copy ID: " + bookCopy.getCopyId() + ") that you were watching is now available for reservation at the library.\n" +
                "Hurry up and reserve it as soon as possible!\n\n" +
                "Thank you for using LoanRanger!";
        try {
            emailService.sendEmail(this.getEmail(), subject, message);
            System.out.println("Email notification sent to " + this.getEmail() + " for watched book copy: " + bookCopy.getCopyId());
        } catch (Exception e) {
            System.err.println("Failed to send email notification to " + this.getEmail() + " for watched book copy: " + bookCopy.getCopyId() + ". Error: " + e.getMessage());
            // Log the error appropriately
        }
    }
}