package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.DomainModel.Admin;
import dev.ltocca.loanranger.ORM.UserDAO;
import dev.ltocca.loanranger.Util.PasswordHasher;

import java.sql.SQLException;

public class AdminAccountController {
    private final Admin admin;
    private final UserDAO userDAO;

    public AdminAccountController(Admin admin) throws SQLException {
        this.admin = admin;
        this.userDAO = new UserDAO();
    }

    public void changeEmail(String newEmail){
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: the new email cannot be null or empty");
        }
        if (!newEmail.trim().contains("@")){
            throw new IllegalArgumentException("Error, invalid address: it must contain an @");
        }
        if (newEmail.trim().equals(this.admin.getEmail())) {
            throw new IllegalArgumentException("Error: the new email cannot be the old one");
        }
        try {
            if (this.userDAO.getUserByEmail(newEmail).isPresent()){
                throw new IllegalArgumentException("Error: this email has already been used by a user");
            }
            this.admin.setEmail(newEmail);
            this.userDAO.updateEmail(this.admin.getId(), newEmail.trim());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void changePassword(String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 8) {
            throw new IllegalArgumentException("Error: the new password cannot be null or empty, at least 8 characters");
        }
        if (newPassword.equals(this.admin.getPassword())) {
            throw new IllegalArgumentException("Error: the new password  cannot be the old one");
        }
        try {
            if (!PasswordHasher.check(currentPassword, this.admin.getPassword()) || currentPassword == null) {
                throw new IllegalArgumentException("Error: inserted incorrect current password");
            }
            if (PasswordHasher.check(newPassword, this.admin.getPassword())) {
                throw new IllegalArgumentException("Error: you have inserted your old password as the new one");
            }
            String hashedPassword = PasswordHasher.hash(newPassword);
            this.userDAO.updatePassword(this.admin.getId(), hashedPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}