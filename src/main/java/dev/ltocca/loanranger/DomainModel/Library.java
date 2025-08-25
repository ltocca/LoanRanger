package dev.ltocca.loanranger.DomainModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Library {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;

    public Library(){}
    // TODO: implement a way to generate the id automatically
    public Library(String name, String address, String phone, String email) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }
}
