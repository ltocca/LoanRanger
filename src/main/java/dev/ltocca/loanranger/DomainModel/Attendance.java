package dev.ltocca.loanranger.DomainModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor

public class Attendance {
    private long id;
    private long eventId;
    private long clientId;
    private LocalDateTime registrationDate;
    private boolean attended;

    public Attendance() {
        this.registrationDate = LocalDateTime.now();
        this.attended = false;
    }

    public Attendance(Long eventId, Long userId) {
        this();
        this.eventId = eventId;
        this.clientId = userId;
    }

//    public Attendance(long id, long eventId, Long clientId, LocalDateTime registrationDate, boolean attended) {
//        this.id = id;
//        this.eventId = eventId;
//        this.clientId = clientId;
//        this.registrationDate = registrationDate;
//        this.attended = attended;
//    }
}