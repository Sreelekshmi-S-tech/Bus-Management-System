package dao;

import database.DBConnection;
import model.Bus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BusDAO {

    public boolean addBus(Bus bus) {
        String sql = "INSERT INTO bus (bus_id, bus_name, source, destination, total_seats, fare, departure_time, bus_type, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bus.getBusId());
            ps.setString(2, bus.getBusName());
            ps.setString(3, bus.getSource());
            ps.setString(4, bus.getDestination());
            ps.setInt(5, bus.getTotalSeats());
            ps.setDouble(6, bus.getFare());
            ps.setTimestamp(7, bus.getDepartureTime() != null ? Timestamp.valueOf(bus.getDepartureTime()) : Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
            ps.setString(8, bus.getBusType() != null ? bus.getBusType() : "SEATER 2+2");
            ps.setString(9, bus.getStatus() != null ? bus.getStatus() : "SCHEDULED");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Bus> getAllBuses() {
        autoUpdateDepartedStatuses();
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT * FROM bus ORDER BY departure_time ASC, bus_id ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractBus(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Bus getBusById(int id) {
        String sql = "SELECT * FROM bus WHERE bus_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractBus(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateBus(Bus bus) {
        String sql = "UPDATE bus SET bus_name = ?, source = ?, destination = ?, total_seats = ?, fare = ?, " +
                     "departure_time = ?, bus_type = ?, status = ? WHERE bus_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bus.getBusName());
            ps.setString(2, bus.getSource());
            ps.setString(3, bus.getDestination());
            ps.setInt(4, bus.getTotalSeats());
            ps.setDouble(5, bus.getFare());
            ps.setTimestamp(6, bus.getDepartureTime() != null ? Timestamp.valueOf(bus.getDepartureTime()) : Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
            ps.setString(7, bus.getBusType());
            ps.setString(8, bus.getStatus());
            ps.setInt(9, bus.getBusId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBus(int id) {
        String sql = "DELETE FROM bus WHERE bus_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Auto-updates buses whose departure time has passed to status 'DEPARTED'.
     */
    public void autoUpdateDepartedStatuses() {
        String sql = "UPDATE bus SET status = 'DEPARTED' WHERE departure_time <= NOW() AND status = 'SCHEDULED'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            // Ignore background error
        }
    }

    public int getNextBusId() {
        String sql = "SELECT COALESCE(MAX(bus_id), 0) + 1 FROM bus";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private Bus extractBus(ResultSet rs) throws SQLException {
        Bus b = new Bus();
        b.setBusId(rs.getInt("bus_id"));
        b.setBusName(rs.getString("bus_name"));
        b.setSource(rs.getString("source"));
        b.setDestination(rs.getString("destination"));
        b.setTotalSeats(rs.getInt("total_seats"));
        b.setFare(rs.getDouble("fare"));

        Timestamp dep = rs.getTimestamp("departure_time");
        if (dep != null) {
            b.setDepartureTime(dep.toLocalDateTime());
        }

        b.setBusType(rs.getString("bus_type"));
        b.setStatus(rs.getString("status"));
        return b;
    }
}
