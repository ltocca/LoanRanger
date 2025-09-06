package dev.ltocca.loanranger.ORM.DAOInterfaces;

import dev.ltocca.loanranger.DomainModel.Library;
import java.util.List;
import java.util.Optional;

public interface ILibraryDAO {
    Library createLibrary(Library library);
    Optional<Library> getLibraryById(Long id); // only one with optional, avoid NullPointerException
    List<Library> findLibrariesByName(String name);
    List<Library> getAllLibraries();
    void updateLibrary(Library library);
    void deleteLibrary(Long id);
    void deleteLibrary (Library library);
}