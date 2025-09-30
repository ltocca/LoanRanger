package dev.ltocca.loanranger.domainModel;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter

public class Reservation {
    private Long id;
    private BookCopy bookCopy;
    private Member member;
    private LocalDate reservationDate;
    private ReservationStatus status;

    public Reservation(Long id, BookCopy bookCopy, Member member, LocalDate reservationDate) {
        this.id = id;
        this.bookCopy = bookCopy;
        this.member = member;
        this.reservationDate = reservationDate;
        this.status = ReservationStatus.PENDING;
    }

    public Reservation(BookCopy bookCopy, Member member) {
        this.bookCopy = bookCopy;
        this.member = member;
        this.reservationDate = LocalDate.now();
        setStatus(ReservationStatus.PENDING);
    }
}
