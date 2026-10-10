package dao;

import database.DBConnection;
import model.Passenger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PassengerDAO {

    public boolean addPassenger(Passenger p) {
        String query = "INSERT INTO passenger (passenger_id, name, age, gender, phone) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {
            
            pst.setInt(1, p.getPassengerId());
            pst.setString(2, p.getName());
            pst.setInt(3, p.getAge());
            pst.setString(4, p.getGender());
            pst.setString(5, p.getPhoneNumber());

            int rows = pst.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Passenger> getAllPassengers() {
        List<Passenger> list = new ArrayList<>();
        String query = "SELECT * FROM passenger ORDER BY passenger_id ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Passenger p = new Passenger(
                        rs.getInt("passenger_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("phone")
                );
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Passenger getPassengerById(int id) {
        String query = "SELECT * FROM passenger WHERE passenger_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Passenger(
                            rs.getInt("passenger_id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("phone")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePassenger(Passenger p) {
        String query = "UPDATE passenger SET name = ?, age = ?, gender = ?, phone = ? WHERE passenger_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, p.getName());
            pst.setInt(2, p.getAge());
            pst.setString(3, p.getGender());
            pst.setString(4, p.getPhoneNumber());
            pst.setInt(5, p.getPassengerId());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePassenger(int id) {
        String query = "DELETE FROM passenger WHERE passenger_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
