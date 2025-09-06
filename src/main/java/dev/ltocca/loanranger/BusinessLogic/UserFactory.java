package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.DomainModel.*;
import dev.ltocca.loanranger.Util.PasswordHasher;

public class UserFactory {
    public static User createUser(UserRole role, String username, String name, String email, String password, Library workLibrary) {
        String hashedPassword = PasswordHasher.hash(password);
        return switch (role) { // suggested by IntelliJ IDE, enhanced switch
            case MEMBER -> new Member(username, name, email, hashedPassword);
            case LIBRARIAN -> {
                if (workLibrary.getId() == null) {
                    throw new IllegalArgumentException("WorkingLibraryId is null: you must provide a correct Library!");
                }
                yield new Librarian(username, hashedPassword, name, email, workLibrary);
            }
            case ADMIN -> new Admin(username, hashedPassword, name, email);
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
    }
}
