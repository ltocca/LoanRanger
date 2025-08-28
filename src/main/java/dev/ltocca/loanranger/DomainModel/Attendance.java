package dev.ltocca.loanranger.DomainModel;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter

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
}