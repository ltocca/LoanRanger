package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.DomainModel.Library;
import dev.ltocca.loanranger.DomainModel.User;
import dev.ltocca.loanranger.DomainModel.UserRole;
import dev.ltocca.loanranger.ORM.UserDAO;
import dev.ltocca.loanranger.Util.PasswordHasher;

import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    public LoginController(){}

    public Optional<User> login(String email, String password) throws SQLException {
        UserDAO userDAO = new UserDAO();
        Optional<User> userOptional = userDAO.getUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (PasswordHasher.check(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        // TODO: print exception/error to signal that the user is not found
        return Optional.empty();
    }

    public User register(UserRole role, String username, String name, String email, String password, Library workLibrary) throws SQLException {
        UserDAO userDAO = new UserDAO();
        User newUser = UserFactory.createUser(role, username, name, email, password, workLibrary);
        return userDAO.createUser(newUser);
    }
}