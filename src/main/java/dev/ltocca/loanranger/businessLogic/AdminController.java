package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.ORM.ConnectionManager;
import dev.ltocca.loanranger.ORM.LibraryDAO;
import dev.ltocca.loanranger.ORM.UserDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public void updateLibrary(Long libraryId, String name, String address, String phone, String email) {
        try {
            Optional<Library> libOpt = libraryDAO.getLibraryById(libraryId);
            if (libOpt.isEmpty()) {
                System.err.println("Library with ID " + libraryId + " not found.");
                return;
            }
            Library library = libOpt.get();
            if (name != null && !name.trim().isEmpty()) library.setName(name);
            if (address != null && !address.trim().isEmpty()) library.setAddress(address);
            if (phone != null && !phone.trim().isEmpty()) library.setPhone(phone);
            if (email != null && !email.trim().isEmpty()) library.setEmail(email);

            libraryDAO.updateLibrary(library);
            System.out.println("Library " + library.getName() + "'s information updated successfully.");
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

    public List<Library> listAllLibraries() {
        try {
            return libraryDAO.getAllLibraries();
        } catch (Exception e) {
            System.err.println("Error fetching all libraries: " + e.getMessage());
            return List.of();
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

    public void recreateSchemaAndAdmin() {
        System.out.println("Executing database reset...");
        try {
            executeSqlScript("sql/reset.sql");
            System.out.println("Database schema has been recreated successfully with a default admin.");
        } catch (Exception e) {
            System.err.println("Failed to reset the database: " + e.getMessage());
        }
    }

    public void generateDefaultDatabase() {
        System.out.println("Generating default database...");
        try {
            executeSqlScript("sql/default.sql");
            System.out.println("Default database generated successfully.");
        } catch (Exception e) {
            System.err.println("Failed to generate the default database: " + e.getMessage());
        }
    }

    private void executeSqlScript(String sqlPath) throws Exception {
        String scriptContent;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(sqlPath)) {
            if (is == null) {
                throw new RuntimeException("Cannot find script file: " + sqlPath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                scriptContent = reader.lines().collect(Collectors.joining("\n"));
            }
        }

        try (Connection conn = ConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(scriptContent);
        }
    }
}