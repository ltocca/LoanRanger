package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.Library;
import dev.ltocca.loanranger.domainModel.User;
import dev.ltocca.loanranger.domainModel.UserRole;
import dev.ltocca.loanranger.ORM.UserDAO;
import dev.ltocca.loanranger.util.PasswordHasher;

import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    public LoginController(){}

    public Optional<User> login(String email, String password) throws SQLException {
        if (email == null || password == null) {
            System.err.println("Error: No email or password provided!");
            return Optional.empty();
        }
        UserDAO userDAO = new UserDAO();
        Optional<User> userOptional = userDAO.getUserByEmail(email.trim());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (PasswordHasher.check(password.trim(), user.getPassword())) {
                System.out.println("Login successful: welcome " + user.getName()+"!");
                return Optional.of(user);
            }
            System.err.println("Login error: Invalid password!");
        }
        return Optional.empty();
    }

/*
    public Optional<User> login(String username, String password) throws SQLException {
        UserDAO userDAO = new UserDAO();
        Optional<User> userOptional = userDAO.getUserByUsername(username.trim());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (PasswordHasher.check(password.trim(), user.getPassword())) {
                System.out.println("Login successful: welcome " + user.getName()+"!");
                return Optional.of(user);
            }
            System.err.println("Login error: Invalid password!");
        }
        System.err.println("Login error: User not found!");
        return Optional.empty();
    }
*/

    public User register(UserRole role, String username, String name, String email, String password, Library workLibrary) throws SQLException {
        try {
            validateRegistrationParameters(role, username, email, password, workLibrary);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        UserDAO userDAO = new UserDAO();
        User newUser = UserFactory.createUser(role, username, name, email, password, workLibrary);
        return userDAO.createUser(newUser);
    }


    /**
     * @param role
     * @param username
     * @param email
     * @param password
     * @param workLibrary
     * <h1>Simple validation for registration parameters</h1>
     *
     * Added to try registration limitation. Using trim to remove excess space. Inspired by spring boot validation!
     * TODO: first javadoc added, to be modified or removed
     */
    private void validateRegistrationParameters(UserRole role, String username, String email, String password, Library workLibrary) throws IllegalArgumentException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (role == UserRole.LIBRARIAN && workLibrary == null) {
            throw new IllegalArgumentException("Librarian must be assigned to a library");
        }
    }
}