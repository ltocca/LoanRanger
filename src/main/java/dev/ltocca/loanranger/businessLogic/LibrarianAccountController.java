package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.Librarian;
import dev.ltocca.loanranger.ORM.UserDAO;
import dev.ltocca.loanranger.util.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
@Transactional
public class LibrarianAccountController {
    private final UserDAO userDAO;

    @Autowired
    public LibrarianAccountController(UserDAO userDAO) throws SQLException {
        this.userDAO = userDAO;
    }

    public void changeUsername(Librarian librarian, String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: The new username cannot be null or empty.");
        }
        if (newUsername.trim().equals(librarian.getUsername())) {
            throw new IllegalArgumentException("Error: The new username cannot be the same as the old one.");
        }
        try {
            if (this.userDAO.findUserByUsername(newUsername).isPresent()) {
                throw new IllegalArgumentException("Error: This username is already taken by another user.");
            }
            librarian.setUsername(newUsername.trim());
            this.userDAO.updateUsername(librarian.getId(), newUsername.trim());
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("A database error occurred while updating the username.", e);
        }
    }

    public void changeEmail(Librarian librarian, String newEmail) {
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: The new email cannot be null or empty.");
        }
        if (!newEmail.trim().contains("@")) {
            throw new IllegalArgumentException("Error: Invalid email address format. It must contain an '@'.");
        }
        if (newEmail.trim().equalsIgnoreCase(librarian.getEmail())) {
            throw new IllegalArgumentException("Error: The new email cannot be the same as the old one.");
        }
        try {
            if (this.userDAO.getUserByEmail(newEmail).isPresent()) {
                throw new IllegalArgumentException("Error: This email address is already in use by another account.");
            }
            librarian.setEmail(newEmail.trim());
            this.userDAO.updateEmail(librarian.getId(), newEmail.trim());
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
        }
    }

    public void changePassword(Librarian librarian, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new IllegalArgumentException("Error: The new password must be at least 6 characters long.");
        }
        if (newPassword.trim().equals(currentPassword.trim())) {
            throw new IllegalArgumentException("Error: The new password cannot be the same as the old one.");
        }

        if (!PasswordHasher.check(currentPassword, librarian.getPassword())) {
            throw new IllegalArgumentException("Error: The current password you entered is incorrect.");
        }

        try {
            String hashedPassword = PasswordHasher.hash(newPassword);
            this.userDAO.updatePassword(librarian.getId(), hashedPassword);
            librarian.setPassword(hashedPassword);
        } catch (Exception e) {
            throw new RuntimeException("A database error occurred while updating the password.", e);
        }
    }
}