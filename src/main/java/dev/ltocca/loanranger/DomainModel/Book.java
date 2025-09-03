package dev.ltocca.loanranger.DomainModel;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Book {
    private String isbn;
    private String title;
    private String author;
    private int publicationYear;
    private String genre = null;

    public Book(String isbn, String title, String author, int publicationYear) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
    }

    public Book(String isbn, String title, String author, int publicationYear, String genre) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.genre = genre;
    }
}