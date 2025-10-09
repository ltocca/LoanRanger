package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.domainModel.Library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryDAOTest extends OrmIntegrationTestBase {

    @BeforeEach
    void setUp() throws Exception {
        executeSchemaScript();
    }

    @Test
    void createLibrary_ShouldCreateLibrarySuccessfully() {
        Library library = new Library();
        library.setName("Central Library");
        library.setAddress("123 Main Street");
        library.setPhone("555-1234");
        library.setEmail("central@library.com");

        Library createdLibrary = libraryDAO.createLibrary(library);

        assertThat(createdLibrary).isNotNull();
        assertThat(createdLibrary.getId()).isNotNull().isPositive();
        assertThat(createdLibrary.getName()).isEqualTo("Central Library");
        assertThat(createdLibrary.getAddress()).isEqualTo("123 Main Street");
        assertThat(createdLibrary.getPhone()).isEqualTo("555-1234");
        assertThat(createdLibrary.getEmail()).isEqualTo("central@library.com");
    }

    @Test
    void getLibraryById_ShouldReturnLibraryWhenExists() {
        Library library = createTestLibrary();
        Long libraryId = library.getId();

        Optional<Library> foundLibrary = libraryDAO.getLibraryById(libraryId);


        assertThat(foundLibrary).isPresent();
        assertThat(foundLibrary.get().getId()).isEqualTo(libraryId);
        assertThat(foundLibrary.get().getName()).isEqualTo(TEST_LIB_NAME);
        assertThat(foundLibrary.get().getAddress()).isEqualTo(TEST_LIB_ADDRESS);
        assertThat(foundLibrary.get().getPhone()).isEqualTo(TEST_LIB_PHONE);
        assertThat(foundLibrary.get().getEmail()).isEqualTo(TEST_LIB_EMAIL);
    }

    @Test
    void getLibraryById_ShouldReturnEmptyWhenLibraryNotFound() {
        Optional<Library> foundLibrary = libraryDAO.getLibraryById(999L);

        assertThat(foundLibrary).isEmpty();
    }

    @Test
    void findLibrariesByName_ShouldReturnMatchingLibraries() {
        Library library1 = createTestLibrary(); // "Test Library"

        Library library2 = new Library();
        library2.setName("Downtown Branch");
        library2.setAddress("456 Downtown Ave");
        library2.setPhone("555-5678");
        library2.setEmail("downtown@library.com");
        libraryDAO.createLibrary(library2);

        Library library3 = new Library();
        library3.setName("Test Library West");
        library3.setAddress("789 West St");
        library3.setPhone("555-9012");
        library3.setEmail("west@library.com");
        libraryDAO.createLibrary(library3);

        List<Library> foundLibraries = libraryDAO.findLibrariesByName("Test Library");

        assertThat(foundLibraries).hasSize(2);
        assertThat(foundLibraries).extracting(Library::getName)
                .containsExactlyInAnyOrder("Test Library", "Test Library West");
    }

    @Test
    void findLibrariesByName_ShouldBeCaseInsensitive() {
        createTestLibrary();
        List<Library> foundLibraries = libraryDAO.findLibrariesByName("test library");

        assertThat(foundLibraries).hasSize(1);
        assertThat(foundLibraries.get(0).getName()).isEqualTo("Test Library");
    }

    @Test
    void findLibrariesByName_ShouldReturnEmptyListWhenNoMatches() {
        createTestLibrary();

        List<Library> foundLibraries = libraryDAO.findLibrariesByName("NonExistent Library");

        assertThat(foundLibraries).isEmpty();
    }

    @Test
    void getAllLibraries_ShouldReturnAllLibraries() {
        Library library1 = createTestLibrary();

        Library library2 = new Library();
        library2.setName("Second Library");
        library2.setAddress("456 Second St");
        library2.setPhone("555-0002");
        library2.setEmail("second@library.com");
        libraryDAO.createLibrary(library2);

        Library library3 = new Library();
        library3.setName("Third Library");
        library3.setAddress("789 Third St");
        library3.setPhone("555-0003");
        library3.setEmail("third@library.com");
        libraryDAO.createLibrary(library3);

        List<Library> allLibraries = libraryDAO.getAllLibraries();

        assertThat(allLibraries).hasSize(3);
        assertThat(allLibraries).extracting(Library::getName)
                .containsExactlyInAnyOrder("Test Library", "Second Library", "Third Library");
    }

    @Test
    void getAllLibraries_ShouldReturnEmptyListWhenNoLibraries() {
        List<Library> allLibraries = libraryDAO.getAllLibraries();

        assertThat(allLibraries).isEmpty();
    }

    @Test
    void updateLibrary_ShouldUpdateAllFields() {
        Library library = createTestLibrary();
        library.setName("Updated Library Name");
        library.setAddress("Updated Address");
        library.setPhone("555-9999");
        library.setEmail("updated@library.com");

        libraryDAO.updateLibrary(library);

        Optional<Library> updatedLibrary = libraryDAO.getLibraryById(library.getId());
        assertThat(updatedLibrary).isPresent();
        assertThat(updatedLibrary.get().getName()).isEqualTo("Updated Library Name");
        assertThat(updatedLibrary.get().getAddress()).isEqualTo("Updated Address");
        assertThat(updatedLibrary.get().getPhone()).isEqualTo("555-9999");
        assertThat(updatedLibrary.get().getEmail()).isEqualTo("updated@library.com");
    }

    @Test
    void updateLibrary_ShouldUpdatePartialFields() {
        Library library = createTestLibrary();
        String originalAddress = library.getAddress();
        String originalPhone = library.getPhone();

        library.setName("Only Name Updated");

        libraryDAO.updateLibrary(library);

        Optional<Library> updatedLibrary = libraryDAO.getLibraryById(library.getId());
        assertThat(updatedLibrary).isPresent();
        assertThat(updatedLibrary.get().getName()).isEqualTo("Only Name Updated");
        assertThat(updatedLibrary.get().getAddress()).isEqualTo(originalAddress); // Unchanged
        assertThat(updatedLibrary.get().getPhone()).isEqualTo(originalPhone); // Unchanged
        assertThat(updatedLibrary.get().getEmail()).isEqualTo(TEST_LIB_EMAIL); // Unchanged
    }

    @Test
    void deleteLibrary_ShouldRemoveLibraryFromDatabase() {
        Library library = createTestLibrary();
        Long libraryId = library.getId();

        libraryDAO.deleteLibrary(libraryId);

        Optional<Library> deletedLibrary = libraryDAO.getLibraryById(libraryId);
        assertThat(deletedLibrary).isEmpty();
    }

    @Test
    void deleteLibraryByObject_ShouldRemoveLibraryFromDatabase() {
        Library library = createTestLibrary();
        Long libraryId = library.getId();

        libraryDAO.deleteLibrary(library);

        Optional<Library> deletedLibrary = libraryDAO.getLibraryById(libraryId);
        assertThat(deletedLibrary).isEmpty();
    }

    @Test
    void createMultipleLibraries_ShouldGenerateUniqueIds() {
        Library library1 = new Library();
        library1.setName("Library One");
        library1.setAddress("Address One");
        library1.setPhone("555-0001");
        library1.setEmail("one@library.com");

        Library library2 = new Library();
        library2.setName("Library Two");
        library2.setAddress("Address Two");
        library2.setPhone("555-0002");
        library2.setEmail("two@library.com");

        Library library3 = new Library();
        library3.setName("Library Three");
        library3.setAddress("Address Three");
        library3.setPhone("555-0003");
        library3.setEmail("three@library.com");

        Library created1 = libraryDAO.createLibrary(library1);
        Library created2 = libraryDAO.createLibrary(library2);
        Library created3 = libraryDAO.createLibrary(library3);

        assertThat(created1.getId()).isNotNull();
        assertThat(created2.getId()).isNotNull();
        assertThat(created3.getId()).isNotNull();

        assertThat(created1.getId()).isNotEqualTo(created2.getId());
        assertThat(created2.getId()).isNotEqualTo(created3.getId());
        assertThat(created1.getId()).isNotEqualTo(created3.getId());

        // IDs should be in increasing order (typical for sequences)
        assertThat(created1.getId()).isLessThan(created2.getId());
        assertThat(created2.getId()).isLessThan(created3.getId());
    }

    @Test
    void updateNonExistentLibrary_ShouldNotThrowException() {
        Library nonExistentLibrary = new Library();
        nonExistentLibrary.setId(999L);
        nonExistentLibrary.setName("Non Existent");
        nonExistentLibrary.setAddress("Nowhere");
        nonExistentLibrary.setPhone("000-0000");
        nonExistentLibrary.setEmail("none@library.com");

        libraryDAO.updateLibrary(nonExistentLibrary);

        Optional<Library> foundLibrary = libraryDAO.getLibraryById(999L);
        assertThat(foundLibrary).isEmpty();
    }

    @Test
    void deleteNonExistentLibrary_ShouldNotThrowException() {
        libraryDAO.deleteLibrary(999L);
    }

    @Test
    void createLibraryWithMinimumRequiredFields_ShouldSucceed() {
        Library library = new Library();
        library.setName("Minimal Library");

        Library createdLibrary = libraryDAO.createLibrary(library);

        assertThat(createdLibrary).isNotNull();
        assertThat(createdLibrary.getId()).isNotNull();
        assertThat(createdLibrary.getName()).isEqualTo("Minimal Library");
    }

    @Test
    void findLibrariesByNameWithPartialMatch_ShouldReturnResults() {
        Library library1 = new Library();
        library1.setName("New York Public Library");
        libraryDAO.createLibrary(library1);

        Library library2 = new Library();
        library2.setName("Brooklyn Public Library");
        libraryDAO.createLibrary(library2);

        Library library3 = new Library();
        library3.setName("Chicago Public Library");
        libraryDAO.createLibrary(library3);

        List<Library> yorkLibraries = libraryDAO.findLibrariesByName("York");
        List<Library> publicLibraries = libraryDAO.findLibrariesByName("Public");
        List<Library> brooklynLibraries = libraryDAO.findLibrariesByName("Brooklyn");

        assertThat(yorkLibraries).hasSize(1);
        assertThat(yorkLibraries.get(0).getName()).isEqualTo("New York Public Library");

        assertThat(publicLibraries).hasSize(3);

        assertThat(brooklynLibraries).hasSize(1);
        assertThat(brooklynLibraries.get(0).getName()).isEqualTo("Brooklyn Public Library");
    }
}