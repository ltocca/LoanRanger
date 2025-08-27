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