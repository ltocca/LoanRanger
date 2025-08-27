package dev.ltocca.loanranger.DomainModel;


import lombok.Getter;
import lombok.Setter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
@Getter
@Setter

public class Book implements Subject{
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private Boolean isAvailable;
    private Long libraryId;
    private List<Observer> observers = new CopyOnWriteArrayList<>();

    // TODO: implement a way to generate the id automatically

    public Book(){
        this.observers = new ArrayList<>();
        this.isAvailable = true;
    }
    public Book(Long id, String isbn, String title, String author, Long libraryId) {
        this();
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.libraryId = libraryId;
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }

    // Overriding Lombok's setter for the isAvailable member
    public void setAvailable(boolean available) {
        boolean wasUnavailable = !this.isAvailable;
        this.isAvailable = available;
        if (wasUnavailable && available) {
            notifyObservers();
        }
    }
}
