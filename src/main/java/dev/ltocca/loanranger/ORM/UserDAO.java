package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.DomainModel.*;
import dev.ltocca.loanranger.ORM.DAOInterfaces.IUserDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO implements IUserDAO {

    private final Connection connection;
    private final LibraryDAO libraryDAO; // Dependency to fetch library details for librarians

    public UserDAO() throws SQLException {
        this.connection = ConnectionManager.getInstance().getConnection();
        this.libraryDAO = new LibraryDAO(); // Instantiate the dependency
    }

    @Override
    public User createUser(User user) {
        String sql = "INSERT INTO users (username, name, email, password, role, library_id) VALUES (?, ?, ?, ?, ?::user_role, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword());
            pstmt.setString(5, user.getRole().name());

            // Handle librarian-specific data
            if (user instanceof Librarian) {
                Librarian librarian = (Librarian) user;
                if (librarian.getWorkLibrary() == null || librarian.getWorkLibrary().getId() == null) {
                    throw new IllegalArgumentException("Validation failed: A Librarian must have an assigned work library with a valid ID.");
                }
                pstmt.setLong(6, librarian.getWorkLibrary().getId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getLong(1)); // First column is user_id
                    }
                }
            }
            return user;
        } catch (SQLException e) {
            // It's good practice to check for unique constraint violations -- suggested by gemini
            if (e.getSQLState().equals("23505")) { // '23505' is the SQLSTATE for unique_violation
                throw new RuntimeException("Error creating user: email '" + user.getEmail() + "' already exists.", e);
            }
            throw new RuntimeException("Error creating user", e);
        }
    }

    @Override
    public Optional<User> getUserById(Long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user with id " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        // ILIKE for case-insensitive email lookup
        String sql = "SELECT * FROM users WHERE email ILIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user with email " + email, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username ILIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user with username " + username, e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all users", e);
        }
        return users;
    }

    @Override
    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, role.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding users by role", e);
        }
        return users;
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username ILIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user with username " + username, e);
        }
        return Optional.empty();
    }

    @Override
    public void updateUsername(Long id, String newUsername) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number.");
        }
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("New username cannot be null or empty.");
        }
        String sql = "UPDATE users SET username = ? WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newUsername);
            pstmt.setLong(2, id);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Warning: No user found with ID " + id + ". Username update had no effect.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating username for user with id: " + id, e);
        }
    }
    @Override
    public void updatePassword(Long id, String newPassword) {
      if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be null or empty.");
        }
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setLong(2, id);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Warning: No user found with ID " + id + ". Password update had no effect.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating password for user with id: " + id, e);
        }
    }

    @Override
    public void updateEmail(Long id, String newEmail) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number.");
        }
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("New email cannot be null or empty.");
        }
        String sql = "UPDATE users SET email = ? WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newEmail);
            pstmt.setLong(2, id);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Warning: No user found with ID " + id + ". Email update had no effect.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating email for user with id: " + id, e);
        }
    }

    @Override
    public void librarianUpdateLibrary(User user, Long libraryId) {
        if (libraryId == null || libraryId <= 0) {
            throw new IllegalArgumentException("Library ID must be a positive number.");
        }
        if (!(user instanceof Librarian)) {
            throw new IllegalArgumentException("This operation is only valid for Librarian users.");
        }
        String sql = "UPDATE users SET library_id = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, libraryId);
            pstmt.setLong(2, user.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Warning: Update failed. No user found with ID " + user.getId() +
                        " or the user is not a Librarian.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error assigning library " + libraryId + " to librarian " + user.getId(), e);
        }
    }

    @Override
    public void updateUser(User user) {
        String sql = "UPDATE users SET username = ?, name = ?, email = ?, role = ?, library_id = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole().name());

            // Apply the same validation here
            if (user instanceof Librarian) {
                Librarian librarian = (Librarian) user;
                if (librarian.getWorkLibrary() == null || librarian.getWorkLibrary().getId() == null) {
                    throw new IllegalArgumentException("Validation failed: A Librarian must have an assigned work library with a valid ID.");
                }
                pstmt.setLong(5, librarian.getWorkLibrary().getId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            pstmt.setLong(6, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user with id " + user.getId(), e);
        }
    }

    @Override
    public void deleteUser(Long id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user with id " + id, e);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user;
        String roleStr = rs.getString("role");
        UserRole role = UserRole.valueOf(roleStr.toUpperCase()); // Ensure that the String is all Caps
        long libraryId = rs.getLong("library_id");


        user = getUserWithRole(rs, role, libraryId, roleStr);

        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(role);

        return user;
    }

    // Decide which subclass to instantiate based on role
    private User getUserWithRole(ResultSet rs, UserRole role, long libraryId, String roleStr) throws SQLException {
        User user;
        switch (role) {
            case LIBRARIAN:
                Librarian librarian = new Librarian();
                // If the librarian is assigned to a library, fetch the full Library object. -- suggested by gemini
                if (!rs.wasNull()) {
                    Optional<Library> library = libraryDAO.getLibraryById(libraryId);
                    library.ifPresent(librarian::setWorkLibrary);
                }
                user = librarian;
                break;
            case MEMBER:
                user = new Member();
                break;
            case ADMIN:
                user = new Admin();
                break;
            default:
                throw new IllegalStateException("Unknown user role in database: " + roleStr);
        }
        return user;
    }
}