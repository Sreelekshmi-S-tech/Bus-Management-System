package dao;

import database.DBConnection;
import model.Booking;
import model.BookingGroup;
import model.Passenger;
import model.Seat;
import service.FareCalculator;
import service.SeatService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class BookingDAO {

    /**
     * Retrieves the seat layout map for a given bus, including occupancy and passenger gender.
     */
    public Map<Integer, Seat> getSeatsForBus(int busId, int totalSeats) {
        Map<Integer, Seat> seatMap = new HashMap<>();
        for (int i = 1; i <= totalSeats; i++) {
            seatMap.put(i, new Seat(i));
        }

        String sql = "SELECT seat_number, seat_passenger_gender, concession_percent " +
                     "FROM booking WHERE bus_id = ? AND status = 'BOOKED'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, busId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int seatNum = rs.getInt("seat_number");
                    String gender = rs.getString("seat_passenger_gender");
                    int concession = rs.getInt("concession_percent");

                    Seat s = seatMap.get(seatNum);
                    if (s != null) {
                        s.setStatus(Seat.Status.BOOKED);
                        s.setPassengerGender(gender);
                        if (concession > 0) {
                            s.setConcession(true);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Calculate adjacent neighbour relationships and genders
        SeatService.populateNeighbourInfo(seatMap, totalSeats);

        return seatMap;
    }

    /**
     * Executes atomic multi-seat booking in a single transaction with row locking.
     */
    public BookingGroup bookSeats(int passengerId, int busId, List<Integer> seatNumbers, String seatGender)
            throws SQLException, IllegalStateException {

        if (seatNumbers == null || seatNumbers.isEmpty()) {
            throw new IllegalArgumentException("No seats selected for booking.");
        }
        if (seatNumbers.size() > 6) {
            throw new IllegalArgumentException("Maximum 6 seats permitted per transaction.");
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Begin transaction

            // 1. Lock bus row and verify bus has not departed
            String busCheckSql = "SELECT bus_name, source, destination, fare, departure_time, status " +
                                 "FROM bus WHERE bus_id = ? FOR UPDATE";
            double baseFare = 0.0;
            Timestamp departureTs = null;
            String busStatus = null;
            String busName = "";
            String source = "";
            String destination = "";

            try (PreparedStatement psBus = con.prepareStatement(busCheckSql)) {
                psBus.setInt(1, busId);
                try (ResultSet rsBus = psBus.executeQuery()) {
                    if (!rsBus.next()) {
                        throw new IllegalStateException("Bus ID " + busId + " not found.");
                    }
                    busName = rsBus.getString("bus_name");
                    source = rsBus.getString("source");
                    destination = rsBus.getString("destination");
                    baseFare = rsBus.getDouble("fare");
                    departureTs = rsBus.getTimestamp("departure_time");
                    busStatus = rsBus.getString("status");
                }
            }

            // Server-side departure enforcement
            if ("DEPARTED".equalsIgnoreCase(busStatus) ||
                (departureTs != null && departureTs.toLocalDateTime().isBefore(LocalDateTime.now()))) {
                throw new IllegalStateException("Booking rejected: The bus '" + busName + "' has already departed.");
            }
            if ("CANCELLED".equalsIgnoreCase(busStatus)) {
                throw new IllegalStateException("Booking rejected: This bus schedule has been cancelled.");
            }

            // 2. Lock and check if any of the requested seats are already booked
            StringBuilder inClause = new StringBuilder();
            for (int i = 0; i < seatNumbers.size(); i++) {
                if (i > 0) inClause.append(",");
                inClause.append("?");
            }

            String seatCheckSql = "SELECT seat_number FROM booking WHERE bus_id = ? AND status = 'BOOKED' " +
                                  "AND seat_number IN (" + inClause + ") FOR UPDATE";

            try (PreparedStatement psSeat = con.prepareStatement(seatCheckSql)) {
                psSeat.setInt(1, busId);
                for (int i = 0; i < seatNumbers.size(); i++) {
                    psSeat.setInt(i + 2, seatNumbers.get(i));
                }
                try (ResultSet rsSeat = psSeat.executeQuery()) {
                    if (rsSeat.next()) {
                        int occupiedSeat = rsSeat.getInt("seat_number");
                        throw new IllegalStateException("Seat #" + occupiedSeat + " has already been booked by another passenger.");
                    }
                }
            }

            // 3. Fetch passenger data for concession evaluation
            String passSql = "SELECT name, age, gender, phone, has_disability, disability_id_no " +
                             "FROM passenger WHERE passenger_id = ?";
            Passenger passenger = new Passenger();
            passenger.setPassengerId(passengerId);

            try (PreparedStatement psPass = con.prepareStatement(passSql)) {
                psPass.setInt(1, passengerId);
                try (ResultSet rsPass = psPass.executeQuery()) {
                    if (!rsPass.next()) {
                        throw new IllegalStateException("Passenger ID " + passengerId + " not found.");
                    }
                    passenger.setName(rsPass.getString("name"));
                    passenger.setAge(rsPass.getInt("age"));
                    passenger.setGender(rsPass.getString("gender"));
                    passenger.setPhoneNumber(rsPass.getString("phone"));
                    passenger.setHasDisability(rsPass.getBoolean("has_disability"));
                    passenger.setDisabilityIdNo(rsPass.getString("disability_id_no"));
                }
            }

            int concessionPercent = FareCalculator.getConcessionPercentage(passenger);
            double seatFare = FareCalculator.calculateSeatFare(baseFare, concessionPercent);
            double totalFare = FareCalculator.calculateTotalFare(baseFare, seatNumbers.size(), concessionPercent);

            String resolvedGender = (seatGender != null && !seatGender.trim().isEmpty())
                    ? seatGender.trim().toUpperCase()
                    : (passenger.getGender() != null ? passenger.getGender().toUpperCase() : "MALE");

            // 4. Create booking_group
            int groupId = -1;
            String groupSql = "INSERT INTO booking_group (passenger_id, bus_id, booking_time, total_fare) " +
                              "VALUES (?, ?, NOW(), ?)";
            try (PreparedStatement psGroup = con.prepareStatement(groupSql, Statement.RETURN_GENERATED_KEYS)) {
                psGroup.setInt(1, passengerId);
                psGroup.setInt(2, busId);
                psGroup.setDouble(3, totalFare);
                psGroup.executeUpdate();

                try (ResultSet rsKeys = psGroup.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        groupId = rsKeys.getInt(1);
                    }
                }
            }

            if (groupId == -1) {
                throw new SQLException("Failed to create booking group record.");
            }

            // 5. Insert individual seat bookings
            String itemSql = "INSERT INTO booking (group_id, passenger_id, bus_id, seat_number, booking_date, " +
                             "base_fare, concession_percent, fare, seat_passenger_gender, status) " +
                             "VALUES (?, ?, ?, ?, CURDATE(), ?, ?, ?, ?, 'BOOKED')";

            List<Booking> bookingItems = new ArrayList<>();
            try (PreparedStatement psItem = con.prepareStatement(itemSql, Statement.RETURN_GENERATED_KEYS)) {
                for (int seatNum : seatNumbers) {
                    psItem.setInt(1, groupId);
                    psItem.setInt(2, passengerId);
                    psItem.setInt(3, busId);
                    psItem.setInt(4, seatNum);
                    psItem.setDouble(5, baseFare);
                    psItem.setInt(6, concessionPercent);
                    psItem.setDouble(7, seatFare);
                    psItem.setString(8, resolvedGender);
                    psItem.addBatch();

                    Booking b = new Booking();
                    b.setGroupId(groupId);
                    b.setPassengerId(passengerId);
                    b.setBusId(busId);
                    b.setSeatNumber(seatNum);
                    b.setBaseFare(baseFare);
                    b.setConcessionPercent(concessionPercent);
                    b.setFare(seatFare);
                    b.setSeatPassengerGender(resolvedGender);
                    b.setStatus("BOOKED");
                    bookingItems.add(b);
                }
                psItem.executeBatch();
            }

            // Commit transaction
            con.commit();

            // Assemble resulting BookingGroup object
            BookingGroup bg = new BookingGroup();
            bg.setGroupId(groupId);
            bg.setPassengerId(passengerId);
            bg.setBusId(busId);
            bg.setTotalFare(totalFare);
            bg.setBookingTime(new Timestamp(System.currentTimeMillis()));
            bg.setPassengerName(passenger.getName());
            bg.setPassengerPhone(passenger.getPhoneNumber());
            bg.setPassengerGender(passenger.getGender());
            bg.setHasDisability(passenger.isHasDisability());
            bg.setDisabilityIdNo(passenger.getDisabilityIdNo());
            bg.setBusName(busName);
            bg.setSource(source);
            bg.setDestination(destination);
            bg.setBaseFare(baseFare);
            bg.setSeatNumbers(seatNumbers);
            bg.setBookings(bookingItems);
            if (departureTs != null) {
                bg.setBusDepartureTime(departureTs.toLocalDateTime().format(model.Bus.FORMATTER));
            }

            return bg;

        } catch (Exception ex) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rbEx) {
                    rbEx.printStackTrace();
                }
            }
            if (ex instanceof IllegalStateException) {
                throw (IllegalStateException) ex;
            }
            throw new SQLException("Transaction aborted: " + ex.getMessage(), ex);
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException cEx) {
                    cEx.printStackTrace();
                }
            }
        }
    }

    /**
     * Cancels an entire booking group. Allowed only prior to bus departure.
     */
    public boolean cancelBookingGroup(int groupId) throws SQLException, IllegalStateException {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Verify bus has not departed
            String checkSql = "SELECT b.departure_time, b.status FROM bus b " +
                              "JOIN booking_group bg ON b.bus_id = bg.bus_id " +
                              "WHERE bg.group_id = ? FOR UPDATE";
            try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
                psCheck.setInt(1, groupId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        Timestamp dep = rs.getTimestamp("departure_time");
                        String status = rs.getString("status");
                        if ("DEPARTED".equalsIgnoreCase(status) ||
                            (dep != null && dep.toLocalDateTime().isBefore(LocalDateTime.now()))) {
                            throw new IllegalStateException("Cancellation rejected: The bus has already departed.");
                        }
                    } else {
                        throw new IllegalStateException("Booking group #" + groupId + " not found.");
                    }
                }
            }

            // Update bookings to CANCELLED
            String updateSql = "UPDATE booking SET status = 'CANCELLED' WHERE group_id = ?";
            try (PreparedStatement psUp = con.prepareStatement(updateSql)) {
                psUp.setInt(1, groupId);
                psUp.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception ex) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (ex instanceof IllegalStateException) throw (IllegalStateException) ex;
            throw new SQLException("Cancellation failed: " + ex.getMessage(), ex);
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Retrieves full details for a BookingGroup by Group ID (for ticket printing).
     */
    public BookingGroup getBookingGroupById(int groupId) {
        String groupSql = "SELECT bg.*, p.name AS pass_name, p.phone AS pass_phone, p.gender AS pass_gender, " +
                          "p.has_disability, p.disability_id_no, b.bus_name, b.source, b.destination, " +
                          "b.departure_time, b.status AS bus_status, b.fare AS base_fare " +
                          "FROM booking_group bg " +
                          "JOIN passenger p ON bg.passenger_id = p.passenger_id " +
                          "JOIN bus b ON bg.bus_id = b.bus_id " +
                          "WHERE bg.group_id = ?";

        BookingGroup bg = null;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(groupSql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    bg = extractBookingGroup(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (bg != null) {
            loadSeatsForGroup(bg);
        }
        return bg;
    }

    /**
     * Retrieves all booking groups.
     */
    public List<BookingGroup> getAllBookingGroups() {
        List<BookingGroup> list = new ArrayList<>();
        String sql = "SELECT bg.*, p.name AS pass_name, p.phone AS pass_phone, p.gender AS pass_gender, " +
                     "p.has_disability, p.disability_id_no, b.bus_name, b.source, b.destination, " +
                     "b.departure_time, b.status AS bus_status, b.fare AS base_fare " +
                     "FROM booking_group bg " +
                     "JOIN passenger p ON bg.passenger_id = p.passenger_id " +
                     "JOIN bus b ON bg.bus_id = b.bus_id " +
                     "ORDER BY bg.group_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BookingGroup bg = extractBookingGroup(rs);
                list.add(bg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (BookingGroup bg : list) {
            loadSeatsForGroup(bg);
        }
        return list;
    }

    /**
     * Retrieves all booking groups for a specific passenger or user account.
     */
    public List<BookingGroup> getBookingGroupsByPassenger(int passengerId) {
        List<BookingGroup> list = new ArrayList<>();
        String sql = "SELECT bg.*, p.name AS pass_name, p.phone AS pass_phone, p.gender AS pass_gender, " +
                     "p.has_disability, p.disability_id_no, b.bus_name, b.source, b.destination, " +
                     "b.departure_time, b.status AS bus_status, b.fare AS base_fare " +
                     "FROM booking_group bg " +
                     "JOIN passenger p ON bg.passenger_id = p.passenger_id " +
                     "JOIN bus b ON bg.bus_id = b.bus_id " +
                     "WHERE bg.passenger_id = ? " +
                     "ORDER BY bg.group_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractBookingGroup(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (BookingGroup bg : list) {
            loadSeatsForGroup(bg);
        }
        return list;
    }

    private void loadSeatsForGroup(BookingGroup bg) {
        String seatSql = "SELECT * FROM booking WHERE group_id = ? ORDER BY seat_number ASC";
        List<Integer> seats = new ArrayList<>();
        List<Booking> bookings = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(seatSql)) {
            ps.setInt(1, bg.getGroupId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int seatNum = rs.getInt("seat_number");
                    seats.add(seatNum);

                    Booking b = new Booking();
                    b.setBookingId(rs.getInt("booking_id"));
                    b.setGroupId(bg.getGroupId());
                    b.setPassengerId(rs.getInt("passenger_id"));
                    b.setBusId(rs.getInt("bus_id"));
                    b.setSeatNumber(seatNum);
                    b.setBookingDate(rs.getString("booking_date"));
                    b.setBaseFare(rs.getDouble("base_fare"));
                    b.setConcessionPercent(rs.getInt("concession_percent"));
                    b.setFare(rs.getDouble("fare"));
                    b.setSeatPassengerGender(rs.getString("seat_passenger_gender"));
                    b.setStatus(rs.getString("status"));
                    bookings.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        bg.setSeatNumbers(seats);
        bg.setBookings(bookings);
    }

    private BookingGroup extractBookingGroup(ResultSet rs) throws SQLException {
        BookingGroup bg = new BookingGroup();
        bg.setGroupId(rs.getInt("group_id"));
        bg.setPassengerId(rs.getInt("passenger_id"));
        bg.setBusId(rs.getInt("bus_id"));
        bg.setBookingTime(rs.getTimestamp("booking_time"));
        bg.setTotalFare(rs.getDouble("total_fare"));

        bg.setPassengerName(rs.getString("pass_name"));
        bg.setPassengerPhone(rs.getString("pass_phone"));
        bg.setPassengerGender(rs.getString("pass_gender"));
        bg.setHasDisability(rs.getBoolean("has_disability"));
        bg.setDisabilityIdNo(rs.getString("disability_id_no"));

        bg.setBusName(rs.getString("bus_name"));
        bg.setSource(rs.getString("source"));
        bg.setDestination(rs.getString("destination"));
        bg.setBaseFare(rs.getDouble("base_fare"));
        bg.setBusStatus(rs.getString("bus_status"));

        Timestamp dep = rs.getTimestamp("departure_time");
        if (dep != null) {
            bg.setBusDepartureTime(dep.toLocalDateTime().format(model.Bus.FORMATTER));
        }
        return bg;
    }
}
