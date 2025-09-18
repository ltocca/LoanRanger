package dev.ltocca.loanranger.DomainModel;

import dev.ltocca.loanranger.BusinessLogic.Observer.BookObserver;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor

public class Member extends User implements BookObserver {

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
    public void onBookAvailable(Book book) {
        System.out.println("Notification for Member " + getUsername() + ": The book '" + book.getTitle() + "' you reserved is now available.");
    }
}