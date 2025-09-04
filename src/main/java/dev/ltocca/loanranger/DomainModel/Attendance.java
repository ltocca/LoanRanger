package dev.ltocca.loanranger.DomainModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class Attendance {
    private long id;
    private Event event;
    private Member member;

    // Maybe the id is not necessary
}