package dev.ltocca.loanranger.DomainModel;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Librarian extends User {
    private int assignedLibraryId;

    public Librarian(int userId, String firstName, String lastName, String email, String password, int assignedLibraryId) {
        super(userId, firstName, lastName, email, password, UserRole.LIBRARIAN);
        this.assignedLibraryId = assignedLibraryId;
    }

}