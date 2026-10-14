package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bus {

    private int busId;
    private String busName;
    private String source;
    private String destination;
    private int totalSeats;
    private double fare;
    private LocalDateTime departureTime;
    private String busType; // "SEATER 2+2", "SLEEPER 2+1", "AC DELUXE 2+2"
    private String status;  // "SCHEDULED", "DEPARTED", "CANCELLED"

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Bus() {
        this.busType = "SEATER 2+2";
        this.status = "SCHEDULED";
        this.departureTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
    }

    public Bus(int busId, String busName, String source, String destination, int totalSeats, double fare,
               LocalDateTime departureTime, String busType, String status) {
        this.busId = busId;
        this.busName = busName;
        this.source = source;
        this.destination = destination;
        this.totalSeats = totalSeats;
        this.fare = fare;
        this.departureTime = departureTime != null ? departureTime : LocalDateTime.now().plusDays(1);
        this.busType = busType != null ? busType : "SEATER 2+2";
        this.status = status != null ? status : "SCHEDULED";
    }

    public int getBusId() { return busId; }
    public void setBusId(int busId) { this.busId = busId; }

    public String getBusName() { return busName; }
    public void setBusName(String busName) { this.busName = busName; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public String getBusType() { return busType; }
    public void setBusType(String busType) { this.busType = busType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isDeparted() {
        if ("DEPARTED".equalsIgnoreCase(status)) return true;
        if (departureTime != null && departureTime.isBefore(LocalDateTime.now())) {
            return true;
        }
        return false;
    }

    public String getFormattedDeparture() {
        return departureTime != null ? departureTime.format(FORMATTER) : "N/A";
    }

    @Override
    public String toString() {
        String depStatus = isDeparted() ? " [DEPARTED]" : (" [" + status + "]");
        return busId + " - " + busName + " (" + source + " -> " + destination + ") Dep: " + getFormattedDeparture() + depStatus;
    }
}
