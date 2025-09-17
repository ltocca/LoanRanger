package dev.ltocca.loanranger.ORM.DAOInterfaces;

import dev.ltocca.loanranger.DomainModel.Attendance;
import dev.ltocca.loanranger.DomainModel.Event;
import dev.ltocca.loanranger.DomainModel.Member;

import java.sql.SQLException;
import java.util.List;

public interface IAttendanceDAO {
    void addAttendance(Event event, Member member);

    Attendance createAttendance(Event event, Member member);

    void deleteAttendance(Event event, Member member);

    void deleteAttendance(Long eventId, Long memberId);

    boolean isMemberAttending(Event event, Member member);

    List<Member> findEventAttendees(Event event);

    List<Event> findMemberParticipation(Member member);

    List<Attendance> findMemberAttendances(Member member) throws SQLException;
}