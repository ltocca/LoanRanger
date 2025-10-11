package dev.ltocca.loanranger.presentationLayer;

import dev.ltocca.loanranger.businessLogic.*;
import dev.ltocca.loanranger.domainModel.*;
import dev.ltocca.loanranger.service.BookCopySearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@ConditionalOnProperty(
        prefix = "app",
        name = "cli.enabled",
        havingValue = "true",
        matchIfMissing = true // Ensures the CLI runs by default if the property is not set (e.g., in production)
)
@Component
public class MainCLI implements CommandLineRunner {
    private Scanner scanner;
    private User currentUser;

    // All controllers are injected by Spring
    private final LoginController loginController;
    private final MemberBookController memberBookController;
    private final MemberAccountController memberAccountController;
    private final LibrarianBookController librarianBookController;
    private final LibrarianAccountController librarianAccountController;
    private final AdminBookController adminBookController;
    private final AdminUsersController adminUsersController;
    private final AdminDatabaseController adminDatabaseController;
    private final AdminAccountController adminAccountController;

    @Autowired
    public MainCLI(LoginController loginController, MemberBookController memberBookController,
                   MemberAccountController memberAccountController, LibrarianBookController librarianBookController,
                   LibrarianAccountController librarianAccountController, AdminBookController adminBookController,
                   AdminUsersController adminUsersController, AdminDatabaseController adminDatabaseController,
                   AdminAccountController adminAccountController) {
        this.loginController = loginController;
        this.memberBookController = memberBookController;
        this.memberAccountController = memberAccountController;
        this.librarianBookController = librarianBookController;
        this.librarianAccountController = librarianAccountController;
        this.adminBookController = adminBookController;
        this.adminUsersController = adminUsersController;
        this.adminDatabaseController = adminDatabaseController;
        this.adminAccountController = adminAccountController;
    }

    @Override
    public void run(String... args) {
        try {
            this.scanner = new Scanner(System.in);
            runPreLoginLoop();
        } catch (Exception e) {
            System.err.println("A fatal error occurred. Exiting application.");
            e.printStackTrace();
        }
    }

    private void runPreLoginLoop() throws SQLException {
        while (true) {
            System.out.println("\n--- Welcome to LoanRanger ---");
            System.out.println("1. Login");
            System.out.println("2. Register as a new Member");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegister();
                    break;
                case "3":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.err.println("Invalid option. Please try again.");
            }
        }
    }

    private void handleLogin() throws SQLException {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        Optional<User> userOpt = loginController.login(email, password);
        if (userOpt.isPresent()) {
            currentUser = userOpt.get();
            runPostLoginLoop();
        } else {
            System.err.println("Login failed. Please check your credentials.");
        }
    }

    private void handleRegister() throws SQLException {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your full name: ");
        String name = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password (min 6 characters): ");
        String password = scanner.nextLine();

        try {
            loginController.register(UserRole.MEMBER, username, name, email, password, null);
            System.out.println("Registration successful! You can now log in.");
        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
        }
    }

    private void runPostLoginLoop() throws SQLException {
        while (currentUser != null) {
            displayRoleBasedMenu();
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            if (currentUser instanceof Member) {
                handleMemberActions((Member) currentUser, choice);
            } else if (currentUser instanceof Librarian) {
                handleLibrarianActions((Librarian) currentUser, choice);
            } else if (currentUser instanceof Admin) {
                handleAdminActions((Admin) currentUser, choice);
            }
        }
    }

    private void displayRoleBasedMenu() {
        System.out.println("\n--- Logged in as: " + currentUser.getName() + " (" + currentUser.getRole() + ") ---");
        if (currentUser instanceof Member) {
            System.out.println("--- Member Menu ---");
            System.out.println("1. Search for Books");
            System.out.println("2. Reserve a Book");
            System.out.println("3. View My Active Loans");
            System.out.println("4. View My Reservations");
            System.out.println("5. Cancel a Reservation");
            System.out.println("--- My Account ---");
            System.out.println("6. Change Username");
            System.out.println("7. Change Email");
            System.out.println("8. Change Password");
            System.out.println("9. Delete Account");
            System.out.println("10. Logout");
        } else if (currentUser instanceof Librarian) {
            System.out.println("--- Librarian Menu ---");
            System.out.println("--- Circulation Desk ---");
            System.out.println("1. Loan Book to Member");
            System.out.println("2. Process Book Return");
            System.out.println("3. Renew a Loan");
            System.out.println("--- Library Monitoring ---");
            System.out.println("4. View Library Loans (Active/Overdue)");
            System.out.println("5. View Library Reservations (Active/Past)");
            System.out.println("--- Book Inventory ---");
            System.out.println("6. Search Books");
            System.out.println("7. Add New Book Copy");
            System.out.println("8. Place Copy Under Maintenance");
            System.out.println("9. Remove Copy from Maintenance");
            System.out.println("--- My Account ---");
            System.out.println("10. Change Username");
            System.out.println("11. Change Email");
            System.out.println("12. Change Password");
            System.out.println("13. Logout");
        }
        if (currentUser instanceof Admin) {
            System.out.println("--- Admin Menu ---");
            System.out.println("--- Library Management ---");
            System.out.println("1. Add New Library");
            System.out.println("2. Update Library Information");
            System.out.println("3. Remove Library");
            System.out.println("4. List All Libraries");
            System.out.println("--- Book Management ---");
            System.out.println("5. Add New Book");
            System.out.println("6. Remove Book");
            System.out.println("7. List All Books");
            System.out.println("8. View Book Details");
            System.out.println("--- User Management ---");
            System.out.println("9. Register New Librarian");
            System.out.println("10. Re-assign Librarian");
            System.out.println("11. Delete a User Account");
            System.out.println("12. List All Users");
            System.out.println("--- System ---");
            System.out.println("13. Seed Database with Default Data");
            System.out.println("14. Recreate Database (Schema + Admin)");
            System.out.println("--- My Account ---");
            System.out.println("15. Change Email");
            System.out.println("16. Change Password");
            System.out.println("17. Logout");
        }

    }

    // --- MEMBER ACTION HANDLERS ---
    private void handleMemberActions(Member member, String choice) throws SQLException {
        switch (choice) {
            case "1":
                handleMemberBookSearch();
                break;
            case "2":
                handleMemberReserveBook(member);
                break;
            case "3":
                handleMemberManageLoans(member);
                break;
            case "4":
                handleMemberViewReservations(member);
                break;
            case "5":
                handleMemberCancelReservation(member);
                break;
            case "6":
                handleMemberChangeUsername(member);
                break;
            case "7":
                handleMemberChangeEmail(member);
                break;
            case "8":
                handleMemberChangePassword(member);
                break;
            case "9":
                handleMemberDeleteAccount(member);
                break;
            case "10":
                currentUser = null;
                break;
            default:
                System.err.println("Invalid option.");
        }
    }

    private void handleMemberBookSearch() {
        System.out.println("\n--- Choose Search Type ---");
        System.out.println("1. Search by Title");
        System.out.println("2. Search by Author");
        System.out.println("3. Search by ISBN");
        System.out.println("4. Smart Search (Title, Author, or ISBN)");
        System.out.print("Choose search type: ");
        String searchTypeChoice = scanner.nextLine();

        System.out.print("Enter search query: ");
        String query = scanner.nextLine();

        List<BookCopy> results;

        switch (searchTypeChoice) {
            case "1":
                results = memberBookController.searchBooksByTitle(query);
                break;
            case "2":
                results = memberBookController.searchBooksByAuthor(query);
                break;
            case "3":
                results = memberBookController.searchBooksByIsbn(query);
                break;
            case "4":
                results = memberBookController.searchBookCopyGeneric(query);
                break;
            default:
                System.err.println("Invalid search type. Performing smart search by default.");
                results = memberBookController.searchBookCopyGeneric(query);
                break;
        }

        if (results.isEmpty()) {
            System.out.println("No books found.");
        } else {
            System.out.println("\n--- Search Results ---");
            String format = "%-7s | %-30.30s | %-20.20s | %-15.15s | %-25.25s | %-15s%n";
            System.out.printf(format, "Copy ID", "Title", "Author", "Genre", "Library (ID)", "Status");
            System.out.println(String.join("", Collections.nCopies(120, "-")));
            results.forEach(c -> System.out.printf(format,
                    c.getCopyId(),
                    c.getBook().getTitle(),
                    c.getBook().getAuthor(),
                    c.getBook().getGenre() != null ? c.getBook().getGenre() : "N/A",
                    String.format("%s (%d)", c.getLibrary().getName() + " :", c.getLibrary().getId()),
                    c.getState().getStatus()));
        }
    }

    private void handleMemberReserveBook(Member member) {
        System.out.print("Enter the Copy ID of the book to reserve: ");
        try {
            Long copyId = Long.parseLong(scanner.nextLine());
            memberBookController.reserveBookCopy(member, copyId);
        } catch (NumberFormatException e) {
            System.err.println("Invalid ID format.");
        }
    }

    private void handleMemberManageLoans(Member member) {
        System.out.println("\n--- Manage My Loans ---");
        System.out.println("1. View Active Loans");
        System.out.println("2. View Overdue Loans");
        System.out.println("3. View Full Loan History");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        List<Loan> loans;
        switch (choice) {
            case "1":
                loans = memberBookController.getActiveLoans(member);
                System.out.println("\n--- Your Active Loans ---");
                break;
            case "2":
                loans = memberBookController.getOverdueLoans(member);
                System.out.println("\n--- Your Overdue Loans ---");
                break;
            case "3":
                loans = memberBookController.getAllLoans(member);
                System.out.println("\n--- Your Full Loan History ---");
                break;
            default:
                System.err.println("Invalid option.");
                return;
        }

        if (loans.isEmpty()) {
            System.out.println("No loans found for this category.");
        } else {
            String format = "%-7s | %-7s | %-30.30s | %-20.20s | %-40.40s | %-12s | %-10s%n";
            System.out.printf(format, "Loan ID", "Copy ID", "Title", "Author", "Library (ID)", "Due Date", "Status");
            System.out.println(String.join("", Collections.nCopies(130, "-")));
            for (Loan loan : loans) {
                String status;
                if (loan.getReturnDate() != null) {
                    status = "Returned";
                } else if (loan.getDueDate().isBefore(LocalDate.now())) {
                    status = "Overdue";
                } else {
                    status = "Active";
                }
                System.out.printf(format,
                        loan.getId(),
                        loan.getBookCopy().getCopyId(),
                        loan.getBookCopy().getBook().getTitle(),
                        loan.getBookCopy().getBook().getAuthor(),
                        String.format("%s (%d)", loan.getBookCopy().getLibrary().getName(), loan.getBookCopy().getLibrary().getId()),
                        loan.getDueDate(),
                        status);
            }
        }
    }

    private void handleMemberViewReservations(Member member) {
        System.out.println("\n--- My Reservations ---");
        System.out.println("1. View Active (Pending) Reservations");
        System.out.println("2. View Full Reservation History");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        List<Reservation> reservations;
        switch (choice) {
            case "1":
                reservations = memberBookController.getActiveReservations(member);
                System.out.println("\n--- Your Active Reservations ---");
                break;
            case "2":
                reservations = memberBookController.getAllReservations(member);
                System.out.println("\n--- Your Full Reservation History ---");
                break;
            default:
                System.err.println("Invalid option.");
                return;
        }

        if (reservations.isEmpty()) {
            System.out.println("No reservations found for this category.");
        } else {
            String format = "%-10s | %-7s | %-30.30s | %-20.20s | %-40.40s | %-12s | %-15s%n";
            System.out.printf(format, "Res. ID", "Copy ID", "Title", "Author", "Library (ID)", "Date", "Status");
            System.out.println(String.join("", Collections.nCopies(130, "-")));
            for (Reservation r : reservations) {
                System.out.printf(format,
                        r.getId(),
                        r.getBookCopy().getCopyId(),
                        r.getBookCopy().getBook().getTitle(),
                        r.getBookCopy().getBook().getAuthor(),
                        String.format("%s (%d)", r.getBookCopy().getLibrary().getName(), r.getBookCopy().getLibrary().getId()),
                        r.getReservationDate(),
                        r.getStatus());
            }
        }
    }

    private void handleMemberCancelReservation(Member member) {
        List<Reservation> reservations = memberBookController.getActiveReservations(member);
        if (reservations.isEmpty()) {
            System.out.println("\nYou have no active reservations to cancel.");
            return;
        }

        System.out.println("\n--- Your Active Reservations ---");
        String format = "%-5s | %-30s | %-15s%n";
        System.out.printf(format, "ID", "Title", "Status");
        System.out.println(String.join("", Collections.nCopies(55, "-")));
        reservations.forEach(r -> System.out.printf(format, r.getId(), r.getBookCopy().getBook().getTitle(), r.getStatus()));
        System.out.println(String.join("", Collections.nCopies(55, "-")));

        System.out.print("Enter the ID of the reservation you want to cancel: ");
        try {
            Long reservationId = Long.parseLong(scanner.nextLine());
            if (memberBookController.cancelReservation(member, reservationId)) {
                System.out.println("Reservation cancelled successfully.");
            } else {
                System.err.println("Failed to cancel reservation. Please check the ID and try again.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid ID format. Please enter a number.");
        }
    }

    private void handleMemberChangeUsername(Member member) {
        System.out.print("Enter new username: ");
        String newUsername = scanner.nextLine();
        try {
            memberAccountController.changeUsername(member, newUsername);
            System.out.println("Username updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleMemberChangeEmail(Member member) {
        System.out.print("Enter new email: ");
        String newEmail = scanner.nextLine();
        try {
            memberAccountController.changeEmail(member, newEmail);
            System.out.println("Email updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleMemberChangePassword(Member member) {
        System.out.print("Enter current password: ");
        String currentPass = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine();
        try {
            memberAccountController.changePassword(member, currentPass, newPass);
            System.out.println("Password updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleMemberDeleteAccount(Member member) {
        System.out.print("Are you sure you want to delete your account? This cannot be undone. Enter your password to confirm: ");
        String password = scanner.nextLine();
        try {
            memberAccountController.deleteAccount(member, password);
            System.out.println("Account deleted successfully.");
            currentUser = null; // Log out after deletion
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    // --- LIBRARIAN ACTION HANDLERS ---
    private void handleLibrarianActions(Librarian librarian, String choice) throws SQLException {
        switch (choice) {
            case "1":
                handleLibrarianLoanBook(librarian);
                break;
            case "2":
                handleLibrarianProcessReturn(librarian);
                break;
            case "3":
                handleLibrarianRenewLoan(librarian);
                break;
            case "4":
                handleLibrarianViewLoans(librarian);
                break;
            case "5":
                handleLibrarianViewReservations(librarian);
                break;
            case "6":
                handleLibrarianBookSearch(librarian);
                break;
            case "7":
                handleLibrarianAddBookCopy(librarian);
                break;
            case "8":
                handleLibrarianMaintenance(librarian, true);
                break;
            case "9":
                handleLibrarianMaintenance(librarian, false);
                break;
            case "10":
                handleLibrarianChangeUsername(librarian);
                break;
            case "11":
                handleLibrarianChangeEmail(librarian);
                break;
            case "12":
                handleLibrarianChangePassword(librarian);
                break;
            case "13":
                currentUser = null;
                break;
            default:
                System.err.println("Invalid option.");
        }
    }

    private void handleLibrarianLoanBook(Librarian librarian) {
        Long memberId = promptForLong("Enter Member ID");
        Long copyId = promptForLong("Enter Copy ID");
        if (memberId != null && copyId != null) {
            if (librarianBookController.loanBookToMember(librarian, memberId, copyId, null)) {
                System.out.println("Loan processed successfully.");
            } else {
                System.err.println("Failed to process loan. Check the IDs and book availability.");
            }
        }
    }

    private void handleLibrarianProcessReturn(Librarian librarian) {
        Long copyId = promptForLong("Enter Copy ID of returned book");
        if (copyId != null) {
            if (librarianBookController.processReturn(librarian, copyId)) {
                System.out.println("Return processed successfully.");
            } else {
                System.err.println("Failed to process return. Check the Copy ID.");
            }
        }
    }

    private void handleLibrarianViewLoans(Librarian librarian) {
        System.out.println("\n--- View Library Loans ---");
        System.out.println("1. View Active Loans");
        System.out.println("2. View Overdue Loans");
        System.out.println("3. View Full Loan History");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        List<Loan> loans;
        switch (choice) {
            case "1":
                loans = librarianBookController.getActiveLoans(librarian);
                System.out.println("\n--- Active Loans in This Library ---");
                break;
            case "2":
                loans = librarianBookController.getOverdueLoans(librarian);
                System.out.println("\n--- Overdue Loans in This Library ---");
                break;
            case "3":
                loans = librarianBookController.getAllLoans(librarian);
                System.out.println("\n--- Full Loan History for This Library ---");
                break;
            default:
                System.err.println("Invalid option.");
                return;
        }

        if (loans.isEmpty()) {
            System.out.println("No loans found for this category.");
        } else {
            String format = "%-7s | %-7s | %-20.20s | %-30.30s | %-12s | %-15s | %-10s%n";
            System.out.printf(format, "Loan ID", "Copy ID", "Member Name", "Title", "Due Date", "Days remaining", "Status");
            System.out.println(String.join("", Collections.nCopies(95, "-")));
            for (Loan loan : loans) {
                String status;
                if (loan.getReturnDate() != null) {
                    status = "Returned";
                } else if (loan.getDueDate().isBefore(LocalDate.now())) {
                    status = "Overdue";
                } else {
                    status = "Active";
                }
                System.out.printf(format,
                        loan.getId(),
                        loan.getBookCopy().getCopyId(),
                        loan.getMember().getName(),
                        loan.getBookCopy().getBook().getTitle(),
                        loan.getDueDate(), loan.getRemainingDays(),
                        status);
            }
        }
    }

    private void handleLibrarianViewReservations(Librarian librarian) throws SQLException {
        System.out.println("\n--- View Library Reservations ---");
        System.out.println("1. View Active (Pending) Reservations");
        System.out.println("2. View Past Reservations");
        System.out.println("3. View Full Reservation History");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        List<Reservation> reservations;
        switch (choice) {
            case "1":
                reservations = librarianBookController.getActiveReservations(librarian);
                System.out.println("\n--- Active Reservations in This Library ---");
                break;
            case "2":
                reservations = librarianBookController.getPastReservations(librarian);
                System.out.println("\n--- Past Reservations in This Library ---");
                break;
            case "3":
                reservations = librarianBookController.getAllReservations(librarian);
                System.out.println("\n--- Full Reservation History for This Library ---");
                break;
            default:
                System.err.println("Invalid option.");
                return;
        }

        if (reservations.isEmpty()) {
            System.out.println("No reservations found for this category.");
        } else {
            String format = "%-10s | %-7s | %-30.30s | %-7s | %-20.20s | %-12s | %-15s%n";
            System.out.printf(format, "Res. ID", "Copy ID", "Title", "Mem. ID", "Member Name", "Res. Date", "Status");
            System.out.println(String.join("", Collections.nCopies(130, "-")));
            for (Reservation r : reservations) {
                System.out.printf(format,
                        r.getId(),
                        r.getBookCopy().getCopyId(),
                        r.getBookCopy().getBook().getTitle(),
                        r.getMember().getId(),
                        r.getMember().getName(),
                        r.getReservationDate(),
                        r.getStatus());
            }
        }
    }

    private void handleLibrarianRenewLoan(Librarian librarian) {
        Long loanId = promptForLong("Enter Loan ID to renew");
        if (loanId != null) {
            if (librarianBookController.renewLoan(librarian, loanId, null)) {
                System.out.println("Loan renewed successfully.");
            } else {
                System.err.println("Failed to renew loan. Check the Loan ID.");
            }
        }
    }

    private void handleLibrarianBookSearch(Librarian librarian) {
        System.out.println("\n--- Choose Search Scope ---");
        System.out.println("1. Search in My Library Only");
        System.out.println("2. Search System-wide");
        System.out.print("Choose scope: ");
        String scopeChoice = scanner.nextLine();
        boolean systemWide = scopeChoice.equals("2");

        System.out.println("\n--- Choose Search Type ---");
        System.out.println("1. Smart Search (Title, Author, or ISBN)");
        System.out.println("2. Search by Title");
        System.out.println("3. Search by Author");
        System.out.println("4. Search by ISBN");
        System.out.print("Choose search type: ");
        String searchTypeChoice = scanner.nextLine();

        System.out.print("Enter search query: ");
        String query = scanner.nextLine();

        List<BookCopy> results;
        if (systemWide) {
            results = switch (searchTypeChoice) {
                case "2" -> librarianBookController.searchBookCopiesElsewhere(query, BookCopySearchService.SearchType.TITLE);
                case "3" -> librarianBookController.searchBookCopiesElsewhere(query, BookCopySearchService.SearchType.AUTHOR);
                case "4" -> librarianBookController.searchBookCopiesElsewhere(query, BookCopySearchService.SearchType.ISBN);
                default -> librarianBookController.searchBookCopiesElsewhere(query);
            };
        } else {
            results = switch (searchTypeChoice) {
                case "2" -> librarianBookController.searchBookCopies(librarian, query, BookCopySearchService.SearchType.TITLE);
                case "3" -> librarianBookController.searchBookCopies(librarian, query, BookCopySearchService.SearchType.AUTHOR);
                case "4" -> librarianBookController.searchBookCopies(librarian, query, BookCopySearchService.SearchType.ISBN);
                default -> librarianBookController.searchBookCopies(librarian, query);
            };
        }

        if (results.isEmpty()) {
            System.out.println("No books found.");
        } else {
            System.out.println("\n--- Search Results ---");
            String format = "%-7s | %-30.30s | %-20.20s | %-25.25s | %-15s%n";
            System.out.printf(format, "Copy ID", "Title", "Author", "Library (ID)", "Status");
            System.out.println(String.join("", Collections.nCopies(105, "-")));
            results.forEach(c -> System.out.printf(format,
                    c.getCopyId(),
                    c.getBook().getTitle(),
                    c.getBook().getAuthor(),
                    String.format("%s (%d)", c.getLibrary().getName(), c.getLibrary().getId()),
                    c.getState().getStatus()));
        }
    }

    private void handleLibrarianAddBookCopy(Librarian librarian) {
        System.out.print("Enter the ISBN of the book to add a new copy: ");
        String isbn = scanner.nextLine();
        try {
            librarianBookController.addBookCopy(librarian, isbn);
        } catch (Exception e) {
            System.err.println("Error adding book copy: " + e.getMessage());
        }
    }

    private void handleLibrarianMaintenance(Librarian librarian, boolean placeUnder) {
        String action = placeUnder ? "place under" : "remove from";
        Long copyId = promptForLong("Enter Copy ID to " + action + " maintenance");
        if (copyId != null) {
            boolean success = placeUnder ? librarianBookController.putCopyUnderMaintenance(librarian, copyId) : librarianBookController.removeCopyFromMaintenance(librarian, copyId);
            if (success) {
                System.out.println("Operation successful.");
            } else {
                System.err.println("Operation failed. Please check the Copy ID and its status.");
            }
        }
    }

    private void handleLibrarianChangeUsername(Librarian librarian) {
        System.out.print("Enter new username: ");
        String newUsername = scanner.nextLine();
        try {
            librarianAccountController.changeUsername(librarian, newUsername);
            System.out.println("Username updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleLibrarianChangeEmail(Librarian librarian) {
        System.out.print("Enter new email: ");
        String newEmail = scanner.nextLine();
        try {
            librarianAccountController.changeEmail(librarian, newEmail);
            System.out.println("Email updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleLibrarianChangePassword(Librarian librarian) {
        System.out.print("Enter your current password: ");
        String currentPass = scanner.nextLine();
        System.out.print("Enter your new password: ");
        String newPass = scanner.nextLine();
        try {
            librarianAccountController.changePassword(librarian, currentPass, newPass);
            System.out.println("Password updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    // --- ADMIN ACTION HANDLERS ---
    private void handleAdminActions(Admin admin, String choice) {
        switch (choice) {
            case "1":
                handleAdminAddLibrary();
                break;
            case "2":
                handleAdminUpdateLibrary();
                break;
            case "3":
                handleAdminRemoveLibrary();
                break;
            case "4":
                handleAdminListLibraries();
                break;
            case "5":
                handleAdminAddBook();
                break;
            case "6":
                handleAdminRemoveBook();
                break;
            case "7":
                handleAdminListBooks();
                break;
            case "8":
                handleAdminViewBookDetails();
                break;
            case "9":
                handleAdminRegisterLibrarian();
                break;
            case "10":
                handleAdminReassignLibrarian();
                break;
            case "11":
                handleAdminDeleteUser(admin);
                break;
            case "12":
                handleAdminListUsers();
                break;
            case "13":
                handleAdminDefaultDatabase();
                break;
            case "14":
                handleAdminRecreateDatabase();
                break;
            case "15":
                handleAdminChangeEmail(admin);
                break;
            case "16":
                handleAdminChangePassword(admin);
                break;
            case "17":
                currentUser = null;
                break;
            default:
                System.err.println("Invalid option.");
        }
    }

    private void handleAdminAddLibrary() {
        System.out.print("Enter library name: ");
        String name = scanner.nextLine();
        System.out.print("Enter address: ");
        String address = scanner.nextLine();
        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        adminBookController.addLibrary(name, address, phone, email);
    }

    private void handleAdminUpdateLibrary() {
        Long libraryId = promptForLong("Enter the ID of the library to update");
        if (libraryId == null) return;

        System.out.print("Enter new name (or press Enter to keep current): ");
        String name = scanner.nextLine();
        System.out.print("Enter new address (or press Enter to keep current): ");
        String address = scanner.nextLine();
        System.out.print("Enter new phone (or press Enter to keep current): ");
        String phone = scanner.nextLine();
        System.out.print("Enter new email (or press Enter to keep current): ");
        String email = scanner.nextLine();
        adminBookController.updateLibrary(libraryId, name, address, phone, email);
    }

    private void handleAdminRemoveLibrary() {
        Long libraryId = promptForLong("Enter the ID of the library to remove");
        if (libraryId == null) return;

        System.err.print("WARNING: This will remove the library and all its book copies. This cannot be undone. Type 'CONFIRM' to proceed: ");
        String confirmation = scanner.nextLine();
        if (confirmation.equals("CONFIRM")) {
            adminBookController.removeLibrary(libraryId);
        } else {
            System.out.println("Removal cancelled.");
        }
    }
    private void handleAdminListLibraries() {
        List<Library> libraries = adminBookController.listAllLibraries();
        if (libraries.isEmpty()) {
            System.out.println("No libraries found in the system.");
        } else {
            System.out.println("\n--- All Libraries ---");
            String format = "%-5s | %-30.30s | %-40.40s | %-20s%n";
            System.out.printf(format, "ID", "Name", "Address", "Email");
            System.out.println(String.join("", Collections.nCopies(105, "-")));
            libraries.forEach(lib -> System.out.printf(format, lib.getId(), lib.getName(), lib.getAddress(), lib.getEmail()));
        }
    }

    private void handleAdminReassignLibrarian() {
        Long librarianId = promptForLong("Enter the Librarian's User ID");
        Long libraryId = promptForLong("Enter the new Library ID to assign them to");
        if (librarianId != null && libraryId != null) {
            adminUsersController.assignLibrarianToLibrary(librarianId, libraryId);
        }
    }

    private void handleAdminAddBook() {
        System.out.println("\n--- Add New Book ---");
        System.out.print("Enter ISBN (required): ");
        String isbn = scanner.nextLine();
        System.out.print("Enter title (required): ");
        String title = scanner.nextLine();
        System.out.print("Enter author (required): ");
        String author = scanner.nextLine();
        System.out.print("Enter publication year (or press Enter to skip): ");
        String yearInput = scanner.nextLine();
        System.out.print("Enter genre (or press Enter to skip): ");
        String genre = scanner.nextLine();

        Integer publicationYear = null;
        if (!yearInput.trim().isEmpty()) {
            try {
                publicationYear = Integer.parseInt(yearInput.trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid year format. Skipping publication year.");
            }
        }

        // Use empty string instead of null for optional fields
        String genreValue = genre.trim().isEmpty() ? null : genre.trim();

        adminBookController.addBook(isbn, title, author, publicationYear, genreValue);
    }

    private void handleAdminRemoveBook() {
        System.out.print("Enter ISBN of the book to remove: ");
        String isbn = scanner.nextLine();

        if (isbn.trim().isEmpty()) {
            System.err.println("ISBN cannot be empty.");
            return;
        }

        System.err.print("WARNING: This will permanently delete the book from the system. Type 'CONFIRM' to proceed: ");
        String confirmation = scanner.nextLine();
        if (confirmation.equals("CONFIRM")) {
            adminBookController.removeBook(isbn);
        } else {
            System.out.println("Book removal cancelled.");
        }
    }

    private void handleAdminListBooks() {
        List<Book> books = adminBookController.listAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books found in the system.");
        } else {
            System.out.println("\n--- All Books ---");
            String format = "%-15s | %-30.30s | %-20.20s | %-6s | %-15s%n";
            System.out.printf(format, "ISBN", "Title", "Author", "Year", "Genre");
            System.out.println(String.join("", java.util.Collections.nCopies(95, "-")));
            for (Book book : books) {
                System.out.printf(format,
                        book.getIsbn(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getPublicationYear() != null ? book.getPublicationYear() : "N/A",
                        book.getGenre() != null && !book.getGenre().isEmpty() ? book.getGenre() : "N/A");
            }
        }
    }

    private void handleAdminViewBookDetails() {
        System.out.print("Enter ISBN of the book to view details: ");
        String isbn = scanner.nextLine();

        if (isbn.trim().isEmpty()) {
            System.err.println("ISBN cannot be empty.");
            return;
        }

        adminBookController.viewBookDetails(isbn);
    }

    private void handleAdminRegisterLibrarian() {
        System.out.print("Enter librarian's username: ");
        String username = scanner.nextLine();
        System.out.print("Enter librarian's full name: ");
        String name = scanner.nextLine();
        System.out.print("Enter librarian's email: ");
        String email = scanner.nextLine();
        System.out.print("Enter a temporary password: ");
        String password = scanner.nextLine();
        Long libraryId = promptForLong("Enter the Library ID to assign this librarian to");
        if (libraryId != null) {
            adminUsersController.registerNewLibrarian(username, name, email, password, libraryId);
        }
    }

    private void handleAdminDeleteUser(Admin admin) {
        Long userId = promptForLong("Enter the User ID to delete");
        if (userId == null) return;

        System.err.print("WARNING: This will permanently delete the user and all their associated loans and reservations. Type 'CONFIRM' to proceed: ");
        String confirmation = scanner.nextLine();
        if (confirmation.equals("CONFIRM")) {
            adminUsersController.deleteUser(admin, userId);
        } else {
            System.out.println("Deletion cancelled.");
        }
    }


    private void handleAdminListUsers() {
        List<User> users = adminUsersController.listAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            System.out.println("\n--- All Users ---");
            String format = "%-5s | %-20.20s | %-30.30s | %-15s | %-20s%n";
            System.out.printf(format, "ID", "Username", "Email", "Role", "Library ID (if any)");
            System.out.println(String.join("", Collections.nCopies(100, "-")));
            for (User u : users) {
                String libraryInfo = "";
                if (u instanceof Librarian) {
                    Library lib = ((Librarian) u).getWorkLibrary();
                    if (lib != null) {
                        libraryInfo = String.valueOf(lib.getId());
                    }
                }
                System.out.printf(format, u.getId(), u.getUsername(), u.getEmail(), u.getRole(), libraryInfo);
            }
        }
    }


    private void handleAdminRecreateDatabase() {
        System.err.println("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.err.println("!! WARNING: THIS WILL DELETE ALL DATA IN THE DATABASE !!");
        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.print("This will recreate the schema and add a single admin user. To proceed, type 'YES, RECREATE DATABASE': ");
        String confirmation = scanner.nextLine();

        if (confirmation.equals("YES, RECREATE DATABASE")) {
            adminDatabaseController.recreateSchemaAndAdmin();
        } else {
            System.out.println("Database recreation cancelled.");
        }
    }

    private void handleAdminDefaultDatabase() {
        System.out.println("\nThis will populate the database with a default set of test data.");
        System.err.println("WARNING: This should only be run on a clean database. Run the 'Recreate Database' option first if you are unsure.");
        System.out.print("Are you sure you want to continue? (y/n): ");
        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("y")) {
            adminDatabaseController.generateDefaultDatabase();
        } else {
            System.out.println("Operation cancelled.");
        }
    }

    private void handleAdminChangeEmail(Admin admin) {
        System.out.print("Enter new email: ");
        String newEmail = scanner.nextLine();
        try {
            adminAccountController.changeEmail(admin, newEmail);
            System.out.println("Email updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleAdminChangePassword(Admin admin) {
        System.out.print("Enter current password: ");
        String currentPass = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine();
        try {
            adminAccountController.changePassword(admin, currentPass, newPass);
            System.out.println("Password updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // --- UTILITY METHODS ---
    private Long promptForLong(String message) {
        System.out.print(message + ": ");
        try {
            return Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a number.");
            return null;
        }
    }
}