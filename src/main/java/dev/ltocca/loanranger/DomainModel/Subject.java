package dev.ltocca.loanranger.DomainModel;

public interface Subject {

    void addObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers(); // notify() method used by threads
}

