
package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.domainModel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserDAOTest extends OrmIntegrationTestBase {

    private Library testLibrary;

    @BeforeEach
    void setUp() throws Exception {
        executeSchemaScript();
        testLibrary = createTestLibrary();
    }

    @Test
    void createUser_ShouldCreateMemberSuccessfully() {
        
        Member member = new Member();
        member.setUsername("john_doe");
        member.setName("John Doe");
        member.setEmail("john@test.com");
        member.setPassword("password123");
        member.setRole(UserRole.MEMBER);

        
        User createdUser = userDAO.createUser(member);

        
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo("john_doe");
        assertThat(createdUser.getEmail()).isEqualTo("john@test.com");
        assertThat(createdUser.getRole()).isEqualTo(UserRole.MEMBER);
    }

    @Test
    void createUser_ShouldCreateLibrarianWithLibrary() {
        
        Librarian librarian = new Librarian();
        librarian.setUsername("lib_user");
        librarian.setPassword("password123");
        librarian.setName("Library User");
        librarian.setEmail("lib@test.com");
        librarian.setRole(UserRole.LIBRARIAN);
        librarian.setWorkLibrary(testLibrary);

        
        User createdUser = userDAO.createUser(librarian);

        
        assertThat(createdUser).isNotNull();
        assertThat(createdUser).isInstanceOf(Librarian.class);
        Librarian createdLibrarian = (Librarian) createdUser;
        assertThat(createdLibrarian.getWorkLibrary()).isNotNull();
        assertThat(createdLibrarian.getWorkLibrary().getId()).isEqualTo(testLibrary.getId());
    }

    @Test
    void createUser_ShouldCreateAdminSuccessfully() {
        
        Admin admin = new Admin();
        admin.setUsername("admin_user");
        admin.setPassword("password123");
        admin.setName("Admin User");
        admin.setEmail("admin@test.com");
        admin.setRole(UserRole.ADMIN);

        
        User createdUser = userDAO.createUser(admin);

        
        assertThat(createdUser).isNotNull();
        assertThat(createdUser).isInstanceOf(Admin.class);
        assertThat(createdUser.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void getUserById_ShouldReturnUserWhenExists() {
        
        Member member = createTestMember();

        
        Optional<User> foundUser = userDAO.getUserById(member.getId());

        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(TEST_MEMBER_USERNAME);
        assertThat(foundUser.get().getEmail()).isEqualTo(TEST_MEMBER_EMAIL);
    }

    @Test
    void getUserById_ShouldReturnEmptyWhenUserNotFound() {
        
        Optional<User> foundUser = userDAO.getUserById(999L);

        
        assertThat(foundUser).isEmpty();
    }

    @Test
    void getUserByEmail_ShouldReturnUserWhenEmailExists() {
        
        createTestMember();

        
        Optional<User> foundUser = userDAO.getUserByEmail(TEST_MEMBER_EMAIL);

        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(TEST_MEMBER_EMAIL);
    }

    @Test
    void getUserByUsername_ShouldReturnUserWhenUsernameExists() {
        
        createTestMember();

        
        Optional<User> foundUser = userDAO.getUserByUsername(TEST_MEMBER_USERNAME);

        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(TEST_MEMBER_USERNAME);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        
        createTestMember();
        createTestLibrarian(testLibrary);

        
        List<User> users = userDAO.getAllUsers();

        
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername)
                .containsExactlyInAnyOrder(TEST_MEMBER_USERNAME, TEST_LIBRARIAN_USERNAME);
    }

    @Test
    void updateUser_ShouldUpdateUserInformation() {
        
        Member member = createTestMember();
        member.setName("Updated Name");
        member.setEmail("updated@test.com");

        
        userDAO.updateUser(member);

        
        Optional<User> updatedUser = userDAO.getUserById(member.getId());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.get().getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    void updatePassword_ShouldChangeUserPassword() {
        
        Member member = createTestMember();

        
        userDAO.updatePassword(member.getId(), "newpassword");

        
        Optional<User> updatedUser = userDAO.getUserById(member.getId());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getPassword()).isEqualTo("newpassword");
    }

    @Test
    void deleteUser_ShouldRemoveUserFromDatabase() {
        
        Member member = createTestMember();

        
        userDAO.deleteUser(member.getId());

        
        Optional<User> deletedUser = userDAO.getUserById(member.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void createUser_ShouldThrowExceptionForDuplicateEmail() {
        
        Member member1 = createTestMember();

        Member member2 = new Member();
        member2.setUsername("different_user");
        member2.setName("Different User");
        member2.setEmail(TEST_MEMBER_EMAIL); 
        member2.setPassword("password");
        member2.setRole(UserRole.MEMBER);

        
        assertThrows(RuntimeException.class, () -> userDAO.createUser(member2));
    }
}