package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
        import dev.ltocca.loanranger.util.PasswordHasher;

public class UserFactory {

    public static User createUser(UserRole role, String username, String name, String email, String password, Library workLibrary) {

        if (role == null) {
            throw new IllegalArgumentException("Invalid role: role cannot be null");
        }

        // Trim input fields
        String trimmedUsername = username != null ? username.trim() : null;
        String trimmedName = name != null ? name.trim() : null;
        String trimmedEmail = email != null ? email.trim() : null;

        if (trimmedUsername == null || trimmedUsername.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (trimmedName == null || trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (trimmedEmail == null || trimmedEmail.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        String hashedPassword = PasswordHasher.hash(password);

        return switch (role) {
            case MEMBER -> new Member(trimmedUsername, trimmedName, trimmedEmail, hashedPassword);
            case ADMIN -> new Admin(trimmedUsername, hashedPassword, trimmedName, trimmedEmail);
            case LIBRARIAN -> {
                if (workLibrary == null || workLibrary.getId() == null) {
                    throw new IllegalArgumentException("WorkingLibrary or its ID is null");
                }
                yield new Librarian(trimmedUsername, hashedPassword, trimmedName, trimmedEmail, workLibrary);
            }
            default -> throw new IllegalArgumentException("Unsupported role: " + role);
        };
    }
}