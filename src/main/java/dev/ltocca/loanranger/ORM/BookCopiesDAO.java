package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.domainModel.State.*;
import dev.ltocca.loanranger.ORM.DAOInterfaces.IBookCopiesDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookCopiesDAO implements IBookCopiesDAO {
    private final Connection connection;

    // A single, reusable JOIN query to build the entire BookCopy object graph efficiently. -- AI Generated
    private static final String BOOK_COPY_SELECT_SQL =
            "SELECT bc.copy_id, bc.status, " +
                    "b.isbn, b.title, b.author, b.publication_year, b.genre, " +
                    "l.library_id, l.name AS library_name, l.address, l.phone, l.email AS library_email " +
                    "FROM book_copies bc " +
                    "JOIN books b ON bc.isbn = b.isbn " +
                    "JOIN libraries l ON bc.library_id = l.library_id";

    public BookCopiesDAO() throws SQLException {
        this.connection = ConnectionManager.getInstance().getConnection();
    }

    @Override
    public BookCopy createCopy(BookCopy bookCopy) {
        String sql = "INSERT INTO book_copies (isbn, library_id, status) VALUES (?, ?, ?::book_status)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, bookCopy.getBook().getIsbn());
            pstmt.setLong(2, bookCopy.getLibrary().getId());
            pstmt.setString(3, bookCopy.getState().getStatus());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        bookCopy.setCopyId(rs.getLong("copy_id"));
                    }
                }
            }
            return bookCopy;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating book copy", e);
        }
    }

    @Override
    public Optional<BookCopy> getCopyById(Long id) {
        String sql = BOOK_COPY_SELECT_SQL + " WHERE bc.copy_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToBookCopy(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching book copy with id " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<BookCopy> getAllBookCopies(){
        List<BookCopy> bookCopies = new ArrayList<>();
        String sql = BOOK_COPY_SELECT_SQL;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)){
            while(rs.next()){
                bookCopies.add(mapRowToBookCopy(rs));
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Error fetching all book copies.");
        }
        return bookCopies;
    }


    @Override
    public List<BookCopy> searchByTitle(String titleFragment) {
        List<BookCopy> bookCopies = new ArrayList<>();
        String sql = BOOK_COPY_SELECT_SQL + " WHERE b.title ILIKE ?"; // using ILIKE to take advantage (again) of postrgresql to lowercase the text inserted
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + titleFragment + "%"); // partial string search
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookCopies.add(mapRowToBookCopy(rs));
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Error: cannot search for partial title: " + titleFragment, e);
        }
        return bookCopies;
    }

    @Override
    public List<BookCopy> searchByAuthor(String authorFragment) {
        List<BookCopy> copies = new ArrayList<>();
        String sql = BOOK_COPY_SELECT_SQL + " WHERE b.author ILIKE ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + authorFragment + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    copies.add(mapRowToBookCopy(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching book copies by author", e);
        }
        return copies;
    }


    @Override
    public List<BookCopy> searchByIsbn(String isbnFragment) {
        List<BookCopy> copies = new ArrayList<>();
        String sql = BOOK_COPY_SELECT_SQL + " WHERE b.isbn LIKE ?"; // all numbers, no need to lowercase
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + isbnFragment + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    copies.add(mapRowToBookCopy(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching book copies by ISBN", e);
        }
        return copies;
    }

    @Override
    public void updateCopyStatus(BookCopy bookCopy) {
        String sql = "UPDATE book_copies SET status = ?::book_status WHERE copy_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, bookCopy.getState().getStatus());
            pstmt.setLong(2, bookCopy.getCopyId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating book copy status for id " + bookCopy.getCopyId(), e);
        }
    }

    @Override
    public void updateCopyStatus(Long copyId, BookStatus status) {
        String sql = "UPDATE book_copies SET status = ? WHERE copy_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.toString());
            pstmt.setLong(2, copyId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating book copy status for id " + copyId, e);
        }
    }

    @Override
    public void deleteCopy(Long id) {
        String sql = "DELETE FROM book_copies WHERE copy_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting book copy with id " + id, e);
        }
    }

    @Override
    public void deleteCopy(BookCopy bookCopy) {
        deleteCopy(bookCopy.getCopyId());
    }

    @Override
    public List<BookCopy> findAllBookCopies(Book book) {
        List<BookCopy> copies = new ArrayList<>();
        String sql = BOOK_COPY_SELECT_SQL + " WHERE bc.isbn = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, book.getIsbn());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                copies.add(mapRowToBookCopy(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding copies for book isbn " + book.getIsbn(), e);
        }
        return copies;
    }

    @Override
    public List<BookCopy> findLibraryCopies(Library library) {
        List<BookCopy> copies = new ArrayList<>();
        String sql = BOOK_COPY_SELECT_SQL + " WHERE bc.library_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, library.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                copies.add(mapRowToBookCopy(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Error finding copies for this Library id: %s", library.getId()), e);
        }
        return copies;
    }

    @Override
    public List<BookCopy> findAvailableBookCopies(Book book) {
        List<BookCopy> copies = new ArrayList<>();
        String sql = BOOK_COPY_SELECT_SQL + " WHERE bc.isbn = ? AND bc.status = 'AVAILABLE'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, book.getIsbn());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                copies.add(mapRowToBookCopy(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Error finding available copies for this isbn: %s", book.getIsbn()), e);
        }
        return copies;
    }

    private BookCopy mapRowToBookCopy(ResultSet rs) throws SQLException {
        // 1. Build the nested Book object
        Book book = new Book(
                rs.getString("isbn"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getInt("publication_year"),
                rs.getString("genre")
        );

        // 2. Build the nested Library object
        Library library = new Library(
                rs.getLong("library_id"),
                rs.getString("library_name"),
                rs.getString("address"),
                rs.getString("phone"),
                rs.getString("library_email")
        );

        // 3. Build the main BookCopy object
        BookCopy copy = new BookCopy();
        copy.setCopyId(rs.getLong("copy_id"));

        // 4. Assemble the object graph
        copy.setBook(book);
        copy.setLibrary(library);

        // 5. Convert status string to concrete State object
        copy.setState(mapStatusToState(rs.getString("status")));

        return copy;
    }

    /**
     * Helper factory to convert a status string from the DB into a concrete State object.
     * This is the key to bridging the State Pattern and the database.
     */
    private AvailabilityState mapStatusToState(String status) {
        if (status == null) return new AvailableState(); // A safe default
        switch (BookStatus.valueOf(status.toUpperCase())) {
            case LOANED:
                return new LoanedState();
            case RESERVED:
                return new ReservedState();
            case UNDER_MAINTENANCE:
                return new UnderMaintenanceState();
            case AVAILABLE:
            default:
                return new AvailableState();
        }
    }
}