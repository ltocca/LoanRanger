package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.DomainModel.*;
import dev.ltocca.loanranger.DomainModel.State.*;
import dev.ltocca.loanranger.ORM.DAOInterfaces.ILoanDAO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanDAO implements ILoanDAO {

    private final Connection connection;
    // I think I need this query to have a "table" for methods that need other types of object
    private static final String LOAN_SELECT_SQL =
            "SELECT " +
                    "l.loan_id, l.loan_date, l.due_date, l.return_date, " +
                    "u.user_id, u.username, u.name, u.email, u.password, u.role, " +
                    "bc.copy_id, bc.status AS book_copy_status, " +
                    "b.isbn, b.title, b.author, b.publication_year, b.genre, " +
                    "lib.library_id, lib.name AS library_name, lib.address AS library_address, " +
                    "lib.phone AS library_phone, lib.email AS library_email " +
                    "FROM loans l " +
                    "JOIN users u ON l.member_id = u.user_id " +
                    "JOIN book_copies bc ON l.book_copy_id = bc.copy_id " +
                    "JOIN books b ON bc.isbn = b.isbn " +
                    "JOIN libraries lib ON bc.library_id = lib.library_id";

    public LoanDAO() throws SQLException {
        this.connection = ConnectionManager.getInstance().getConnection();
    }

    @Override
    public Loan createLoan(Loan loan) {
        String sql = "INSERT INTO loans (book_copy_id, member_id, loan_date, due_date, return_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, loan.getBookCopy().getCopyId());
            pstmt.setLong(2, loan.getMember().getId());
            pstmt.setDate(3, Date.valueOf(loan.getLoanDate()));
            pstmt.setDate(4, Date.valueOf(loan.getDueDate()));

            if (loan.getReturnDate() != null) {
                pstmt.setDate(5, Date.valueOf(loan.getReturnDate()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        loan.setId(rs.getLong("loan_id"));
                    }
                }
            }
            return loan;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating loan", e);
        }
    }

    @Override
    public Loan createLoan(BookCopy bookCopy, Member member) {
        String sql = "INSERT INTO loans (book_copy_id, member_id, loan_date, due_date, return_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Loan loan = new Loan(bookCopy, member, LocalDate.now());
            pstmt.setInt(1, loan.getBookCopy().getCopyId());
            pstmt.setLong(2, loan.getMember().getId());
            pstmt.setDate(3, Date.valueOf(loan.getLoanDate()));
            pstmt.setDate(4, Date.valueOf(LocalDate.now().plusDays(30)));
            pstmt.setNull(5, Types.DATE);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        loan.setId(rs.getLong("loan_id"));
                    }
                }
            }
            return loan;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating loan", e);
        }
    }

    @Override
    public Optional<Loan> getLoanById(Long id) {
        String sql = LOAN_SELECT_SQL + " WHERE l.loan_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToLoan(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Loan not found: id %s does not exist", id), e);
        }
        return Optional.empty();
    }

    @Override
    public void updateDueDate(Long id) {
        LocalDate newDueDate = LocalDate.now().plusDays(30);
        updateDueDate(id, newDueDate);
    }

    @Override
    public void updateDueDate(Long id, int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Number of days cannot be negative.");
        }
        LocalDate newDueDate = LocalDate.now().plusDays(days);
        updateDueDate(id, newDueDate);
    }

    @Override
    public void updateDueDate(Long id, LocalDate dueDate) {
        String sql = "UPDATE loans SET due_date = ? WHERE loan_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(dueDate));
            pstmt.setLong(2, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("Warning: No loan found with ID " + id + ". Due date update had no effect.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating due date for loan with id " + id, e);
        }
    }


    @Override
    public List<Loan> findLoansByMember(Member member) {
        List<Loan> loans = new ArrayList<>();
        String sql = LOAN_SELECT_SQL + " WHERE l.member_id = ? ORDER BY l.loan_date DESC";
        return extractLoansMember(member, loans, sql);
    }

    @Override
    public List<Loan> findActiveLoansByMember(Member member) {
        List<Loan> loans = new ArrayList<>();
        String sql = LOAN_SELECT_SQL + " WHERE l.member_id = ? AND l.return_date IS NULL ORDER BY l.due_date ASC";
        return extractLoansMember(member, loans, sql);
    }

    private List<Loan> extractLoansMember(Member member, List<Loan> loans, String sql) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, member.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapRowToLoan(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding loans for member id " + member.getId(), e);
        }
        return loans;
    }

    @Override
    public List<Loan> findOverdueLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = LOAN_SELECT_SQL + " WHERE l.due_date < CURRENT_DATE AND l.return_date IS NULL ORDER BY l.due_date ASC";
        try (Statement stmt = connection.createStatement(); // no ? in the sql string, statement is sufficient
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                loans.add(mapRowToLoan(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding overdue loans", e);
        }
        return loans;
    }

    @Override
    public List<Loan> findMemberOverdueLoans(Long memberId) {
        List<Loan> loans = new ArrayList<>();
        String sql = LOAN_SELECT_SQL + " WHERE l.due_date < CURRENT_DATE AND l.member_id = ? AND l.return_date IS NULL ORDER BY l.due_date ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapRowToLoan(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding overdue loans for member id: " + memberId, e);
        }
        return loans;
    }

    @Override
    public void deleteLoan(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Loan ID must be a positive number.");
        }
        String sql = "DELETE FROM loans WHERE loan_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting loan with id: " + id, e);
        }
    }

    @Override
    public void deleteLoan(Loan loan) {
        if (loan == null || loan.getId() == null) {
            throw new IllegalArgumentException("Loan and its ID cannot be null.");
        }
        deleteLoan(loan.getId());
    }

    private Loan mapRowToLoan(ResultSet rs) throws SQLException {

        Member member = new Member();
        member.setId(rs.getLong("user_id"));
        member.setUsername(rs.getString("username"));
        member.setName(rs.getString("name"));
        member.setEmail(rs.getString("email"));
        member.setPassword(rs.getString("password"));
        member.setRole(UserRole.MEMBER);

        Library library = new Library(
                rs.getLong("library_id"), rs.getString("library_name"),
                rs.getString("library_address"), rs.getString("library_phone"),
                rs.getString("library_email")
        );

        Book book = new Book(
                rs.getString("isbn"), rs.getString("title"),
                rs.getString("author"), rs.getInt("publication_year"),
                rs.getString("genre")
        );

        BookCopy bookCopy = new BookCopy();
        bookCopy.setCopyId(rs.getInt("copy_id"));
        bookCopy.setBook(book); // Associate the Book
        bookCopy.setLibrary(library); // Associate the Library
        bookCopy.setState(mapStatusToState(rs.getString("book_copy_status")));

        Loan loan = new Loan();
        loan.setId(rs.getLong("loan_id"));
        loan.setLoanDate(rs.getDate("loan_date").toLocalDate());
        loan.setDueDate(rs.getDate("due_date").toLocalDate());
        Date returnDate = rs.getDate("return_date");
        if (returnDate != null) {
            loan.setReturnDate(returnDate.toLocalDate());
        }

        loan.setMember(member);
        loan.setBookCopy(bookCopy);

        return loan;
    }

    private AvailabilityState mapStatusToState(String status) {
        if (status == null) return new AvailableState();
        switch (BookStatus.valueOf(status.toUpperCase())) {
            case LOANED:
                return new LoanedState();
            case RESERVED:
                return new ReservedState();
            case UNDER_MAINTENANCE:
                return new UnderMaintenanceState();
            default:
                return new AvailableState();
        }
    }

}
