package dev.ltocca.loanranger.DomainModel;

import dev.ltocca.loanranger.BusinessLogic.Observer.BookObserver;
import dev.ltocca.loanranger.BusinessLogic.Observer.EventObserver;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor

public class Member extends User implements BookObserver, EventObserver {

    public Member(String email, String password) {
        super(email, password);
        setRole(UserRole.MEMBER);
    }

    @Override
    public void onBookAvailable(Book book) {
        System.out.println("Notification for Member " + getUsername() + ": The book '" + book.getTitle() + "' you reserved is now available.");
    }

    @Override
    public void onEventUpdate(Event event) {
        System.out.println("Notification for Member " + getUsername() + ": The event '" + event.getTitle() + "' has new information.");
    }
}