package model;

public class Bus {

    // Private variables (Encapsulation)
    private int busId;
    private String busName;
    private String source;
    private String destination;
    private int totalSeats;
    private double fare;

    // Constructors
    public Bus() {
    }

    public Bus(int busId, String busName, String source, String destination, int totalSeats, double fare) {
        this.busId = busId;
        this.busName = busName;
        this.source = source;
        this.destination = destination;
        this.totalSeats = totalSeats;
        this.fare = fare;
    }

    // Getter methods
    public int getBusId() {
        return busId;
    }

    public String getBusName() {
        return busName;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public double getFare() {
        return fare;
    }

    // Setter methods
    public void setBusId(int busId) {
        this.busId = busId;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    @Override
    public String toString() {
        return busId + " - " + busName + " (" + source + " -> " + destination + ")";
    }
}
