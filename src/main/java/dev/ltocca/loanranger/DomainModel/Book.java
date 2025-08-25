package dev.ltocca.loanranger.DomainModel;

import java.util.*;

public class Book {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private Boolean isAvailable;
    private Long libraryId;
    private List<Observer> observers;

    // TODO: implement a way to generate the id automatically
}
