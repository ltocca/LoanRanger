package dev.ltocca.loanranger.DomainModel;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Librarian extends User {
    private Library workLibrary; // library where they work

    // added constructor with no arguments for the DAO
    public Librarian() {
        super();
        setRole(UserRole.LIBRARIAN);
    }

    public Librarian(Long userId, String firstName, String lastName, String email, String password, Library library) {
        super(userId, firstName, lastName, email, password, UserRole.LIBRARIAN);
        this.workLibrary = library;
    }

    public void setWorkLibrary(Library workLibrary) {
        if (workLibrary == null || workLibrary.getId() == null) { // a librarian must have a Library!
            throw new IllegalArgumentException("A Librarian's work library and its ID cannot be null.");
        }
        this.workLibrary = workLibrary;
    }
}