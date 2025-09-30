package dev.ltocca.loanranger.presentationLayer;

import dev.ltocca.loanranger.BusinessLogic.*;
import dev.ltocca.loanranger.DomainModel.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class MainCLI {
    private static final Scanner scanner = new Scanner(System.in);
    private static final LoginController loginController = new LoginController();
    private static User currentUser;

    public static void main(String[] args) {
        try {
            runPreLoginLoop();
        } catch (Exception e) {
            System.err.println("A fatal error occurred. Exiting application.");
            e.printStackTrace();
        }
    }

    private static void runPreLoginLoop() throws SQLException {
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

    private static void handleLogin() throws SQLException {
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

    private static void handleRegister() throws SQLException {
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

    private static void runPostLoginLoop() throws SQLException {
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

    private static void displayRoleBasedMenu() {
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
        } else if (currentUser instanceof Admin) {
            System.out.println("--- Admin Menu ---");
            System.out.println("--- Library Management ---");
            System.out.println("1. Add New Library");
            System.out.println("2. Update Library Information");
            System.out.println("3. Remove Library");
            System.out.println("4. List All Libraries");
            System.out.println("--- User Management ---");
            System.out.println("5. Register New Librarian");
            System.out.println("6. Re-assign Librarian");
            System.out.println("7. Delete a User Account");
            System.out.println("8. List All Users");
            System.out.println("--- System ---");
            System.out.println("9. Seed Database with Default Data");
            System.out.println("10. Recreate Database (Schema + Admin)");
            System.out.println("--- My Account ---");
            System.out.println("11. Change Email");
            System.out.println("12. Change Password");
            System.out.println("13. Logout");
        }
    }

    // --- MEMBER ACTION HANDLERS ---
    private static void handleMemberActions(Member member, String choice) throws SQLException {
        MemberBookController bookCtrl = new MemberBookController(member, new LibraryFacade());
        MemberAccountController accountCtrl = new MemberAccountController(member);

        switch (choice) {
            case "1":
                handleMemberBookSearch(bookCtrl);
                break;
            case "2":
                handleMemberReserveBook(bookCtrl);
                break;
            case "3":
                handleMemberManageLoans(bookCtrl);
                break;
            case "4":
                handleMemberViewReservations(bookCtrl);
                break;
            case "5": // New Case
                handleMemberCancelReservation(bookCtrl);
                break;
            case "6":
                handleMemberChangeUsername(accountCtrl);
                break;
            case "7":
                handleMemberChangeEmail(accountCtrl);
                break;
            case "8":
                handleMemberChangePassword(accountCtrl);
                break;
            case "9":
                handleMemberDeleteAccount(accountCtrl);
                break;
            case "10":
                currentUser = null;
                break;
            default:
                System.err.println("Invalid option.");
        }
    }

    private static void handleMemberBookSearch(MemberBookController bookCtrl) {
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
                results = bookCtrl.searchBooksByTitle(query);
                break;
            case "2":
                results = bookCtrl.searchBooksByAuthor(query);
                break;
            case "3":
                results = bookCtrl.searchBooksByIsbn(query);
                break;
            case "4":
                results = bookCtrl.searchBookCopyGeneric(query);
                break;
            default:
                System.err.println("Invalid search type. Performing smart search by default.");
                results = bookCtrl.searchBookCopyGeneric(query);
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

    private static void handleMemberReserveBook(MemberBookController bookCtrl) {
        System.out.print("Enter the Copy ID of the book to reserve: ");
        try {
            Long copyId = Long.parseLong(scanner.nextLine());
            bookCtrl.reserveBookCopy(copyId);
        } catch (NumberFormatException e) {
            System.err.println("Invalid ID format.");
        }
    }

    private static void handleMemberManageLoans(MemberBookController bookCtrl) {
        System.out.println("\n--- Manage My Loans ---");
        System.out.println("1. View Active Loans");
        System.out.println("2. View Overdue Loans");
        System.out.println("3. View Full Loan History");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        List<Loan> loans;
        switch (choice) {
            case "1":
                loans = bookCtrl.getActiveLoans();
                System.out.println("\n--- Your Active Loans ---");
                break;
            case "2":
                loans = bookCtrl.getOverdueLoans();
                System.out.println("\n--- Your Overdue Loans ---");
                break;
            case "3":
                loans = bookCtrl.getAllLoans();
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

    private static void handleMemberViewReservations(MemberBookController bookCtrl) {
        System.out.println("\n--- My Reservations ---");
        System.out.println("1. View Active (Pending) Reservations");
        System.out.println("2. View Full Reservation History");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        List<Reservation> reservations;
        switch (choice) {
            case "1":
                reservations = bookCtrl.getActiveReservations();
                System.out.println("\n--- Your Active Reservations ---");
                break;
            case "2":
                reservations = bookCtrl.getAllReservations();
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

    private static void handleMemberCancelReservation(MemberBookController bookCtrl) {
        List<Reservation> reservations = bookCtrl.getActiveReservations();
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
            if (bookCtrl.cancelReservation(reservationId)) {
                System.out.println("Reservation cancelled successfully.");
            } else {
                System.err.println("Failed to cancel reservation. Please check the ID and try again.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid ID format. Please enter a number.");
        }
    }

    private static void handleMemberChangeUsername(MemberAccountController accountCtrl) {
        System.out.print("Enter new username: ");
        String newUsername = scanner.nextLine();
        try {
            accountCtrl.changeUsername(newUsername);
            System.out.println("Username updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleMemberChangeEmail(MemberAccountController accountCtrl) {
        System.out.print("Enter new email: ");
        String newEmail = scanner.nextLine();
        try {
            accountCtrl.changeEmail(newEmail);
            System.out.println("Email updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleMemberChangePassword(MemberAccountController accountCtrl) {
        System.out.print("Enter current password: ");
        String currentPass = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine();
        try {
            accountCtrl.changePassword(currentPass, newPass);
            System.out.println("Password updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleMemberDeleteAccount(MemberAccountController accountCtrl) {
        System.out.print("Are you sure you want to delete your account? This cannot be undone. Enter your password to confirm: ");
        String password = scanner.nextLine();
        try {
            accountCtrl.deleteAccount(password);
            System.out.println("Account deleted successfully.");
            currentUser = null; // Log out after deletion
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    // --- LIBRARIAN ACTION HANDLERS ---
    private static void handleLibrarianActions(Librarian librarian, String choice) throws SQLException {
        LibrarianBookController bookCtrl = new LibrarianBookController(librarian);
        LibrarianAccountController accountCtrl = new LibrarianAccountController(librarian);
        switch (choice) {
            case "1":
                handleLibrarianLoanBook(bookCtrl);
                break;
            case "2":
                handleLibrarianProcessReturn(bookCtrl);
                break;
            case "3":
                handleLibrarianRenewLoan(bookCtrl);
                break;
            case "4":
                handleLibrarianViewLoans(bookCtrl);
                break;
            case "5":
                handleLibrarianViewReservations(bookCtrl);
                break;
            case "6":
                handleLibrarianBookSearch(bookCtrl);
                break;
            case "7":
                handleLibrarianAddBookCopy(bookCtrl);
                break;
            case "8":
                handleLibrarianMaintenance(bookCtrl, true);
                break;
            case "9":
                handleLibrarianMaintenance(bookCtrl, false);
                break;
            case "10":
                handleLibrarianChangeUsername(accountCtrl);
                break;
            case "11":
                handleLibrarianChangeEmail(accountCtrl);
                break;
            case "12":
                handleLibrarianChangePassword(accountCtrl);
                break;
            case "13":
                currentUser = null;
                break;
            default:
                System.err.println("Invalid option.");
        }
    }

    private static void handleLibrarianLoanBook(LibrarianBookController bookCtrl) {
        Long memberId = promptForLong("Enter Member ID");
        Long copyId = promptForLong("Enter Copy ID");
        if (memberId != null && copyId != null) {
            if (bookCtrl.loanBookToMember(memberId, copyId, null)) {
                System.out.println("Loan processed successfully.");
            } else {
                System.err.println("Failed to process loan. Check the IDs and book availability.");
            }
        }
    }

    private static void handleLibrarianProcessReturn(LibrarianBookController bookCtrl) {
        Long copyId = promptForLong("Enter Copy ID of returned book");
        if (copyId != null) {
            if (bookCtrl.processReturn(copyId)) {
                System.out.println("Return processed successfully.");
            } else {
                System.err.println("Failed to process return. Check the Copy ID.");
            }
        }
    }

    private static void handleLibrarianViewLoans(LibrarianBookController bookCtrl) {
        System.out.println("\n--- View Library Loans ---");
        System.out.println("1. View Active Loans");
        System.out.println("2. View Overdue Loans");
        System.out.println("3. View Full Loan History");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        List<Loan> loans;
        switch (choice) {
            case "1":
                loans = bookCtrl.getActiveLoans();
                System.out.println("\n--- Active Loans in This Library ---");
                break;
            case "2":
                loans = bookCtrl.getOverdueLoans();
                System.out.println("\n--- Overdue Loans in This Library ---");
                break;
            case "3": // New Case
                loans = bookCtrl.getAllLoans();
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

    private static void handleLibrarianViewReservations(LibrarianBookController bookCtrl) throws SQLException {
        System.out.println("\n--- View Library Reservations ---");
        System.out.println("1. View Active (Pending) Reservations");
        System.out.println("2. View Past Reservations");
        System.out.println("3. View Full Reservation History");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine();

        List<Reservation> reservations;
        switch (choice) {
            case "1":
                reservations = bookCtrl.getActiveReservations();
                System.out.println("\n--- Active Reservations in This Library ---");
                break;
            case "2":
                reservations = bookCtrl.getPastReservations();
                System.out.println("\n--- Past Reservations in This Library ---");
                break;
            case "3":
                reservations = bookCtrl.getAllReservations();
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

    private static void handleLibrarianRenewLoan(LibrarianBookController bookCtrl) {
        Long loanId = promptForLong("Enter Loan ID to renew");
        if (loanId != null) {
            if (bookCtrl.renewLoan(loanId, null)) {
                System.out.println("Loan renewed successfully.");
            } else {
                System.err.println("Failed to renew loan. Check the Loan ID.");
            }
        }
    }

    private static void handleLibrarianBookSearch(LibrarianBookController bookCtrl) {
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
                case "2" -> bookCtrl.searchBookCopiesElsewhere(query, BookCopySearchService.SearchType.TITLE);
                case "3" -> bookCtrl.searchBookCopiesElsewhere(query, BookCopySearchService.SearchType.AUTHOR);
                case "4" -> bookCtrl.searchBookCopiesElsewhere(query, BookCopySearchService.SearchType.ISBN);
                default -> bookCtrl.searchBookCopiesElsewhere(query);
            };
        } else {
            results = switch (searchTypeChoice) {
                case "2" -> bookCtrl.searchBookCopies(query, BookCopySearchService.SearchType.TITLE);
                case "3" -> bookCtrl.searchBookCopies(query, BookCopySearchService.SearchType.AUTHOR);
                case "4" -> bookCtrl.searchBookCopies(query, BookCopySearchService.SearchType.ISBN);
                default -> bookCtrl.searchBookCopies(query);
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

    private static void handleLibrarianAddBookCopy(LibrarianBookController bookCtrl) {
        System.out.print("Enter the ISBN of the book to add a new copy: ");
        String isbn = scanner.nextLine();
        try {
            bookCtrl.addBookCopy(isbn);
            System.out.println("New book copy added successfully.");
        } catch (Exception e) {
            System.err.println("Error adding book copy: " + e.getMessage());
        }
    }

    private static void handleLibrarianMaintenance(LibrarianBookController bookCtrl, boolean placeUnder) {
        String action = placeUnder ? "place under" : "remove from";
        Long copyId = promptForLong("Enter Copy ID to " + action + " maintenance");
        if (copyId != null) {
            boolean success = placeUnder ? bookCtrl.putCopyUnderMaintenance(copyId) : bookCtrl.removeCopyFromMaintenance(copyId);
            if (success) {
                System.out.println("Operation successful.");
            } else {
                System.err.println("Operation failed. Please check the Copy ID and its status.");
            }
        }
    }

    private static void handleLibrarianChangeUsername(LibrarianAccountController accountCtrl) {
        System.out.print("Enter new username: ");
        String newUsername = scanner.nextLine();
        try {
            accountCtrl.changeUsername(newUsername);
            System.out.println("Username updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleLibrarianChangeEmail(LibrarianAccountController accountCtrl) {
        System.out.print("Enter new email: ");
        String newEmail = scanner.nextLine();
        try {
            accountCtrl.changeEmail(newEmail);
            System.out.println("Email updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleLibrarianChangePassword(LibrarianAccountController accountCtrl) {
        System.out.print("Enter your current password: ");
        String currentPass = scanner.nextLine();
        System.out.print("Enter your new password: ");
        String newPass = scanner.nextLine();
        try {
            accountCtrl.changePassword(currentPass, newPass);
            System.out.println("Password updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    // --- ADMIN ACTION HANDLERS ---
    private static void handleAdminActions(Admin admin, String choice) throws SQLException { // TODO: handle possible credential updating
        AdminBookController bookCtrl = new AdminBookController(admin);
        AdminUsersController usersCtrl = new AdminUsersController(admin);
        AdminDatabaseController dbCtrl = new AdminDatabaseController(admin);
        AdminAccountController accountCtrl = new AdminAccountController(admin);
        switch (choice) {
            case "1":
                handleAdminAddLibrary(bookCtrl);
                break;
            case "2":
                handleAdminUpdateLibrary(bookCtrl);
                break;
            case "3":
                handleAdminRemoveLibrary(bookCtrl);
                break;
            case "4":
                handleAdminListLibraries(bookCtrl);
                break;
            case "5":
                handleAdminRegisterLibrarian(usersCtrl);
                break;
            case "6":
                handleAdminReassignLibrarian(usersCtrl);
                break;
            case "7":
                handleAdminDeleteUser(usersCtrl);
                break;
            case "8":
                handleAdminListUsers(usersCtrl);
                break;
            case "9":
                handleAdminDefaultDatabase(dbCtrl);
                break;
            case "10":
                handleAdminRecreateDatabase(dbCtrl);
                break;
            case "11":
                handleAdminChangeEmail(accountCtrl);
                break;
            case "12":
                handleAdminChangePassword(accountCtrl);
                break;
            case "13":
                currentUser = null;
                break;
            default:
                System.err.println("Invalid option.");
        }
    }

    private static void handleAdminAddLibrary(AdminBookController bookCtrl) {
        System.out.print("Enter library name: ");
        String name = scanner.nextLine();
        System.out.print("Enter address: ");
        String address = scanner.nextLine();
        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        bookCtrl.addLibrary(name, address, phone, email);
    }

    private static void handleAdminUpdateLibrary(AdminBookController bookCtrl) {
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
        bookCtrl.updateLibrary(libraryId, name, address, phone, email);
    }

    private static void handleAdminRemoveLibrary(AdminBookController bookCtrl) {
        Long libraryId = promptForLong("Enter the ID of the library to remove");
        if (libraryId == null) return;

        System.err.print("WARNING: This will remove the library and all its book copies. This cannot be undone. Type 'CONFIRM' to proceed: ");
        String confirmation = scanner.nextLine();
        if (confirmation.equals("CONFIRM")) {
            bookCtrl.removeLibrary(libraryId);
        } else {
            System.out.println("Removal cancelled.");
        }
    }
    private static void handleAdminListLibraries(AdminBookController bookCtrl) {
        List<Library> libraries = bookCtrl.listAllLibraries();
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

    private static void handleAdminReassignLibrarian(AdminUsersController usersCtrl) {
        Long librarianId = promptForLong("Enter the Librarian's User ID");
        Long libraryId = promptForLong("Enter the new Library ID to assign them to");
        if (librarianId != null && libraryId != null) {
            usersCtrl.assignLibrarianToLibrary(librarianId, libraryId);
        }
    }

    private static void handleAdminRegisterLibrarian(AdminUsersController usersCtrl) {
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
            usersCtrl.registerNewLibrarian(username, name, email, password, libraryId);
        }
    }

    private static void handleAdminDeleteUser(AdminUsersController usersCtrl) {
        Long userId = promptForLong("Enter the User ID to delete");
        if (userId == null) return;

        System.err.print("WARNING: This will permanently delete the user and all their associated loans and reservations. Type 'CONFIRM' to proceed: ");
        String confirmation = scanner.nextLine();
        if (confirmation.equals("CONFIRM")) {
            usersCtrl.deleteUser(userId);
        } else {
            System.out.println("Deletion cancelled.");
        }
    }


    private static void handleAdminListUsers(AdminUsersController usersCtrl) {
        List<User> users = usersCtrl.listAllUsers();
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


    private static void handleAdminRecreateDatabase(AdminDatabaseController dbCtrl) {
        System.err.println("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.err.println("!! WARNING: THIS WILL DELETE ALL DATA IN THE DATABASE !!");
        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.print("This will recreate the schema and add a single admin user. To proceed, type 'YES, RECREATE DATABASE': ");
        String confirmation = scanner.nextLine();

        if (confirmation.equals("YES, RECREATE DATABASE")) {
            dbCtrl.recreateSchemaAndAdmin();
        } else {
            System.out.println("Database recreation cancelled.");
        }
    }

    private static void handleAdminDefaultDatabase(AdminDatabaseController dbCtrl) {
        System.out.println("\nThis will populate the database with a default set of test data.");
        System.err.println("WARNING: This should only be run on a clean database. Run the 'Recreate Database' option first if you are unsure.");
        System.out.print("Are you sure you want to continue? (y/n): ");
        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("y")) {
            dbCtrl.generateDefaultDatabase();
        } else {
            System.out.println("Operation cancelled.");
        }
    }

    private static void handleAdminChangeEmail(AdminAccountController accountCtrl) {
        System.out.print("Enter new email: ");
        String newEmail = scanner.nextLine();
        try {
            accountCtrl.changeEmail(newEmail);
            System.out.println("Email updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleAdminChangePassword(AdminAccountController accountCtrl) {
        System.out.print("Enter current password: ");
        String currentPass = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine();
        try {
            accountCtrl.changePassword(currentPass, newPass);
            System.out.println("Password updated successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // --- UTILITY METHODS ---
    private static Long promptForLong(String message) {
        System.out.print(message + ": ");
        try {
            return Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a number.");
            return null;
        }
    }
}