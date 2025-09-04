package dev.ltocca.loanranger.DomainModel;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Librarian extends User {
    private Library workLibrary; // library where they work

    public Librarian(Long userId, String firstName, String lastName, String email, String password, Library library) {
        super(userId, firstName, lastName, email, password, UserRole.LIBRARIAN);
        this.workLibrary = library;
    }

}