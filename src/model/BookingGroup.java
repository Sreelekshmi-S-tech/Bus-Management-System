package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BookingGroup {

    private int groupId;
    private int passengerId;
    private int busId;
    private Timestamp bookingTime;
    private double totalFare;

    // Supplementary joined fields
    private String passengerName;
    private String passengerPhone;
    private String passengerGender;
    private boolean hasDisability;
    private String disabilityIdNo;

    private String busName;
    private String source;
    private String destination;
    private String busDepartureTime;
    private String busStatus;
    private double baseFare;

    private List<Integer> seatNumbers = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();

    public BookingGroup() {}

    public BookingGroup(int groupId, int passengerId, int busId, Timestamp bookingTime, double totalFare) {
        this.groupId = groupId;
        this.passengerId = passengerId;
        this.busId = busId;
        this.bookingTime = bookingTime;
        this.totalFare = totalFare;
    }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public int getPassengerId() { return passengerId; }
    public void setPassengerId(int passengerId) { this.passengerId = passengerId; }

    public int getBusId() { return busId; }
    public void setBusId(int busId) { this.busId = busId; }

    public Timestamp getBookingTime() { return bookingTime; }
    public void setBookingTime(Timestamp bookingTime) { this.bookingTime = bookingTime; }

    public double getTotalFare() { return totalFare; }
    public void setTotalFare(double totalFare) { this.totalFare = totalFare; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getPassengerPhone() { return passengerPhone; }
    public void setPassengerPhone(String passengerPhone) { this.passengerPhone = passengerPhone; }

    public String getPassengerGender() { return passengerGender; }
    public void setPassengerGender(String passengerGender) { this.passengerGender = passengerGender; }

    public boolean isHasDisability() { return hasDisability; }
    public void setHasDisability(boolean hasDisability) { this.hasDisability = hasDisability; }

    public String getDisabilityIdNo() { return disabilityIdNo; }
    public void setDisabilityIdNo(String disabilityIdNo) { this.disabilityIdNo = disabilityIdNo; }

    public String getBusName() { return busName; }
    public void setBusName(String busName) { this.busName = busName; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getBusDepartureTime() { return busDepartureTime; }
    public void setBusDepartureTime(String busDepartureTime) { this.busDepartureTime = busDepartureTime; }

    public String getBusStatus() { return busStatus; }
    public void setBusStatus(String busStatus) { this.busStatus = busStatus; }

    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }

    public List<Integer> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<Integer> seatNumbers) { this.seatNumbers = seatNumbers; }

    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }

    public String getSeatNumbersAsString() {
        if (seatNumbers == null || seatNumbers.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < seatNumbers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(seatNumbers.get(i));
        }
        return sb.toString();
    }
}
