package dev.ltocca.loanranger.domainModel;

import lombok.NoArgsConstructor;

@NoArgsConstructor

public class Admin extends User {
    public Admin(Long id, String username, String name, String email, String password) {
        super(id, name, username, email, password, UserRole.ADMIN);
    }

    public Admin(Long id, String username, String Password) {
        super(id, username, Password);
        this.setRole(UserRole.ADMIN);
    }
    public Admin(String username, String password, String name, String email) {
        super(username, password);
        this.setName(name);
        this.setEmail(email);
        this.setRole(UserRole.ADMIN);
    }
}