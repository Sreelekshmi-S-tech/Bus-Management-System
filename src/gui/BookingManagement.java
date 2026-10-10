package gui;

import dao.BookingDAO;
import dao.BusDAO;
import dao.PassengerDAO;
import model.Booking;
import model.Bus;
import model.Passenger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class BookingManagement extends JFrame {

    private JComboBox<BusItem> busComboBox;
    private JComboBox<PassengerItem> passengerComboBox;
    private JTextField seatNumberField, dateField, fareField;
    private JLabel seatStatusLabel;
    private JTable bookingTable;
    private DefaultTableModel tableModel;

    private BusDAO busDAO;
    private PassengerDAO passengerDAO;
    private BookingDAO bookingDAO;

    // Helper item for bus combo
    private static class BusItem {
        final Bus bus;
        BusItem(Bus bus) { this.bus = bus; }
        @Override
        public String toString() {
            return bus.getBusId() + " - " + bus.getBusName() + " (" + bus.getSource() + " to " + bus.getDestination() + ")";
        }
    }

    // Helper item for passenger combo
    private static class PassengerItem {
        final Passenger passenger;
        PassengerItem(Passenger passenger) { this.passenger = passenger; }
        @Override
        public String toString() {
            return passenger.getPassengerId() + " - " + passenger.getName() + " (" + passenger.getPhoneNumber() + ")";
        }
    }

    public BookingManagement() {
        busDAO = new BusDAO();
        passengerDAO = new PassengerDAO();
        bookingDAO = new BookingDAO();

        setTitle("Bus Reservation System - Seat Booking");
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 174, 96));
        JLabel title = new JLabel("Seat Booking & Reservation");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.add(title);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Content Split (Form on Left, Table on Right)
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setBorder(BorderFactory.createTitledBorder("Booking Details"));

        JLabel busLabel = new JLabel("Select Bus:");
        busLabel.setBounds(20, 35, 120, 25);
        busComboBox = new JComboBox<>();
        busComboBox.setBounds(140, 35, 300, 28);

        JLabel passLabel = new JLabel("Select Passenger:");
        passLabel.setBounds(20, 80, 120, 25);
        passengerComboBox = new JComboBox<>();
        passengerComboBox.setBounds(140, 80, 300, 28);

        JLabel seatLabel = new JLabel("Seat Number:");
        seatLabel.setBounds(20, 125, 120, 25);
        seatNumberField = new JTextField();
        seatNumberField.setBounds(140, 125, 140, 28);

        seatStatusLabel = new JLabel("Seats: -");
        seatStatusLabel.setBounds(290, 125, 160, 25);
        seatStatusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        seatStatusLabel.setForeground(new Color(41, 128, 185));

        JLabel dateLabel = new JLabel("Travel Date:");
        dateLabel.setBounds(20, 170, 120, 25);
        dateField = new JTextField(LocalDate.now().toString());
        dateField.setBounds(140, 170, 300, 28);

        JLabel fareLabel = new JLabel("Fare ($):");
        fareLabel.setBounds(20, 215, 120, 25);
        fareField = new JTextField();
        fareField.setEditable(false);
        fareField.setBounds(140, 215, 300, 28);

        // Action Buttons
        JButton bookButton = new JButton("Confirm Booking");
        bookButton.setBounds(20, 275, 150, 35);
        bookButton.setBackground(new Color(46, 204, 113));
        bookButton.setForeground(Color.BLACK);

        JButton cancelButton = new JButton("Cancel Booking");
        cancelButton.setBounds(180, 275, 140, 35);
        cancelButton.setBackground(new Color(231, 76, 60));
        cancelButton.setForeground(Color.BLACK);

        JButton ticketButton = new JButton("Generate Ticket");
        ticketButton.setBounds(330, 275, 130, 35);
        ticketButton.setBackground(new Color(52, 152, 219));
        ticketButton.setForeground(Color.BLACK);

        formPanel.add(busLabel);
        formPanel.add(busComboBox);
        formPanel.add(passLabel);
        formPanel.add(passengerComboBox);
        formPanel.add(seatLabel);
        formPanel.add(seatNumberField);
        formPanel.add(seatStatusLabel);
        formPanel.add(dateLabel);
        formPanel.add(dateField);
        formPanel.add(fareLabel);
        formPanel.add(fareField);

        formPanel.add(bookButton);
        formPanel.add(cancelButton);
        formPanel.add(ticketButton);

        contentPanel.add(formPanel);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Current Bookings"));

        String[] columns = {"ID", "Passenger", "Bus", "Seat", "Date", "Fare", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookingTable = new JTable(tableModel);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh Bookings");
        tablePanel.add(refreshButton, BorderLayout.SOUTH);

        contentPanel.add(tablePanel);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Events
        busComboBox.addActionListener(e -> onBusSelected());
        bookButton.addActionListener(e -> bookSeat());
        cancelButton.addActionListener(e -> cancelSelectedBooking());
        ticketButton.addActionListener(e -> openTicketForSelected());
        refreshButton.addActionListener(e -> {
            loadDropdownData();
            loadBookingTable();
        });

        loadDropdownData();
        loadBookingTable();
    }

    private void loadDropdownData() {
        busComboBox.removeAllItems();
        List<Bus> buses = busDAO.getAllBuses();
        for (Bus b : buses) {
            busComboBox.addItem(new BusItem(b));
        }

        passengerComboBox.removeAllItems();
        List<Passenger> passengers = passengerDAO.getAllPassengers();
        for (Passenger p : passengers) {
            passengerComboBox.addItem(new PassengerItem(p));
        }

        onBusSelected();
    }

    private void onBusSelected() {
        BusItem item = (BusItem) busComboBox.getSelectedItem();
        if (item != null) {
            Bus b = item.bus;
            fareField.setText(String.format("%.2f", b.getFare()));
            List<Integer> booked = bookingDAO.getBookedSeats(b.getBusId());
            int available = b.getTotalSeats() - booked.size();
            seatStatusLabel.setText("Avail: " + available + " / " + b.getTotalSeats());
            seatStatusLabel.setToolTipText("Booked seats: " + booked.toString());
        } else {
            fareField.setText("");
            seatStatusLabel.setText("Seats: -");
        }
    }

    private void loadBookingTable() {
        tableModel.setRowCount(0);
        List<Booking> list = bookingDAO.getAllBookings();
        for (Booking b : list) {
            tableModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getPassengerName() != null ? b.getPassengerName() : ("ID:" + b.getPassengerId()),
                    b.getBusName() != null ? b.getBusName() : ("ID:" + b.getBusId()),
                    b.getSeatNumber(),
                    b.getBookingDate(),
                    String.format("$%.2f", b.getFare()),
                    b.getStatus()
            });
        }
    }

    private void bookSeat() {
        try {
            BusItem busItem = (BusItem) busComboBox.getSelectedItem();
            PassengerItem passItem = (PassengerItem) passengerComboBox.getSelectedItem();

            if (busItem == null || passItem == null) {
                JOptionPane.showMessageDialog(this, "Please select both a Bus and a Passenger.", "Missing Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (seatNumberField.getText().trim().isEmpty() || dateField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please specify Seat Number and Date.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int seatNum = Integer.parseInt(seatNumberField.getText().trim());
            Bus bus = busItem.bus;
            Passenger passenger = passItem.passenger;

            if (seatNum < 1 || seatNum > bus.getTotalSeats()) {
                JOptionPane.showMessageDialog(this, "Invalid Seat Number! Please choose between 1 and " + bus.getTotalSeats() + ".", "Invalid Seat", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (bookingDAO.isSeatBooked(bus.getBusId(), seatNum)) {
                JOptionPane.showMessageDialog(this, "Seat #" + seatNum + " is already booked for this bus! Please choose another seat.", "Seat Occupied", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String date = dateField.getText().trim();
            double fare = bus.getFare();

            Booking booking = new Booking(0, passenger.getPassengerId(), bus.getBusId(), seatNum, date, fare, "CONFIRMED");
            if (bookingDAO.addBooking(booking)) {
                JOptionPane.showMessageDialog(this, "Seat #" + seatNum + " booked successfully!\nBooking ID: #" + booking.getBookingId());
                seatNumberField.setText("");
                onBusSelected();
                loadBookingTable();
            } else {
                JOptionPane.showMessageDialog(this, "Booking failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Seat Number must be a valid integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelectedBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = tableModel.getValueAt(selectedRow, 6).toString();

        if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This booking is already cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel Booking #" + bookingId + "?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (bookingDAO.cancelBooking(bookingId)) {
                JOptionPane.showMessageDialog(this, "Booking #" + bookingId + " cancelled successfully!");
                onBusSelected();
                loadBookingTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel booking.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openTicketForSelected() {
        int selectedRow = bookingTable.getSelectedRow();
        int bookingId = -1;
        if (selectedRow >= 0) {
            bookingId = (int) tableModel.getValueAt(selectedRow, 0);
        }
        TicketGeneration ticketGen = new TicketGeneration(bookingId);
        ticketGen.setVisible(true);
    }
}
