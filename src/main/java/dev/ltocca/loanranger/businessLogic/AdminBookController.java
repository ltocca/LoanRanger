package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.Admin;
import dev.ltocca.loanranger.domainModel.Library;
import dev.ltocca.loanranger.ORM.LibraryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class AdminBookController {
    private final LibraryDAO libraryDAO;

    @Autowired
    public AdminBookController(LibraryDAO libraryDAO) throws SQLException {
        this.libraryDAO = libraryDAO;
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

    public void removeLibrary(Long libraryId) {
        try {
            libraryDAO.deleteLibrary(libraryId);
            System.out.println("Library with ID " + libraryId + " removed successfully.");
        } catch (Exception e) {
            System.err.println("Error removing library: " + e.getMessage());
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
}