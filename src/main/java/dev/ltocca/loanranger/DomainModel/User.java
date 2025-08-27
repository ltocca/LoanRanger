package dev.ltocca.loanranger.DomainModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public abstract class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String role; // type of user
    private Long libraryId;

    public User() {}
    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // TODO: implement a way to generate the id automatically
    // TODO: implement a way to add the password

}
