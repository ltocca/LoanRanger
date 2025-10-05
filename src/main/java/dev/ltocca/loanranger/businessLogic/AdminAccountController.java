package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.Admin;
import dev.ltocca.loanranger.ORM.UserDAO;
import dev.ltocca.loanranger.util.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class AdminAccountController {
    private final UserDAO userDAO;

    @Autowired
    public AdminAccountController(UserDAO userDAO) throws SQLException {
        this.userDAO = userDAO;
    }

    public void changeEmail(Admin admin, String newEmail){
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: the new email cannot be null or empty");
        }
        if (!newEmail.trim().contains("@")){
            throw new IllegalArgumentException("Error, invalid address: it must contain an @");
        }
        if (newEmail.trim().equals(admin.getEmail())) {
            throw new IllegalArgumentException("Error: the new email cannot be the old one");
        }
        try {
            if (this.userDAO.getUserByEmail(newEmail).isPresent()){
                throw new IllegalArgumentException("Error: this email has already been used by a user");
            }
            admin.setEmail(newEmail);
            this.userDAO.updateEmail(admin.getId(), newEmail.trim());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void changePassword(Admin admin, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 8) {
            throw new IllegalArgumentException("Error: the new password cannot be null or empty, at least 8 characters");
        }
        if (newPassword.equals(admin.getPassword())) {
            throw new IllegalArgumentException("Error: the new password  cannot be the old one");
        }
        try {
            if (!PasswordHasher.check(currentPassword, admin.getPassword()) || currentPassword == null) {
                throw new IllegalArgumentException("Error: inserted incorrect current password");
            }
            if (PasswordHasher.check(newPassword, admin.getPassword())) {
                throw new IllegalArgumentException("Error: you have inserted your old password as the new one");
            }
            String hashedPassword = PasswordHasher.hash(newPassword);
            this.userDAO.updatePassword(admin.getId(), hashedPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}