package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.DomainModel.Library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibraryDAO implements ILibraryDAO {

    private final Connection connection;

    public LibraryDAO() throws SQLException {
        this.connection = ConnectionManager.getInstance().getConnection();
    }

    @Override
    public Library createLibrary(Library library) {
        String sql = "INSERT INTO libraries (name, address, phone, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, library.getName());
            pstmt.setString(2, library.getAddress());
            pstmt.setString(3, library.getPhone());
            pstmt.setString(4, library.getEmail());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        library.setId(rs.getLong(1)); // extracts the first column
                    }
                }
            }
            return library;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating library", e);
        }
    }

    @Override
    public Optional<Library> getLibraryById(Long id) {
        String sql = "SELECT * FROM libraries WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToLibrary(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching library with id " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Library> findLibrariesByName(String name) {
        List<Library> libraries = new ArrayList<>();
        String sql = "SELECT * FROM libraries WHERE name ILIKE ?"; // ILIKE used to avoid case sensitivity
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    libraries.add(mapRowToLibrary(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("No library contains: %s", name), e);
        }
        return libraries;
    }

    @Override
    public List<Library> getAllLibraries() {
        List<Library> libraries = new ArrayList<>();
        String sql = "SELECT * FROM libraries ORDER BY name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                libraries.add(mapRowToLibrary(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all libraries", e);
        }
        return libraries;
    }

    @Override
    public void updateLibrary(Library library) {
        String sql = "UPDATE libraries SET name = ?, address = ?, phone = ?, email = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, library.getName());
            pstmt.setString(2, library.getAddress());
            pstmt.setString(3, library.getPhone());
            pstmt.setString(4, library.getEmail());
            pstmt.setLong(5, library.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating library with id " + library.getId(), e);
        }
    }

    @Override
    public void deleteLibrary(Long id) {
        String sql = "DELETE FROM libraries WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting library with id " + id, e);
        }
    }

    @Override
    public void deleteLibrary(Library library) {
        Long id = library.getId();
        String sql = "DELETE FROM libraries WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting library named: "+ library.getName() + ", and with id " + id, e);
        }
    }

    // helper function to generate object from row
    private Library mapRowToLibrary(ResultSet rs) throws SQLException {
        Library library = new Library();
        library.setId(rs.getLong("id"));
        library.setName(rs.getString("name"));
        library.setAddress(rs.getString("address"));
        library.setPhone(rs.getString("phone"));
        library.setEmail(rs.getString("email"));
        return library;
    }
}