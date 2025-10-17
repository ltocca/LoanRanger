package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.ORM.BookCopiesDAO;
import dev.ltocca.loanranger.ORM.BookDAO;
import dev.ltocca.loanranger.domainModel.Admin;
import dev.ltocca.loanranger.domainModel.Book;
import dev.ltocca.loanranger.domainModel.BookCopy;
import dev.ltocca.loanranger.domainModel.Library;
import dev.ltocca.loanranger.ORM.LibraryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class AdminBookController {
    private final LibraryDAO libraryDAO;
    private final BookDAO bookDAO;
    private final BookCopiesDAO bookCopiesDAO;

    @Autowired
    public AdminBookController(LibraryDAO libraryDAO, BookDAO bookDAO, BookCopiesDAO bookCopiesDAO) {
        this.libraryDAO = libraryDAO;
        this.bookDAO = bookDAO;
        this.bookCopiesDAO = bookCopiesDAO;
    }

    @Transactional
    public void addBook(String isbn, String title, String author, Integer publicationYear, String genre) {
        try {
            // Check if book already exists
            Optional<Book> existingBook = bookDAO.getBookByIsbn(isbn);
            if (existingBook.isPresent()) {
                System.err.println("Error: A book with ISBN " + isbn + " already exists.");
                return;
            }

            if (isbn == null || isbn.trim().isEmpty()) {
                throw new IllegalArgumentException("ISBN cannot be null or empty");
            }
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title cannot be null or empty");
            }
            if (author == null || author.trim().isEmpty()) {
                throw new IllegalArgumentException("Author cannot be null or empty");
            }

            Book newBook;
            if (publicationYear != null && genre != null && !genre.trim().isEmpty()) {
                newBook = new Book(isbn.trim(), title.trim(), author.trim(), publicationYear, genre.trim());
            } else if (publicationYear != null) {
                newBook = new Book(isbn.trim(), title.trim(), author.trim(), publicationYear);
            } else {
                newBook = new Book(isbn.trim(), title.trim(), author.trim());
            }

            Book createdBook = bookDAO.createBook(newBook);
            System.out.println("Book '" + title + "' added successfully with ISBN " + createdBook.getIsbn());

        } catch (Exception e) {
            System.err.println("Error adding book: " + e.getMessage());
        }
    }

    @Transactional
    public void removeBook(String isbn) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                throw new IllegalArgumentException("ISBN cannot be null or empty");
            }

            // Check if book exists
            Optional<Book> bookOpt = bookDAO.getBookByIsbn(isbn.trim());
            if (bookOpt.isEmpty()) {
                System.err.println("Error: No book found with ISBN " + isbn);
                return;
            }

            Book book = bookOpt.get();

            // Check if there are any copies of this book
            List<BookCopy> copies = bookCopiesDAO.findAllBookCopies(book);
            if (!copies.isEmpty()) {
                System.err.println("Error: Cannot remove book '" + book.getTitle() +
                        "' because there are " + copies.size() + " copies in the system.");
                System.err.println("Please remove all copies first before deleting the book.");
                return;
            }

            bookDAO.deleteBook(isbn.trim());
            System.out.println("Book '" + book.getTitle() + "' with ISBN " + isbn + " removed successfully.");

        } catch (Exception e) {
            System.err.println("Error removing book: " + e.getMessage());
        }
    }

    @Transactional
    public void addLibrary(String name, String address, String phone, String email) {
        try {
            Library newLibrary = new Library(name, address, phone, email);
            libraryDAO.createLibrary(newLibrary);
            System.out.println("Library '" + name + "' added successfully with ID " + newLibrary.getId());
        } catch (Exception e) {
            System.err.println("Error adding library: " + e.getMessage());
        }
    }

    @Transactional
    public void updateLibrary(Library library) {
        try {
            libraryDAO.updateLibrary(library);
            System.out.println("Library " + library.getId() + " updated successfully.");
        } catch (Exception e) {
            System.err.println("Error updating library: " + e.getMessage());
        }
    }

    @Transactional
    public void updateLibrary(Long libraryId, String name, String address, String phone, String email) {
        try {
            Library library = libraryDAO.getLibraryById(libraryId)
                    .orElseThrow(() -> new IllegalArgumentException("Library with ID " + libraryId + " not found."));
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

    @Transactional
    public void removeLibrary(Long libraryId) {
        try {
            libraryDAO.deleteLibrary(libraryId);
            System.out.println("Library with ID " + libraryId + " removed successfully.");
        } catch (Exception e) {
            System.err.println("Error removing library: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Library> listAllLibraries() {
        try {
            return libraryDAO.getAllLibraries();
        } catch (Exception e) {
            System.err.println("Error fetching all libraries: " + e.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<Book> listAllBooks() {
        try {
            return bookDAO.getAllBooks();
        } catch (Exception e) {
            System.err.println("Error fetching all books: " + e.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public void viewBookDetails(String isbn) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                throw new IllegalArgumentException("ISBN cannot be null or empty");
            }

            Optional<Book> bookOpt = bookDAO.getBookByIsbn(isbn.trim());
            if (bookOpt.isEmpty()) {
                System.err.println("Error: No book found with ISBN " + isbn);
                return;
            }

            Book book = bookOpt.get();
            List<BookCopy> copies = bookCopiesDAO.findAllBookCopies(book);

            System.out.println("\n--- Book Details ---");
            System.out.println("ISBN: " + book.getIsbn());
            System.out.println("Title: " + book.getTitle());
            System.out.println("Author: " + book.getAuthor());
            System.out.println("Publication Year: " +
                    (book.getPublicationYear() != null ? book.getPublicationYear() : "N/A"));
            System.out.println("Genre: " +
                    (book.getGenre() != null && !book.getGenre().isEmpty() ? book.getGenre() : "N/A"));
            System.out.println("Total Copies: " + copies.size());

            if (!copies.isEmpty()) {
                System.out.println("\nCopy Locations:");
                String format = "%-8s | %-25.25s | %-15s%n";
                System.out.printf(format, "Copy ID", "Library", "Status");
                System.out.println(String.join("", java.util.Collections.nCopies(55, "-")));

                for (BookCopy copy : copies) {
                    System.out.printf(format,
                            copy.getCopyId(),
                            copy.getLibrary().getName(),
                            copy.getState().getStatus());
                }
            }

        } catch (Exception e) {
            System.err.println("Error fetching book details: " + e.getMessage());
        }
    }
}