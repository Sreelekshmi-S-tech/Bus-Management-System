package gui;

import dao.BookingDAO;
import model.Booking;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class TicketGeneration extends JFrame {

    private JTextField searchField;
    private JTextArea ticketArea;
    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private BookingDAO bookingDAO;
    private Booking currentBooking;

    public TicketGeneration() {
        this(-1);
    }

    public TicketGeneration(int initialBookingId) {
        bookingDAO = new BookingDAO();

        setTitle("Bus Reservation System - Ticket Generation");
        setSize(950, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(142, 68, 173));
        JLabel title = new JLabel("Ticket Generation & Boarding Pass");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.add(title);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Top Search Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBar.setBorder(BorderFactory.createTitledBorder("Search Booking"));
        searchBar.add(new JLabel("Booking ID:"));
        searchField = new JTextField(12);
        searchBar.add(searchField);
        JButton searchButton = new JButton("View Ticket");
        searchButton.setBackground(new Color(52, 152, 219));
        searchButton.setForeground(Color.BLACK);
        searchBar.add(searchButton);
        JButton refreshButton = new JButton("Refresh Bookings");
        searchBar.add(refreshButton);

        // Center Split (List of Bookings on Left, Formatted Ticket on Right)
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // Left Table
        JPanel listPanel = new JPanel(new BorderLayout(5, 5));
        listPanel.setBorder(BorderFactory.createTitledBorder("Select from Bookings"));
        String[] columns = {"ID", "Passenger", "Bus", "Seat", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingTable = new JTable(tableModel);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.setRowHeight(24);
        listPanel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        contentPanel.add(listPanel);

        // Right Ticket View
        JPanel ticketPanel = new JPanel(new BorderLayout(10, 10));
        ticketPanel.setBorder(BorderFactory.createTitledBorder("Ticket Preview"));

        ticketArea = new JTextArea();
        ticketArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ticketArea.setEditable(false);
        ticketArea.setBackground(new Color(250, 250, 250));
        ticketArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        ticketPanel.add(new JScrollPane(ticketArea), BorderLayout.CENTER);

        // Bottom Actions for Ticket
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton printButton = new JButton("Print Ticket");
        printButton.setBackground(new Color(46, 204, 113));
        printButton.setForeground(Color.BLACK);

        JButton saveButton = new JButton("Save as File");
        saveButton.setBackground(new Color(241, 196, 15));
        saveButton.setForeground(Color.BLACK);

        JButton closeButton = new JButton("Close");

        actionPanel.add(printButton);
        actionPanel.add(saveButton);
        actionPanel.add(closeButton);
        ticketPanel.add(actionPanel, BorderLayout.SOUTH);

        contentPanel.add(ticketPanel);

        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.add(searchBar, BorderLayout.NORTH);
        centerContainer.add(contentPanel, BorderLayout.CENTER);

        mainPanel.add(centerContainer, BorderLayout.CENTER);
        add(mainPanel);

        // Event Listeners
        searchButton.addActionListener(e -> searchTicket());
        refreshButton.addActionListener(e -> loadBookings());
        printButton.addActionListener(e -> printTicket());
        saveButton.addActionListener(e -> saveTicketToFile());
        closeButton.addActionListener(e -> dispose());

        bookingTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = bookingTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int bookingId = (int) tableModel.getValueAt(selectedRow, 0);
                    displayTicket(bookingId);
                }
            }
        });

        loadBookings();

        if (initialBookingId > 0) {
            searchField.setText(String.valueOf(initialBookingId));
            displayTicket(initialBookingId);
        } else {
            ticketArea.setText("\n  Please select a booking from the list or enter a Booking ID to view the ticket.");
        }
    }

    private void loadBookings() {
        tableModel.setRowCount(0);
        List<Booking> list = bookingDAO.getAllBookings();
        for (Booking b : list) {
            tableModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getPassengerName() != null ? b.getPassengerName() : ("ID: " + b.getPassengerId()),
                    b.getBusName() != null ? b.getBusName() : ("Bus: " + b.getBusId()),
                    b.getSeatNumber(),
                    b.getStatus()
            });
        }
    }

    private void searchTicket() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Booking ID to search.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(text);
            displayTicket(id);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Booking ID must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayTicket(int bookingId) {
        Booking b = bookingDAO.getBookingById(bookingId);
        if (b == null) {
            JOptionPane.showMessageDialog(this, "No ticket found for Booking ID #" + bookingId, "Not Found", JOptionPane.INFORMATION_MESSAGE);
            ticketArea.setText("\n  Ticket not found for Booking ID #" + bookingId);
            currentBooking = null;
            return;
        }

        currentBooking = b;
        searchField.setText(String.valueOf(b.getBookingId()));

        StringBuilder sb = new StringBuilder();
        sb.append("=====================================================\n");
        sb.append("             BUS RESERVATION BOARDING PASS           \n");
        sb.append("=====================================================\n");
        sb.append(String.format(" Ticket / Booking ID : #%d\n", b.getBookingId()));
        sb.append(String.format(" Status              : %s\n", b.getStatus()));
        sb.append(String.format(" Date of Journey     : %s\n", b.getBookingDate()));
        sb.append("-----------------------------------------------------\n");
        sb.append(" PASSENGER INFORMATION:\n");
        sb.append(String.format(" Name                : %s\n", b.getPassengerName() != null ? b.getPassengerName() : "N/A"));
        sb.append(String.format(" Passenger ID        : %d\n", b.getPassengerId()));
        sb.append(String.format(" Contact Phone       : %s\n", b.getPassengerPhone() != null ? b.getPassengerPhone() : "N/A"));
        sb.append("-----------------------------------------------------\n");
        sb.append(" TRIP & BUS DETAILS:\n");
        sb.append(String.format(" Bus ID & Name       : #%d - %s\n", b.getBusId(), b.getBusName() != null ? b.getBusName() : "Standard Bus"));
        sb.append(String.format(" Origin              : %s\n", b.getSource() != null ? b.getSource() : "N/A"));
        sb.append(String.format(" Destination         : %s\n", b.getDestination() != null ? b.getDestination() : "N/A"));
        sb.append(String.format(" Seat Number         : %d\n", b.getSeatNumber()));
        sb.append("-----------------------------------------------------\n");
        sb.append(" FARE SUMMARY:\n");
        sb.append(String.format(" Fare Amount         : $%.2f\n", b.getFare()));
        sb.append(String.format(" Payment Status      : %s\n", "PAID"));
        sb.append("=====================================================\n");
        sb.append("       Thank you for traveling with us! Have a safe trip!\n");
        sb.append("=====================================================\n");

        ticketArea.setText(sb.toString());
    }

    private void printTicket() {
        if (currentBooking == null) {
            JOptionPane.showMessageDialog(this, "Please select or search a valid ticket first.", "No Ticket", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean complete = ticketArea.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "Ticket printing complete!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveTicketToFile() {
        if (currentBooking == null) {
            JOptionPane.showMessageDialog(this, "Please select or search a valid ticket first.", "No Ticket", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Ticket_" + currentBooking.getBookingId() + ".txt"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter out = new PrintWriter(fileToSave)) {
                out.print(ticketArea.getText());
                JOptionPane.showMessageDialog(this, "Ticket saved successfully to:\n" + fileToSave.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
