package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.ORM.LibraryDAO;
import dev.ltocca.loanranger.ORM.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class AdminUsersController {
    private final LibraryDAO libraryDAO;
    private final UserDAO userDAO;
    private final LoginController loginController;

    @Autowired
    public AdminUsersController(LibraryDAO libraryDAO, UserDAO userDAO, LoginController loginController) throws SQLException {
        this.libraryDAO = libraryDAO;
        this.userDAO = userDAO;
        this.loginController = loginController;
    }

    public void registerNewLibrarian(String username, String name, String email, String password, Long libraryId) {
        try {
            Optional<Library> libraryOpt = libraryDAO.getLibraryById(libraryId);
            if (libraryOpt.isEmpty()) {
                System.err.println("Cannot register librarian: Library with ID " + libraryId + " not found.");
                return;
            }
            loginController.register(UserRole.LIBRARIAN, username, name, email, password, libraryOpt.get());
            System.out.println("Librarian '" + username + "' registered successfully.");
        } catch (Exception e) {
            System.err.println("Failed to register new librarian: " + e.getMessage());
        }
    }

    public void deleteUser(Admin admin, Long userId) {
        try {
            if (userId.equals(admin.getId())) {
                System.err.println("Cannot delete your own admin account.");
                return;
            }
            userDAO.deleteUser(userId);
            System.out.println("User with ID " + userId + " deleted successfully.");
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }

    public void assignLibrarianToLibrary(Long librarianId, Long newLibraryId) {
        try {
            Optional<User> userOpt = userDAO.getUserById(librarianId);
            if (userOpt.isEmpty() || !(userOpt.get() instanceof Librarian)) {
                System.err.println("Librarian not found with ID: " + librarianId);
                return;
            }
            userDAO.librarianUpdateLibrary(userOpt.get(), newLibraryId);
            System.out.println("Librarian " + librarianId + " assigned to new library " + newLibraryId);
        } catch (Exception e) {
            System.err.println("Error assigning librarian to library: " + e.getMessage());
        }
    }

    public List<User> listAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (Exception e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            return List.of();
        }
    }
}