/*
package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.Admin;
import dev.ltocca.loanranger.domainModel.Library;
import dev.ltocca.loanranger.domainModel.UserRole;
import dev.ltocca.loanranger.ORM.LibraryDAO;
import dev.ltocca.loanranger.ORM.UserDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUsersControllerTest {

    @Mock
    private LibraryDAO libraryDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private LoginController loginController;

    @InjectMocks
    private AdminUsersController adminUsersController;

    @Test
    void registerNewLibrarian_whenLibraryExists_registersUser() throws Exception {
        // Given
        Long libraryId = 1L;
        Library library = new Library(libraryId, "Test Library", "Address", "Phone", "Email");
        when(libraryDAO.getLibraryById(libraryId)).thenReturn(Optional.of(library));

        // When
        adminUsersController.registerNewLibrarian("newlib", "New Librarian", "new@lib.com", "password", libraryId);

        // Then
        verify(loginController).register(
                eq(UserRole.LIBRARIAN),
                eq("newlib"),
                eq("New Librarian"),
                eq("new@lib.com"),
                eq("password"),
                eq(library)
        );
    }

    @Test
    void registerNewLibrarian_whenLibraryDoesNotExist_doesNotRegisterUser() throws Exception {
        // Given
        Long libraryId = 99L;
        when(libraryDAO.getLibraryById(libraryId)).thenReturn(Optional.empty());

        // When
        adminUsersController.registerNewLibrarian("newlib", "New Librarian", "new@lib.com", "password", libraryId);

        // Then
        verify(loginController, never()).register(any(), any(), any(), any(), any(), any());
    }

    @Test
    void deleteUser_whenNotDeletingSelf_deletesUser() throws Exception {
        // Given
        Admin currentAdmin = new Admin(1L, "admin", "Admin");
        Long userToDeleteId = 2L;

        // When
        adminUsersController.deleteUser(currentAdmin, userToDeleteId);

        // Then
        verify(userDAO).deleteUser(userToDeleteId);
    }

    @Test
    void deleteUser_whenDeletingSelf_doesNotDeleteUser() throws Exception {
        // Given
        Admin currentAdmin = new Admin(1L, "admin", "Admin");
        Long userToDeleteId = 1L;

        // When
        adminUsersController.deleteUser(currentAdmin, userToDeleteId);

        // Then
        verify(userDAO, never()).deleteUser(anyLong());
    }
}*/

package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.Admin;
import dev.ltocca.loanranger.domainModel.Librarian;
import dev.ltocca.loanranger.domainModel.Library;
import dev.ltocca.loanranger.domainModel.User;
import dev.ltocca.loanranger.domainModel.UserRole;
import dev.ltocca.loanranger.ORM.LibraryDAO;
import dev.ltocca.loanranger.ORM.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUsersControllerTest {

    @Mock
    private LibraryDAO libraryDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private LoginController loginController;

    @InjectMocks
    private AdminUsersController adminUsersController;

    private Admin admin;
    private Library library;
    private Librarian librarian;

    @BeforeEach
    void setUp() {
        admin = new Admin(1L, "adminUser", "Admin User", "admin@example.com", "hashed_password");
        library = new Library(1L, "Main Library", "123 Main St", "555-0100", "main@library.com");
        librarian = new Librarian(2L, "librarianUser", "Librarian User", "lib@example.com", "hashed_password", library);
    }


    @Test
    void registerNewLibrarian_whenLibraryExists_registersUser() throws Exception {
        // Given
        Long libraryId = 1L;
        Library library = new Library(libraryId, "Test Library", "Address", "Phone", "Email");
        when(libraryDAO.getLibraryById(libraryId)).thenReturn(Optional.of(library));

        // When
        adminUsersController.registerNewLibrarian("newlib", "New Librarian", "new@lib.com", "password", libraryId);

        // Then
        verify(loginController).register(
                eq(UserRole.LIBRARIAN),
                eq("newlib"),
                eq("New Librarian"),
                eq("new@lib.com"),
                eq("password"),
                eq(library)
        );
    }

    @Test
    void registerNewLibrarian_whenLibraryDoesNotExist_doesNotRegisterUser() throws Exception {
        // Given
        Long libraryId = 99L;
        when(libraryDAO.getLibraryById(libraryId)).thenReturn(Optional.empty());

        // When
        adminUsersController.registerNewLibrarian("newlib", "New Librarian", "new@lib.com", "password", libraryId);

        // Then
        verify(loginController, never()).register(any(), any(), any(), any(), any(), any());
    }

    @Test
    void deleteUser_whenNotDeletingSelf_deletesUser() throws Exception {
        // Given
        Admin currentAdmin = new Admin(1L, "admin", "Admin");
        Long userToDeleteId = 2L;

        // When
        adminUsersController.deleteUser(currentAdmin, userToDeleteId);

        // Then
        verify(userDAO).deleteUser(userToDeleteId);
    }

    @Test
    void deleteUser_whenDeletingSelf_doesNotDeleteUser() throws Exception {
        // Given
        Admin currentAdmin = new Admin(1L, "admin", "Admin");
        Long userToDeleteId = 1L;

        // When
        adminUsersController.deleteUser(currentAdmin, userToDeleteId);

        // Then
        verify(userDAO, never()).deleteUser(anyLong());
    }

    @Test
    void registerNewLibrarian_withValidData_registersSuccessfully() throws Exception {
        when(libraryDAO.getLibraryById(1L)).thenReturn(Optional.of(library));
        when(loginController.register(eq(UserRole.LIBRARIAN), anyString(), anyString(), anyString(), anyString(), any(Library.class)))
                .thenReturn(librarian);

        adminUsersController.registerNewLibrarian("newlib", "New Librarian", "new@lib.com", "password", 1L);

        verify(loginController).register(
                eq(UserRole.LIBRARIAN),
                eq("newlib"),
                eq("New Librarian"),
                eq("new@lib.com"),
                eq("password"),
                eq(library)
        );
    }

    @Test
    void registerNewLibrarian_forNonExistentLibrary_failsRegistration() throws Exception {
        when(libraryDAO.getLibraryById(999L)).thenReturn(Optional.empty());

        adminUsersController.registerNewLibrarian("newlib", "New Librarian", "new@lib.com", "password", 999L);

        verify(loginController, never()).register(any(), any(), any(), any(), any(), any());
    }

    @Test
    void deleteUser_deletesAnotherUserSuccessfully() {
        Long userToDeleteId = 2L;

        adminUsersController.deleteUser(admin, userToDeleteId);

        verify(userDAO).deleteUser(userToDeleteId);
    }

    @Test
    void deleteUser_whenDeletingSelf_failsDeletion() {
        Long selfId = 1L;

        adminUsersController.deleteUser(admin, selfId);

        verify(userDAO, never()).deleteUser(anyLong());
    }

    @Test
    void deleteUser_withNonExistentUserId_doesNothing() {
        Long nonExistentId = 999L;

        adminUsersController.deleteUser(admin, nonExistentId);

        verify(userDAO).deleteUser(nonExistentId);
    }

    @Test
    void assignLibrarianToLibrary_withValidIds_updatesSuccessfully() {
        when(userDAO.getUserById(2L)).thenReturn(Optional.of(librarian));

        adminUsersController.assignLibrarianToLibrary(2L, 1L);

        verify(userDAO).librarianUpdateLibrary(librarian, 1L);
    }

    @Test
    void assignLibrarian_withNonLibrarianUser_failsAssignment() {
        User member = new dev.ltocca.loanranger.domainModel.Member(3L, "memberUser", "Member User", "member@example.com", "hashed_password");
        when(userDAO.getUserById(3L)).thenReturn(Optional.of(member));

        adminUsersController.assignLibrarianToLibrary(3L, 1L);

        verify(userDAO, never()).librarianUpdateLibrary(any(), anyLong());
    }

    @Test
    void assignLibrarian_toNonExistentLibrary_failsAssignment() {
        when(userDAO.getUserById(2L)).thenReturn(Optional.of(librarian));

        adminUsersController.assignLibrarianToLibrary(2L, 999L);

        verify(userDAO).librarianUpdateLibrary(librarian, 999L);
    }

    @Test
    void listAllUsers_whenUsersExist_returnsListOfUsers() {
        List<User> users = List.of(admin, librarian);
        when(userDAO.getAllUsers()).thenReturn(users);

        List<User> result = adminUsersController.listAllUsers();

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).contains(admin, librarian);
    }

    @Test
    void listAllUsers_whenNoUsersExist_returnsEmptyList() {
        when(userDAO.getAllUsers()).thenReturn(List.of());

        List<User> result = adminUsersController.listAllUsers();

        assertThat(result).isEmpty();
    }
}