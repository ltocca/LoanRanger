package dev.ltocca.loanranger.businessLogic;

import dev.ltocca.loanranger.domainModel.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserFactoryTest {

    @Test
    void createUser_withMemberRole_returnsMemberInstance() {
        User user = UserFactory.createUser(UserRole.MEMBER, "memberUser", "Member User",
                "member@example.com", "password123", null);

        assertThat(user).isInstanceOf(Member.class);
        assertThat(user.getUsername()).isEqualTo("memberUser");
        assertThat(user.getName()).isEqualTo("Member User");
        assertThat(user.getEmail()).isEqualTo("member@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.MEMBER);
        assertThat(user.getPassword()).isNotEqualTo("password123"); // Should be hashed
        assertThat(user.getPassword()).startsWith("$2a$");
    }

    @Test
    void createUser_withAdminRole_returnsAdminInstance() {
        User user = UserFactory.createUser(UserRole.ADMIN, "adminUser", "Admin User",
                "admin@example.com", "password123", null);

        assertThat(user).isInstanceOf(Admin.class);
        assertThat(user.getUsername()).isEqualTo("adminUser");
        assertThat(user.getName()).isEqualTo("Admin User");
        assertThat(user.getEmail()).isEqualTo("admin@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(user.getPassword()).isNotEqualTo("password123"); // Should be hashed
    }

    @Test
    void createUser_withLibrarianRoleAndLibrary_returnsLibrarianInstance() {
        Library library = new Library(1L, "Main Library", "123 Main St", "555-0100", "main@library.com");

        User user = UserFactory.createUser(UserRole.LIBRARIAN, "libUser", "Librarian User",
                "lib@example.com", "password123", library);

        assertThat(user).isInstanceOf(Librarian.class);
        Librarian librarian = (Librarian) user;
        assertThat(librarian.getUsername()).isEqualTo("libUser");
        assertThat(librarian.getName()).isEqualTo("Librarian User");
        assertThat(librarian.getEmail()).isEqualTo("lib@example.com");
        assertThat(librarian.getRole()).isEqualTo(UserRole.LIBRARIAN);
        assertThat(librarian.getWorkLibrary()).isEqualTo(library);
        assertThat(librarian.getPassword()).isNotEqualTo("password123"); // Should be hashed
    }

    @Test
    void createUser_withLibrarianRoleAndNullLibrary_throwsIllegalArgumentException() {
        assertThatThrownBy(() ->
                UserFactory.createUser(UserRole.LIBRARIAN, "libUser", "Librarian User",
                        "lib@example.com", "password123", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("WorkingLibrary or its ID is null");
    }

    @Test
    void createUser_withLibrarianRoleAndLibraryWithoutId_throwsIllegalArgumentException() {
        Library libraryWithoutId = new Library("Main Library", "123 Main St", "555-0100", "main@library.com");

        assertThatThrownBy(() ->
                UserFactory.createUser(UserRole.LIBRARIAN, "libUser", "Librarian User",
                        "lib@example.com", "password123", libraryWithoutId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("WorkingLibrary or its ID is null");
    }

    @Test
    void createUser_withInvalidRole_throwsIllegalArgumentException() {
        assertThatThrownBy(() ->
                UserFactory.createUser(null, "user", "User", "user@example.com", "password123", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role");
    }

    @Test
    void createUser_hashesPassword() {
        String plainPassword = "plainTextPassword";

        User user = UserFactory.createUser(UserRole.MEMBER, "testUser", "Test User",
                "test@example.com", plainPassword, null);

        assertThat(user.getPassword()).isNotEqualTo(plainPassword);
        assertThat(user.getPassword()).startsWith("$2a$");
        // Verify the hash is valid by checking if we can validate the original password
        assertThat(dev.ltocca.loanranger.util.PasswordHasher.check(plainPassword, user.getPassword())).isTrue();
    }

    @Test
    void createUser_withEmptyPassword_hashesEmptyPassword() {
        User user = UserFactory.createUser(UserRole.MEMBER, "testUser", "Test User",
                "test@example.com", "", null);

        assertThat(user.getPassword()).isNotEqualTo("");
        assertThat(user.getPassword()).startsWith("$2a$");
        assertThat(dev.ltocca.loanranger.util.PasswordHasher.check("", user.getPassword())).isTrue();
    }

    @Test
    void createUser_trimsInputFields() {
        User user = UserFactory.createUser(UserRole.MEMBER, "  user  ", "  User Name  ",
                "  user@example.com  ", "  password  ", null);

        assertThat(user.getUsername()).isEqualTo("user");
        assertThat(user.getName()).isEqualTo("User Name");
        assertThat(user.getEmail()).isEqualTo("user@example.com");
    }
}