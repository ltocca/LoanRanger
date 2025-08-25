package dev.ltocca.loanranger.DomainModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public abstract class User {
    private Long id;
    private String name;
    private String email;
    private String passwordHash;
    private Long libraryId;

    // TODO: implement a way to generate the id automatically
    // TODO: implement a way to add the password

}
