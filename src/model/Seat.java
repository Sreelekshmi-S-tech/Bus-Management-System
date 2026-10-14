package model;

public class Seat {

    public enum Status {
        AVAILABLE,
        SELECTED,
        BOOKED
    }

    private int seatNumber;
    private Status status;
    private String passengerGender; // "MALE", "FEMALE", "OTHERS" or null if available
    private int adjacentSeatNumber;
    private String adjacentGender; // Gender in the adjacent seat, if booked
    private boolean preferenceMatch; // True if matches user's seating preference
    private boolean concession;

    public Seat(int seatNumber) {
        this.seatNumber = seatNumber;
        this.status = Status.AVAILABLE;
        this.passengerGender = null;
        this.adjacentSeatNumber = -1;
        this.adjacentGender = null;
        this.preferenceMatch = false;
        this.concession = false;
    }

    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getPassengerGender() { return passengerGender; }
    public void setPassengerGender(String passengerGender) { this.passengerGender = passengerGender; }

    public int getAdjacentSeatNumber() { return adjacentSeatNumber; }
    public void setAdjacentSeatNumber(int adjacentSeatNumber) { this.adjacentSeatNumber = adjacentSeatNumber; }

    public String getAdjacentGender() { return adjacentGender; }
    public void setAdjacentGender(String adjacentGender) { this.adjacentGender = adjacentGender; }

    public boolean isPreferenceMatch() { return preferenceMatch; }
    public void setPreferenceMatch(boolean preferenceMatch) { this.preferenceMatch = preferenceMatch; }

    public boolean isConcession() { return concession; }
    public void setConcession(boolean concession) { this.concession = concession; }

    public boolean isAvailable() { return status == Status.AVAILABLE; }
    public boolean isSelected() { return status == Status.SELECTED; }
    public boolean isBooked() { return status == Status.BOOKED; }
}
