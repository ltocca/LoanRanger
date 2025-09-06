package dev.ltocca.loanranger.ORM.DAOInterfaces;

import dev.ltocca.loanranger.DomainModel.*;
import java.util.List;
import java.util.Optional;

public interface IReservationDAO {
    Reservation createReservation(Reservation reservation);
    Optional<Reservation> getReservationById(Long id);
    void updateReservation(Reservation reservation);
    void updateStatus(Reservation reservation, ReservationStatus status);
    void updateStatus(Long id, ReservationStatus status);
    void deleteReservation(Reservation reservation);
    void deleteReservation(Long id);

    List<Reservation> findMemberReservations(Member member);
    List<Reservation> findMemberReservations(Long memberId);
    List<Reservation> findBookPendingReservations(Book book);
    List<Reservation> findBookPendingReservations(String bookIsbn);
}