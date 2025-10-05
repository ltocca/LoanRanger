package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.ORM.*;
import dev.ltocca.loanranger.util.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class MemberAccountController {
    private final UserDAO userDAO;
    private final LoanDAO loanDAO;
    private final ReservationDAO reservationDAO;

    @Autowired
    public MemberAccountController(UserDAO userDAO, LoanDAO loanDAO, ReservationDAO reservationDAO) {
        this.userDAO = userDAO;
        this.loanDAO = loanDAO;
        this.reservationDAO = reservationDAO;
    }

    public void changeUsername(Member member, String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: the new username cannot be null or empty");
        }
        if (newUsername.equals(member.getUsername())) {
            throw new IllegalArgumentException("Error: the new username cannot be the old one");
        }
        try {
            if (this.userDAO.findUserByUsername(newUsername).isPresent()) {
                throw new IllegalArgumentException("Error: the username already exists");
            }
            member.setUsername(newUsername);
            this.userDAO.updateUsername(member.getId(), newUsername.trim());
        } catch (Exception e) {
            throw new RuntimeException("Error updating username: " + e.getMessage());
        }
    }

    public void changeEmail(Member member, String newEmail){
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: the new email cannot be null or empty");
        }
        if (!newEmail.trim().contains("@")){
            throw new IllegalArgumentException("Error, invalid address: it must contain an @");
        }
        if (newEmail.trim().equals(member.getEmail())) {
            throw new IllegalArgumentException("Error: the new email cannot be the old one");
        }
        try {
            if (this.userDAO.getUserByEmail(newEmail).isPresent()){
                throw new IllegalArgumentException("Error: this email has already been used by a user");
            }
            member.setEmail(newEmail);
            this.userDAO.updateEmail(member.getId(), newEmail.trim());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void changePassword(Member member, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 8) {
            throw new IllegalArgumentException("Error: the new password cannot be null or empty, at least 8 characters");
        }
        if (newPassword.equals(member.getPassword())) {
            throw new IllegalArgumentException("Error: the new password  cannot be the old one");
        }
        try {
            if (!PasswordHasher.check(currentPassword, member.getPassword()) || currentPassword == null) {
                throw new IllegalArgumentException("Error: inserted incorrect current password");
            }
            if (PasswordHasher.check(newPassword, member.getPassword())) {
                throw new IllegalArgumentException("Error: you have inserted your old password as the new one");
            }
            String hashedPassword = PasswordHasher.hash(newPassword);
            this.userDAO.updatePassword(member.getId(), hashedPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAccount(Member member, String password){
        // FIXME: maybe it is too risky
        try{
            if (!PasswordHasher.check(password, member.getPassword())) {
                System.err.println("Incorrect password");
                return;
            }
            List<Loan> activeLoans = loanDAO.findActiveLoansByMember(member);
            if (!activeLoans.isEmpty()) {
                System.err.println("Account cannot be deleted. You have active loans.");
                return;
            }
            // Check for pending reservations
            List<Reservation> reservations = reservationDAO.findMemberReservations(member);
            for (Reservation reservation : reservations) {
                if (reservation.getStatus() == ReservationStatus.PENDING) {
                    System.err.println("Account cannot be deleted. You have pending reservations. Please cancel them first.");
                    return; // Exit early, no deletion
                }
            }
            userDAO.deleteUser(member.getId());
            System.err.println("Account has been deleted.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
