package dao;

import database.DBConnection;
import model.Passenger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerDAO {

    public boolean addPassenger(Passenger passenger) {
        String sql = "INSERT INTO passenger (passenger_id, user_id, name, age, gender, phone, has_disability, disability_id_no) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, passenger.getPassengerId());
            if (passenger.getUserId() != null) {
                ps.setInt(2, passenger.getUserId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, passenger.getName());
            ps.setInt(4, passenger.getAge());
            ps.setString(5, passenger.getGender());
            ps.setString(6, passenger.getPhoneNumber());
            ps.setBoolean(7, passenger.isHasDisability());
            ps.setString(8, passenger.getDisabilityIdNo());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Passenger> getAllPassengers() {
        List<Passenger> list = new ArrayList<>();
        String sql = "SELECT * FROM passenger ORDER BY passenger_id ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractPassenger(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Passenger getPassengerById(int id) {
        String sql = "SELECT * FROM passenger WHERE passenger_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractPassenger(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Passenger getPassengerByUserId(int userId) {
        String sql = "SELECT * FROM passenger WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractPassenger(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePassenger(Passenger passenger) {
        String sql = "UPDATE passenger SET name = ?, age = ?, gender = ?, phone = ?, has_disability = ?, disability_id_no = ? " +
                     "WHERE passenger_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, passenger.getName());
            ps.setInt(2, passenger.getAge());
            ps.setString(3, passenger.getGender());
            ps.setString(4, passenger.getPhoneNumber());
            ps.setBoolean(5, passenger.isHasDisability());
            ps.setString(6, passenger.getDisabilityIdNo());
            ps.setInt(7, passenger.getPassengerId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePassenger(int id) {
        String sql = "DELETE FROM passenger WHERE passenger_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getNextPassengerId() {
        String sql = "SELECT COALESCE(MAX(passenger_id), 100) + 1 FROM passenger";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 101;
    }

    private Passenger extractPassenger(ResultSet rs) throws SQLException {
        Passenger p = new Passenger();
        p.setPassengerId(rs.getInt("passenger_id"));
        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) {
            p.setUserId(uid);
        }
        p.setName(rs.getString("name"));
        p.setAge(rs.getInt("age"));
        p.setGender(rs.getString("gender"));
        p.setPhoneNumber(rs.getString("phone"));
        p.setHasDisability(rs.getBoolean("has_disability"));
        p.setDisabilityIdNo(rs.getString("disability_id_no"));
        return p;
    }
}
