package dev.ltocca.loanranger.domainModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public abstract class User {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String password;
    private UserRole role;

    public User(Long id, String username, String Password) {
        this();
        this.id = id;
        this.username = username;
        this.password = Password;
        this.role = UserRole.MEMBER;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}