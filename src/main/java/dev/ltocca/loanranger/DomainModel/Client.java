package dev.ltocca.loanranger.DomainModel;

public class Client extends User {
    public Client() {
        super();
        setRole("CLIENT");
    }

    public Client(String name, String password, String email) {
        super(name, email, password, "CLIENT");
    }
}

// NOTE: maybe change class to Member, as usually a person has a membership card to a public library