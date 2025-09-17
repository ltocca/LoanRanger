package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.DomainModel.Member;
import dev.ltocca.loanranger.DomainModel.User;
import dev.ltocca.loanranger.ORM.UserDAO;
import dev.ltocca.loanranger.Util.PasswordHasher;

import java.sql.SQLException;

public class MemberAccountController {
    private final Member member;
    private final UserDAO userDAO;

    public MemberAccountController(Member member) throws SQLException {
        this.member = member;
        this.userDAO = new UserDAO();
    }

    public void changeUsername(String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: the new username cannot be null or empty");
        }
        if (newUsername.equals(this.member.getUsername())) {
            throw new IllegalArgumentException("Error: the new username cannot be the old one");
        }
        try {
            if (this.userDAO.findUserByUsername(newUsername).isPresent()) {
                throw new IllegalArgumentException("Error: the username already exists");
            }
            this.member.setUsername(newUsername);
            this.userDAO.updateUsername(this.member.getId(), newUsername.trim());
        } catch (Exception e) {
            throw new RuntimeException("Error updating username: " + e.getMessage());
        }
    }

    public void changeEmail(String newEmail){
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: the new email cannot be null or empty");
        }
        if (!newEmail.trim().contains("@")){
            throw new IllegalArgumentException("Error, invalid address: it must contain an @");
        }
        if (newEmail.trim().equals(this.member.getEmail())) {
            throw new IllegalArgumentException("Error: the new email cannot be the old one");
        }
        try {
            if (this.userDAO.getUserByEmail(newEmail).isPresent()){
                throw new IllegalArgumentException("Error: this email has already been used by a user");
            }
            this.member.setEmail(newEmail);
            this.userDAO.updateEmail(this.member.getId(), newEmail.trim());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void changePassword(String currentPassword, String newPassword){
        if (newPassword == null || newPassword.trim().length() < 8) {
            throw new IllegalArgumentException("Error: the new password cannot be null or empty, at least 8 characters");
        }
        if (newPassword.equals(this.member.getPassword())) {
            throw new IllegalArgumentException("Error: the new email cannot be the old one");
        }
        try {
            if (!PasswordHasher.check(currentPassword, this.member.getPassword()) || currentPassword == null) {
                throw new IllegalArgumentException("Error: inserted incorrect current password");
            }
            if (!PasswordHasher.check(newPassword, this.member.getPassword())){
                throw new IllegalArgumentException("Error: you have inserted your old password as the new one");
            }
            String hashedPassword = PasswordHasher.hash(newPassword);
            this.userDAO.updatePassword(this.member.getId(), hashedPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAccount(String password){
        // TODO: finish delete account method, probably need to check if reservations placed and loans are active
    }
}
