package dev.ltocca.loanranger.DomainModel;

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
}