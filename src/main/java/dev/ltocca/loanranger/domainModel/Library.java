package dev.ltocca.loanranger.domainModel;

import lombok.Data;

@Data


public class Library {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;

    public Library(){}

    public Library(Long id, String name, String address, String phone, String email) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;

    }
    public Library(String name, String address, String phone, String email) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }
}
