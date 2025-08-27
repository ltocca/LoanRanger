package dev.ltocca.loanranger.DomainModel;

public class Librarian extends User {
    public Librarian() {
        super();
        setRole("LIBRARIAN");
    }

    public Librarian(String name, String email, String password) {
        super(name, email, password, "LIBRARIAN");
    }
}