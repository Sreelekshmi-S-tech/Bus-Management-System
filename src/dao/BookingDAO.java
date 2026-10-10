package dao;

import database.DBConnection;
import model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public boolean addBooking(Booking b) {
        String query = "INSERT INTO booking (passenger_id, bus_id, seat_number, booking_date, fare, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pst.setInt(1, b.getPassengerId());
            pst.setInt(2, b.getBusId());
            pst.setInt(3, b.getSeatNumber());
            pst.setString(4, b.getBookingDate());
            pst.setDouble(5, b.getFare());
            pst.setString(6, b.getStatus() != null ? b.getStatus() : "CONFIRMED");

            int rows = pst.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        b.setBookingId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String query = "SELECT b.booking_id, b.passenger_id, b.bus_id, b.seat_number, b.booking_date, b.fare, b.status, " +
                       "p.name AS passenger_name, p.phone AS passenger_phone, " +
                       "bs.bus_name, bs.source, bs.destination " +
                       "FROM booking b " +
                       "LEFT JOIN passenger p ON b.passenger_id = p.passenger_id " +
                       "LEFT JOIN bus bs ON b.bus_id = bs.bus_id " +
                       "ORDER BY b.booking_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Booking b = new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("passenger_id"),
                        rs.getInt("bus_id"),
                        rs.getInt("seat_number"),
                        rs.getString("booking_date"),
                        rs.getDouble("fare"),
                        rs.getString("status")
                );
                b.setPassengerName(rs.getString("passenger_name"));
                b.setPassengerPhone(rs.getString("passenger_phone"));
                b.setBusName(rs.getString("bus_name"));
                b.setSource(rs.getString("source"));
                b.setDestination(rs.getString("destination"));

                list.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Booking getBookingById(int id) {
        String query = "SELECT b.booking_id, b.passenger_id, b.bus_id, b.seat_number, b.booking_date, b.fare, b.status, " +
                       "p.name AS passenger_name, p.phone AS passenger_phone, " +
                       "bs.bus_name, bs.source, bs.destination " +
                       "FROM booking b " +
                       "LEFT JOIN passenger p ON b.passenger_id = p.passenger_id " +
                       "LEFT JOIN bus bs ON b.bus_id = bs.bus_id " +
                       "WHERE b.booking_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Booking b = new Booking(
                            rs.getInt("booking_id"),
                            rs.getInt("passenger_id"),
                            rs.getInt("bus_id"),
                            rs.getInt("seat_number"),
                            rs.getString("booking_date"),
                            rs.getDouble("fare"),
                            rs.getString("status")
                    );
                    b.setPassengerName(rs.getString("passenger_name"));
                    b.setPassengerPhone(rs.getString("passenger_phone"));
                    b.setBusName(rs.getString("bus_name"));
                    b.setSource(rs.getString("source"));
                    b.setDestination(rs.getString("destination"));
                    return b;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Integer> getBookedSeats(int busId) {
        List<Integer> booked = new ArrayList<>();
        String query = "SELECT seat_number FROM booking WHERE bus_id = ? AND status = 'CONFIRMED'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, busId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    booked.add(rs.getInt("seat_number"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return booked;
    }

    public boolean isSeatBooked(int busId, int seatNumber) {
        String query = "SELECT COUNT(*) FROM booking WHERE bus_id = ? AND seat_number = ? AND status = 'CONFIRMED'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, busId);
            pst.setInt(2, seatNumber);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelBooking(int bookingId) {
        String query = "UPDATE booking SET status = 'CANCELLED' WHERE booking_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, bookingId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBooking(int bookingId) {
        String query = "DELETE FROM booking WHERE booking_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, bookingId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
