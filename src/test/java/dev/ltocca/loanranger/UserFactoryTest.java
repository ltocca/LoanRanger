package dev.ltocca.loanranger;

import dev.ltocca.loanranger.businessLogic.*;

import dev.ltocca.loanranger.domainModel.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserFactoryTest {

    @Test
    void createUser_shouldCreateMember() {
        User user = UserFactory.createUser(UserRole.MEMBER, "testuser", "Test User",
                "test@example.com", "password", null);

        assertThat(user).isInstanceOf(Member.class);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getRole()).isEqualTo(UserRole.MEMBER);
    }

    @Test
    void createUser_shouldCreateAdmin() {
        User user = UserFactory.createUser(UserRole.ADMIN, "admin", "Admin User",
                "admin@example.com", "password", null);

        assertThat(user).isInstanceOf(Admin.class);
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void createUser_shouldCreateLibrarianWithLibrary() {
        Library library = new Library(1L, "Central Library", "123 Main St", "555-0100", "central@lib.com");

        User user = UserFactory.createUser(UserRole.LIBRARIAN, "librarian", "Lib User",
                "librarian@example.com", "password", library);

        assertThat(user).isInstanceOf(Librarian.class);
        assertThat(((Librarian) user).getWorkLibrary()).isEqualTo(library);
    }

    @Test
    void createUser_shouldThrowExceptionWhenLibrarianWithoutLibrary() {
        assertThatThrownBy(() ->
                UserFactory.createUser(UserRole.LIBRARIAN, "librarian", "Lib User",
                        "librarian@example.com", "password", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("WorkingLibrary");
    }

    @Test
    void createUser_shouldThrowExceptionWhenLibrarianWithLibraryWithoutId() {
        Library library = new Library("Central Library", "123 Main St", "555-0100", "central@lib.com");

        assertThatThrownBy(() ->
                UserFactory.createUser(UserRole.LIBRARIAN, "librarian", "Lib User",
                        "lib@example.com", "password", library))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID");
    }

    @Test
    void createUser_shouldHashPassword() {
        String plainPassword = "password123";
        User user = UserFactory.createUser(UserRole.MEMBER, "testuser", "Test User",
                "test@example.com", plainPassword, null);

        assertThat(user.getPassword()).isNotEqualTo(plainPassword);
        assertThat(user.getPassword()).startsWith("$2a$");
    }
}
