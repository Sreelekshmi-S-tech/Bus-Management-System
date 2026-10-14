package model;

public class Booking {

    private int bookingId;
    private int groupId;
    private int passengerId;
    private int busId;
    private int seatNumber;
    private String bookingDate;
    private double baseFare;
    private int concessionPercent;
    private double fare;
    private String seatPassengerGender;
    private String status; // "BOOKED" or "CANCELLED"

    // Supplementary detail fields for views and ticket generation
    private String passengerName;
    private String passengerPhone;
    private String busName;
    private String source;
    private String destination;
    private String busDepartureTime;

    public Booking() {}

    public Booking(int bookingId, int groupId, int passengerId, int busId, int seatNumber,
                   String bookingDate, double baseFare, int concessionPercent, double fare,
                   String seatPassengerGender, String status) {
        this.bookingId = bookingId;
        this.groupId = groupId;
        this.passengerId = passengerId;
        this.busId = busId;
        this.seatNumber = seatNumber;
        this.bookingDate = bookingDate;
        this.baseFare = baseFare;
        this.concessionPercent = concessionPercent;
        this.fare = fare;
        this.seatPassengerGender = seatPassengerGender;
        this.status = status;
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public int getPassengerId() { return passengerId; }
    public void setPassengerId(int passengerId) { this.passengerId = passengerId; }

    public int getBusId() { return busId; }
    public void setBusId(int busId) { this.busId = busId; }

    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }

    public int getConcessionPercent() { return concessionPercent; }
    public void setConcessionPercent(int concessionPercent) { this.concessionPercent = concessionPercent; }

    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }

    public String getSeatPassengerGender() { return seatPassengerGender; }
    public void setSeatPassengerGender(String seatPassengerGender) { this.seatPassengerGender = seatPassengerGender; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getPassengerPhone() { return passengerPhone; }
    public void setPassengerPhone(String passengerPhone) { this.passengerPhone = passengerPhone; }

    public String getBusName() { return busName; }
    public void setBusName(String busName) { this.busName = busName; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getBusDepartureTime() { return busDepartureTime; }
    public void setBusDepartureTime(String busDepartureTime) { this.busDepartureTime = busDepartureTime; }
}
