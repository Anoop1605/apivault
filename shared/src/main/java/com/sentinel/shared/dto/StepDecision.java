package com.sentinel.shared.dto;

public class StepDecision {
    private final EventDTO event;

    public StepDecision(EventDTO event) {
        this.event = event;
    }

    public EventDTO getEvent() {
        return event;
    }
}
