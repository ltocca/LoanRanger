package dev.ltocca.loanranger.DomainModel;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter

public class Reservation {
    private Long id;
    private Book book; // FIXME(STATE:BookCopy): modify this member to BookCopy in order to be coherent with the State pattern
    private Member member;
    private LocalDate reservationDate;
    private ReservationStatus status;

    public Reservation(Long id, Book book, Member member, LocalDate reservationDate) {
        this.id = id;
        this.book = book;
        this.member = member;
        this.reservationDate = reservationDate;
        this.status = ReservationStatus.PENDING;
    }

    public Reservation(Book book, Member member) {
        this.book = book;
        this.member = member;
        this.reservationDate = LocalDate.now();
        setStatus(ReservationStatus.PENDING);
    }
}
