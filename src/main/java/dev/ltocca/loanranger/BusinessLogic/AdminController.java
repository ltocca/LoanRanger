package dev.ltocca.loanranger.BusinessLogic;

import dev.ltocca.loanranger.DomainModel.*;
import dev.ltocca.loanranger.ORM.LibraryDAO;
import dev.ltocca.loanranger.ORM.UserDAO;

import java.sql.SQLException;
import java.util.Optional;

public class AdminController {
    private final Admin admin;
    private final LibraryDAO libraryDAO;
    private final UserDAO userDAO;
    private final LoginController loginController;

    public AdminController(Admin admin) throws SQLException {
        this.admin = admin;
        this.libraryDAO = new LibraryDAO();
        this.userDAO = new UserDAO();
        this.loginController = new LoginController();
    }

    public void addLibrary(String name, String address, String phone, String email) {
        try {
            Library newLibrary = new Library(name, address, phone, email);
            libraryDAO.createLibrary(newLibrary);
            System.out.println("Library '" + name + "' added successfully with ID " + newLibrary.getId());
        } catch (Exception e) {
            System.err.println("Error adding library: " + e.getMessage());
        }
    }


    public void updateLibrary(Library library) {
        try {
            libraryDAO.updateLibrary(library);
            System.out.println("Library " + library.getId() + " updated successfully.");
        } catch (Exception e) {
            System.err.println("Error updating library: " + e.getMessage());
        }
    }

    public void removeLibrary(Long libraryId) {
        try {
            // Add checks here to ensure library is empty or handle cascading deletes if necessary
            libraryDAO.deleteLibrary(libraryId);
            System.out.println("Library with ID " + libraryId + " removed successfully.");
        } catch (Exception e) {
            System.err.println("Error removing library: " + e.getMessage());
        }
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

    public void deleteUser(Long userId) {
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

    // TODO resetDatabase()
    // TODO generateDefaultDatabase
}