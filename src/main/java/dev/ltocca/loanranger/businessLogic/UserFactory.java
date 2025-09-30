package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.util.PasswordHasher;

public class UserFactory {
    public static User createUser(UserRole role, String username, String name, String email, String password, Library workLibrary) {
        String hashedPassword = PasswordHasher.hash(password);
        return switch (role) { // suggested by IntelliJ IDE, enhanced switch
            case MEMBER -> new Member(username, name, email, hashedPassword);
            case LIBRARIAN -> {
                if (workLibrary == null ||workLibrary.getId() == null) {
                    throw new IllegalArgumentException("WorkingLibrary or its ID is null: you must provide a correct Library!");
                }
                yield new Librarian(username, hashedPassword, name, email, workLibrary);
            }
            case ADMIN -> new Admin(username, hashedPassword, name, email);
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
    }
}
