package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.domainModel.Book;
import dev.ltocca.loanranger.ORM.DAOInterfaces.IBookDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDAO implements IBookDAO {
    private final Connection connection;

    public BookDAO() throws SQLException, ClassNotFoundException {
        this.connection = ConnectionManager.getInstance().getConnection();
    }

    @Override
    public Book createBook(Book book) {
        String sql = "INSERT INTO books (title, author, publication_year, genre) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3, book.getPublicationYear());
            pstmt.setString(4, book.getGenre());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        book.setIsbn(rs.getString(1)); // extracts the first column
                    }
                }
            }
            return book;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating book", e);
        }
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, isbn);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Book not found: ISBN %s does not exist", isbn), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Book> getBookByTitle(String title) {
        String sql = "SELECT * FROM books WHERE title = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, title);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Book not found: there are no books with '%s' as title", title), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Book> getAllBooks() {
        String sql = "SELECT * FROM books ORDER BY title";
        List<Book> books = new ArrayList<>();
        try (Statement stmt = connection.prepareStatement(sql)) { // no need fort a PreparedMethod, it is a static queru
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    books.add(mapRowToBook(rs));
                }
                return books;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all books", e);
        }
    }

    @Override
    public List<Book> findBooksByAuthor(String author) {
        String sql = "SELECT * FROM books WHERE author = ? ORDER BY title";
        List<Book> books = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, author);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRowToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Book not found: there are no books written by '%s'", author), e);
        }
        return books;
    }

    @Override
    public List<Book> findBooksByPublicationYear(int publicationYear) {
        String sql = "SELECT * FROM books WHERE publication_year = ? ORDER BY title";
        List<Book> books = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, publicationYear);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRowToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Book not found: there are no books written in %s in our system", publicationYear), e);
        }
        return books;
    }

    @Override
    public List<Book> findBookByIsbn(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ? ORDER BY title";
        List<Book> books = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + isbn+ "%"); // partial string search
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRowToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Book not found: there are no books that contain this ISBN: '%s'", isbn), e);
        }
        return books;
    }

    @Override
    public void deleteBook(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty.");
        }
        String sql = "DELETE FROM books WHERE isbn = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting book with ISBN: " + isbn, e);
        }
    }

    @Override
    public void deleteBook(Book book) {
        if (book == null || book.getIsbn() == null) {
            throw new IllegalArgumentException("Book and its ISBN cannot be null.");
        }
        deleteBook(book.getIsbn());
    }

    private Book mapRowToBook(ResultSet rs) throws SQLException {
        String isbn = rs.getString("isbn");
        String title = rs.getString("title");
        String author = rs.getString("author");
        int year = rs.getInt("publication_year");
        String genre = rs.getString("genre");
        return new Book(isbn, title, author, year, genre);
    }
}
