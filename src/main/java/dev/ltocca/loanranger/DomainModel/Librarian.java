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
        this.setRole(UserRole.LIBRARIAN);
    }

    public Librarian(String username, String password, String email, Library workLibrary) {
        super(username, password);
        this.setEmail(email);
        this.setWorkLibrary(workLibrary);
        this.setRole(UserRole.LIBRARIAN);
    }
    public Librarian(String username, String password, String name, String email, Library workLibrary) {
        super(username, password);
        this.setName(name);
        this.setEmail(email);
        this.setWorkLibrary(workLibrary);
        this.setRole(UserRole.LIBRARIAN);
    }

    public Librarian(Long userId, String username, String name, String email, String password, Library workLibrary) {
        super(userId, username, name, email, password, UserRole.LIBRARIAN);
        this.workLibrary = workLibrary;
    }

    public void setWorkLibrary(Library workLibrary) {
        if (workLibrary == null || workLibrary.getId() == null) { // a librarian must have a Library!
            throw new IllegalArgumentException("A Librarian's work library and its ID cannot be null.");
        }
        this.workLibrary = workLibrary;
    }
}