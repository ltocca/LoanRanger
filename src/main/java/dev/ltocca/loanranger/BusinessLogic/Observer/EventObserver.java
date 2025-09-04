package dev.ltocca.loanranger.BusinessLogic.Observer;

import dev.ltocca.loanranger.DomainModel.Event;

public interface EventObserver {
    void onEventUpdate(Event event);
}