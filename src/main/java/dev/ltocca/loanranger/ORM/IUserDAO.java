package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.DomainModel.User;
import java.util.List;
import java.util.Optional;

public interface IUserDAO {
    User createUser(User user);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    List<User> getAllUsers();
    List<User> getUsersByRole(String role);
    Optional<User> findUserByUsername(String username);

    void updateUser(User user);
    void deleteUser(Long id);
    // TODO: add update username, password, library
}