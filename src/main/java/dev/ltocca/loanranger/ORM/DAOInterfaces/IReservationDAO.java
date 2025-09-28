package dev.ltocca.loanranger.ORM.DAOInterfaces;

import dev.ltocca.loanranger.DomainModel.*;

import java.util.List;
import java.util.Optional;

public interface IReservationDAO {
    Reservation createReservation(Reservation reservation);

    Optional<Reservation> getReservationById(Long id);

    Optional<Reservation> getReservationMemberBook(Member member, BookCopy bookCopy); // temporary name

    void updateReservation(Reservation reservation);

    void updateStatus(Reservation reservation, ReservationStatus status);

    void updateStatus(Long id, ReservationStatus status);

    void deleteReservation(Reservation reservation);

    void deleteReservation(Long id);

    List<Reservation> findMemberReservations(Member member);

    List<Reservation> findMemberReservations(Long memberId);

    List<Reservation> findCopyReservation(Long copyId);

    List<Reservation> findCopyReservation(BookCopy bookCopy);

    List<Reservation> findCopyWaitingReservation(Long copyId);

    List<Reservation> findCopyPendingReservation(Long copyId);

    List<Reservation> findReservationsByLibrary(Long libraryId);

    List<Reservation> findActiveReservationsByLibrary(Long libraryId);

    List<Reservation> findPastReservationsByLibrary(Long libraryId);

    boolean hasOtherPendingReservations(Long copyId, Long reservationIdToExclude);

}

// TODO maybe add other methods to obtain information from the table