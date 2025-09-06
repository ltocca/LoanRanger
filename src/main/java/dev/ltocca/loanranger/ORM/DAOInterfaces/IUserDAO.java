package dev.ltocca.loanranger.ORM.DAOInterfaces;

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

    //TODO: ADD LOGIN FUNCTION and password hash

    void updateUsername(Long id, String newUsername);
    void updatePassword(Long id, String newPassword);
    void librarianUpdateLibrary(User user, Long libraryId);

    void updateUser(User user);
    void deleteUser(Long id);
}