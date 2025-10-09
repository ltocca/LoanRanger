
package dev.ltocca.loanranger.ORM;

import dev.ltocca.loanranger.domainModel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationDAOTest extends OrmIntegrationTestBase {

    private Library testLibrary;
    private Book testBook;
    private BookCopy testBookCopy;
    private Member testMember;
    private Member testMember2;

    @BeforeEach
    void setUp() throws Exception {
        executeSchemaScript();
        testLibrary = createTestLibrary();
        testBook = createTestBook();
        testBookCopy = createTestBookCopy(testBook, testLibrary);
        testMember = createTestMember();

        testMember2 = new Member();
        testMember2.setUsername("member2");
        testMember2.setName("Member Two");
        testMember2.setEmail("member2@test.com");
        testMember2.setPassword("password");
        testMember2.setRole(UserRole.MEMBER);
        userDAO.createUser(testMember2);
    }

    @Test
    void createReservation_ShouldCreateReservationSuccessfully() {
        
        Reservation reservation = new Reservation(testBookCopy, testMember);

        
        Reservation createdReservation = reservationDAO.createReservation(reservation);

        
        assertThat(createdReservation).isNotNull();
        assertThat(createdReservation.getId()).isNotNull();
        assertThat(createdReservation.getBookCopy().getCopyId()).isEqualTo(testBookCopy.getCopyId());
        assertThat(createdReservation.getMember().getId()).isEqualTo(testMember.getId());
        assertThat(createdReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void getReservationById_ShouldReturnReservationWhenExists() {
        
        Reservation reservation = createTestReservation(testBookCopy, testMember);

        
        Optional<Reservation> foundReservation = reservationDAO.getReservationById(reservation.getId());

        
        assertThat(foundReservation).isPresent();
        assertThat(foundReservation.get().getId()).isEqualTo(reservation.getId());
        assertThat(foundReservation.get().getBookCopy().getCopyId()).isEqualTo(testBookCopy.getCopyId());
    }

    @Test
    void getReservationById_ShouldReturnEmptyWhenReservationNotFound() {
        
        Optional<Reservation> foundReservation = reservationDAO.getReservationById(999L);

        
        assertThat(foundReservation).isEmpty();
    }

    @Test
    void getReservationMemberBook_ShouldReturnReservationWhenExists() {
        
        createTestReservation(testBookCopy, testMember);

        
        Optional<Reservation> foundReservation = reservationDAO.getReservationMemberBook(testMember, testBookCopy);

        
        assertThat(foundReservation).isPresent();
        assertThat(foundReservation.get().getMember().getId()).isEqualTo(testMember.getId());
        assertThat(foundReservation.get().getBookCopy().getCopyId()).isEqualTo(testBookCopy.getCopyId());
    }

    @Test
    void updateReservation_ShouldUpdateReservationInformation() {
        
        Reservation reservation = createTestReservation(testBookCopy, testMember);
        reservation.setStatus(ReservationStatus.FULFILLED);

        
        reservationDAO.updateReservation(reservation);

        
        Optional<Reservation> updatedReservation = reservationDAO.getReservationById(reservation.getId());
        assertThat(updatedReservation).isPresent();
        assertThat(updatedReservation.get().getStatus()).isEqualTo(ReservationStatus.FULFILLED);
    }

    @Test
    void updateStatus_ShouldUpdateReservationStatus() {
        
        Reservation reservation = createTestReservation(testBookCopy, testMember);

        
        reservationDAO.updateStatus(reservation.getId(), ReservationStatus.CANCELLED);

        
        Optional<Reservation> updatedReservation = reservationDAO.getReservationById(reservation.getId());
        assertThat(updatedReservation).isPresent();
        assertThat(updatedReservation.get().getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void findMemberReservations_ShouldReturnAllMemberReservations() {
        
        createTestReservation(testBookCopy, testMember);

        
        BookCopy copy2 = createTestBookCopy(testBook, testLibrary);
        createTestReservation(copy2, testMember2);

        
        List<Reservation> memberReservations = reservationDAO.findMemberReservations(testMember);

        
        assertThat(memberReservations).hasSize(1);
        assertThat(memberReservations.get(0).getMember().getId()).isEqualTo(testMember.getId());
    }

    @Test
    void findCopyReservation_ShouldReturnAllReservationsForCopy() {
        
        createTestReservation(testBookCopy, testMember);
        createTestReservation(testBookCopy, testMember2);

        
        List<Reservation> copyReservations = reservationDAO.findCopyReservation(testBookCopy);

        
        assertThat(copyReservations).hasSize(2);
        assertThat(copyReservations).allMatch(res -> res.getBookCopy().getCopyId().equals(testBookCopy.getCopyId()));
    }

    @Test
    void findCopyWaitingReservation_ShouldReturnOnlyWaitingReservations() {
        
        Reservation pendingReservation = createTestReservation(testBookCopy, testMember);

        Reservation waitingReservation = new Reservation(testBookCopy, testMember2);
        waitingReservation.setStatus(ReservationStatus.WAITING);
        reservationDAO.createReservation(waitingReservation);

        
        List<Reservation> waitingReservations = reservationDAO.findCopyWaitingReservation(testBookCopy.getCopyId());

        
        assertThat(waitingReservations).hasSize(1);
        assertThat(waitingReservations.get(0).getStatus()).isEqualTo(ReservationStatus.WAITING);
        assertThat(waitingReservations.get(0).getMember().getId()).isEqualTo(testMember2.getId());
    }

    @Test
    void findCopyPendingReservation_ShouldReturnOnlyPendingReservations() {
        
        Reservation pendingReservation = createTestReservation(testBookCopy, testMember);

        Reservation waitingReservation = new Reservation(testBookCopy, testMember2);
        waitingReservation.setStatus(ReservationStatus.WAITING);
        reservationDAO.createReservation(waitingReservation);

        
        List<Reservation> pendingReservations = reservationDAO.findCopyPendingReservation(testBookCopy.getCopyId());

        
        assertThat(pendingReservations).hasSize(1);
        assertThat(pendingReservations.get(0).getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(pendingReservations.get(0).getMember().getId()).isEqualTo(testMember.getId());
    }

    @Test
    void hasOtherPendingReservations_ShouldReturnTrueWhenOtherPendingReservationsExist() {
        
        Reservation reservation1 = createTestReservation(testBookCopy, testMember);
        Reservation reservation2 = new Reservation(testBookCopy, testMember2);
        reservation2.setStatus(ReservationStatus.PENDING);
        reservationDAO.createReservation(reservation2);

        
        boolean hasOther = reservationDAO.hasOtherPendingReservations(testBookCopy.getCopyId(), reservation1.getId());

        
        assertThat(hasOther).isTrue();
    }

    @Test
    void hasOtherPendingReservations_ShouldReturnFalseWhenNoOtherPendingReservations() {
        
        Reservation reservation = createTestReservation(testBookCopy, testMember);

        
        boolean hasOther = reservationDAO.hasOtherPendingReservations(testBookCopy.getCopyId(), reservation.getId());

        
        assertThat(hasOther).isFalse();
    }

    @Test
    void deleteReservation_ShouldRemoveReservationFromDatabase() {
        
        Reservation reservation = createTestReservation(testBookCopy, testMember);

        
        reservationDAO.deleteReservation(reservation.getId());

        
        Optional<Reservation> deletedReservation = reservationDAO.getReservationById(reservation.getId());
        assertThat(deletedReservation).isEmpty();
    }
}