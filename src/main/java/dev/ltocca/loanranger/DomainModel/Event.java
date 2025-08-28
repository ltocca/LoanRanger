package dev.ltocca.loanranger.DomainModel;

import java.time.LocalDateTime;

public class Event {
    private Long id;
    private Long libraryId;
    private String title;
    private String description;
    private EventType eventType;
    private LocalDateTime eventDate;
    private String location; // optional
    private int maxCapacity;
    private int attendance;
    private Boolean isActive;

    public Event() {
        isActive = true;
        attendance = 0;
    }

    public Event(Long libraryId, String title, String description, EventType eventType, LocalDateTime eventDate, int maxCapacity, int attendance) {
        this();
        this.libraryId = libraryId;
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.maxCapacity = maxCapacity;
        this.attendance = attendance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(Long libraryId) {
        this.libraryId = libraryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getAttendance() {
        return attendance;
    }

    public void setAttendance(int attendance) {
        this.attendance = attendance;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean hasAvailableSpots() {
        return attendance < maxCapacity;
    }

    public int availableSpots() {
        return maxCapacity - attendance;
    }

    public String getEventData() {
        String sb = "Event: '" + getTitle() + "'\n" +
                "Description: " + getDescription() + "\n" +
                "Location: " + (getLocation() != null && !getLocation().isEmpty() ? getLocation() : "Not specified") + "\n" +
                "Date and time: " + getEventDate() + "\n" +
                "Is active: " + isActive() + "\n" +
                "Maximum Capacity: " + getMaxCapacity() + "\n" +
                "Available spots: " + availableSpots();
        return sb;
    }
}
