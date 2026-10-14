package gui;

import dao.BookingDAO;
import dao.BusDAO;
import dao.PassengerDAO;
import model.BookingGroup;
import model.Bus;
import model.Passenger;
import model.Seat;
import model.User;
import service.AuthService;
import service.FareCalculator;
import service.RateLimiter;
import service.SeatService;
import util.AppIcon;
import util.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookingManagement extends JFrame implements ThemeManager.ThemeChangeListener {

    private JComboBox<Bus> busCombo;
    private JComboBox<Passenger> passengerCombo;
    private JComboBox<String> preferenceCombo;
    private JComboBox<String> seatGenderCombo;

    private JLabel routeLabel;
    private JLabel departureLabel;
    private JLabel busStatusBadge;
    private JLabel baseFareLabel;
    private JLabel concessionLabel;
    private JLabel totalFareLabel;
    private JLabel selectedSeatsLabel;
    private JLabel seatAvailabilityLabel;
    private JLabel recLbl;

    private SeatMapPanel seatMapPanel;
    private ModernButton bookButton;
    private ModernButton clearBtn;
    private ModernButton cancelButton;
    private ModernButton generateTicketBtn;
    private ModernButton refreshBtn;

    private JTable bookingTable;
    private DefaultTableModel tableModel;

    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel controlsPanel;
    private JPanel seatMapContainer;
    private JPanel summaryBox;

    private final BusDAO busDAO = new BusDAO();
    private final PassengerDAO passengerDAO = new PassengerDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    private Map<Integer, Seat> currentSeatMap;
    private List<Integer> currentlySelectedSeats = new ArrayList<>();

    public BookingManagement() {
        setTitle("Seat Booking & Reservation Subsystem - Bus Reservation System");
        setSize(1180, 740);
        setMinimumSize(new Dimension(1060, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadDropdownData();
        loadBookingsTable();

        ThemeManager.getInstance().addListener(this);
        applyTheme(ThemeManager.getInstance().isDarkMode());
    }

    private void initUI() {
        ThemeManager tm = ThemeManager.getInstance();
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(tm.getBgColor());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Header Strip
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(tm.getHeaderBg());
        headerPanel.setPreferredSize(new Dimension(1140, 56));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JLabel title = new JLabel("Interactive Seat Booking & Multi-Seat Reservation Terminal", AppIcon.of(AppIcon.Type.SEAT, 22, Color.WHITE), SwingConstants.LEFT);
        title.setIconTextGap(10);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);

        refreshBtn = new ModernButton("Refresh Data", AppIcon.of(AppIcon.Type.REFRESH, 13, Color.WHITE), tm.getPrimaryBlue(), Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(130, 32));
        refreshBtn.setCornerRadius(6);
        refreshBtn.addActionListener(e -> {
            loadDropdownData();
            loadBookingsTable();
        });
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Split: Left Control & Info, Center SeatMap, Bottom Bookings Table
        JPanel centerContainer = new JPanel(new BorderLayout(14, 0));
        centerContainer.setOpaque(false);

        // Left Controls Panel
        controlsPanel = new JPanel();
        controlsPanel.setLayout(null);
        controlsPanel.setPreferredSize(new Dimension(380, 430));
        controlsPanel.setBackground(tm.getCardBg());
        controlsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        int y = 8;
        int step = 46;

        // 1. Select Bus
        JLabel busLbl = new JLabel("1. Select Bus Schedule:");
        busLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        busLbl.setForeground(tm.getTextPrimary());
        busLbl.setBounds(16, y, 200, 16);
        busCombo = new JComboBox<>();
        busCombo.setBounds(16, y + 16, 345, 28);
        busCombo.setBackground(tm.getInputBg());
        busCombo.setForeground(tm.getInputText());
        busCombo.addActionListener(e -> onBusSelected());
        controlsPanel.add(busLbl);
        controlsPanel.add(busCombo);

        // Bus Route & Departure info strip
        y += step;
        routeLabel = new JLabel("Route: Select a bus above");
        routeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        routeLabel.setForeground(tm.getTextSecondary());
        routeLabel.setBounds(16, y, 345, 16);
        controlsPanel.add(routeLabel);

        y += 18;
        departureLabel = new JLabel("Departure: -");
        departureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        departureLabel.setForeground(tm.getTextSecondary());
        departureLabel.setBounds(16, y, 230, 16);

        busStatusBadge = new JLabel("SCHEDULED");
        busStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        busStatusBadge.setForeground(tm.getSuccessGreen());
        busStatusBadge.setBounds(250, y, 110, 16);
        controlsPanel.add(departureLabel);
        controlsPanel.add(busStatusBadge);

        // 2. Select Passenger
        y += 26;
        JLabel passLbl = new JLabel("2. Primary Passenger:");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLbl.setForeground(tm.getTextPrimary());
        passLbl.setBounds(16, y, 200, 16);
        passengerCombo = new JComboBox<>();
        passengerCombo.setBounds(16, y + 16, 345, 28);
        passengerCombo.setBackground(tm.getInputBg());
        passengerCombo.setForeground(tm.getInputText());
        passengerCombo.addActionListener(e -> onPassengerSelected());
        controlsPanel.add(passLbl);
        controlsPanel.add(passengerCombo);

        // 3. Seating Preference & Passenger Gender for Seats
        y += step;
        JLabel prefLbl = new JLabel("3. Seating Preference:");
        prefLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        prefLbl.setForeground(tm.getTextPrimary());
        prefLbl.setBounds(16, y, 180, 16);
        preferenceCombo = new JComboBox<>(new String[]{"No Preference", "Next to Same Gender"});
        preferenceCombo.setBounds(16, y + 16, 175, 26);
        preferenceCombo.setBackground(tm.getInputBg());
        preferenceCombo.setForeground(tm.getInputText());
        preferenceCombo.addActionListener(e -> applySeatingPreference());
        controlsPanel.add(prefLbl);
        controlsPanel.add(preferenceCombo);

        JLabel genderLbl = new JLabel("Passenger Gender:");
        genderLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        genderLbl.setForeground(tm.getTextPrimary());
        genderLbl.setBounds(200, y, 160, 16);
        seatGenderCombo = new JComboBox<>(new String[]{"MALE", "FEMALE", "OTHERS"});
        seatGenderCombo.setBounds(200, y + 16, 160, 26);
        seatGenderCombo.setBackground(tm.getInputBg());
        seatGenderCombo.setForeground(tm.getInputText());
        seatGenderCombo.addActionListener(e -> applySeatingPreference());
        controlsPanel.add(genderLbl);
        controlsPanel.add(seatGenderCombo);

        // 4. Live Fare Summary Box
        y += step + 4;
        summaryBox = new JPanel(new GridLayout(4, 1, 0, 3));
        summaryBox.setBounds(16, y, 345, 96);
        summaryBox.setBackground(tm.getTableAlternateRow());
        summaryBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        selectedSeatsLabel = new JLabel("Selected Seats: None (0/6)");
        selectedSeatsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectedSeatsLabel.setForeground(tm.getPrimaryBlue());

        baseFareLabel = new JLabel("Base Rate: $0.00 / seat");
        baseFareLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        baseFareLabel.setForeground(tm.getTextSecondary());

        concessionLabel = new JLabel("Concession: 0% (Standard Fare)");
        concessionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        concessionLabel.setForeground(tm.getTextSecondary());

        totalFareLabel = new JLabel("Total Fare: $0.00");
        totalFareLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalFareLabel.setForeground(tm.getSuccessGreen());

        summaryBox.add(selectedSeatsLabel);
        summaryBox.add(baseFareLabel);
        summaryBox.add(concessionLabel);
        summaryBox.add(totalFareLabel);
        controlsPanel.add(summaryBox);

        // Action Buttons
        y += 106;
        bookButton = new ModernButton(
                "Confirm Booking",
                AppIcon.of(AppIcon.Type.CHECK, 15, Color.WHITE),
                tm.getSuccessGreen(),
                Color.WHITE
        );
        bookButton.setBounds(16, y, 220, 38);
        bookButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bookButton.setCornerRadius(8);
        bookButton.addActionListener(e -> bookSelectedSeats());
        controlsPanel.add(bookButton);

        clearBtn = new ModernButton(
                "Clear",
                AppIcon.of(AppIcon.Type.REFRESH, 12, Color.WHITE),
                new Color(130, 140, 155),
                Color.WHITE
        );
        clearBtn.setBounds(245, y, 116, 38);
        clearBtn.setCornerRadius(8);
        clearBtn.addActionListener(e -> seatMapPanel.clearSelection());
        controlsPanel.add(clearBtn);

        centerContainer.add(controlsPanel, BorderLayout.WEST);

        // Seat Map Container Panel
        seatMapContainer = new JPanel(new BorderLayout());
        seatMapContainer.setBackground(tm.getCardBg());
        seatMapContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // Seat Availability Sub-header
        seatAvailabilityLabel = new JLabel("Bus Interior Layout & Real-Time Availability (Hover for details, Click to select)", SwingConstants.CENTER);
        seatAvailabilityLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        seatAvailabilityLabel.setForeground(tm.getTextSecondary());
        seatMapContainer.add(seatAvailabilityLabel, BorderLayout.NORTH);

        seatMapPanel = new SeatMapPanel();
        seatMapPanel.setSeatSelectionListener(selected -> {
            currentlySelectedSeats = selected;
            updateFareCalculations();
        });

        JScrollPane seatScroll = new JScrollPane(seatMapPanel);
        seatScroll.setBorder(BorderFactory.createEmptyBorder());
        seatScroll.getVerticalScrollBar().setUnitIncrement(16);
        seatMapContainer.add(seatScroll, BorderLayout.CENTER);

        // Add Legend at the bottom of the seat map
        seatMapContainer.add(SeatMapPanel.createLegendPanel(), BorderLayout.SOUTH);

        centerContainer.add(seatMapContainer, BorderLayout.CENTER);
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // Bottom Table: Existing Bookings & Actions
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.setPreferredSize(new Dimension(1140, 210));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        recLbl = new JLabel("Recent Bookings & Reservations:");
        recLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        recLbl.setForeground(tm.getTextPrimary());
        tableHeader.add(recLbl, BorderLayout.WEST);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tableActions.setOpaque(false);

        cancelButton = new ModernButton(
                "Cancel Selected",
                AppIcon.of(AppIcon.Type.TRASH, 13, Color.WHITE),
                tm.getDangerRed(),
                Color.WHITE
        );
        cancelButton.setPreferredSize(new Dimension(150, 32));
        cancelButton.setCornerRadius(6);
        cancelButton.addActionListener(e -> cancelSelectedBooking());

        generateTicketBtn = new ModernButton(
                "View & Print Ticket",
                AppIcon.of(AppIcon.Type.PRINT, 13, Color.WHITE),
                tm.getPrimaryBlue(),
                Color.WHITE
        );
        generateTicketBtn.setPreferredSize(new Dimension(175, 32));
        generateTicketBtn.setCornerRadius(6);
        generateTicketBtn.addActionListener(e -> openTicketForSelected());

        tableActions.add(cancelButton);
        tableActions.add(generateTicketBtn);
        tableHeader.add(tableActions, BorderLayout.EAST);
        bottomPanel.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"Group ID", "Passenger", "Bus Name", "Route", "Seats Booked", "Total Fare ($)", "Departure Time", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        bookingTable = new JTable(tableModel);
        bookingTable.setRowHeight(26);
        bookingTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bookingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        bookingTable.getTableHeader().setBackground(tm.getTableHeaderBg());
        bookingTable.getTableHeader().setForeground(tm.getTableHeaderText());
        bookingTable.setBackground(tm.getTableBg());
        bookingTable.setForeground(tm.getTextPrimary());
        bookingTable.setGridColor(tm.getTableGrid());
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScroll = new JScrollPane(bookingTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(tm.getCardBorder()));
        tableScroll.getViewport().setBackground(tm.getTableBg());
        bottomPanel.add(tableScroll, BorderLayout.CENTER);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    @Override
    public void onThemeChanged(boolean isDark) {
        applyTheme(isDark);
    }

    private void applyTheme(boolean isDark) {
        ThemeManager tm = ThemeManager.getInstance();
        mainPanel.setBackground(tm.getBgColor());
        headerPanel.setBackground(tm.getHeaderBg());

        controlsPanel.setBackground(tm.getCardBg());
        controlsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        seatMapContainer.setBackground(tm.getCardBg());
        seatMapContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        summaryBox.setBackground(tm.getTableAlternateRow());
        summaryBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        routeLabel.setForeground(tm.getTextSecondary());
        departureLabel.setForeground(tm.getTextSecondary());
        seatAvailabilityLabel.setForeground(tm.getTextSecondary());
        recLbl.setForeground(tm.getTextPrimary());

        busCombo.setBackground(tm.getInputBg());
        busCombo.setForeground(tm.getInputText());
        passengerCombo.setBackground(tm.getInputBg());
        passengerCombo.setForeground(tm.getInputText());
        preferenceCombo.setBackground(tm.getInputBg());
        preferenceCombo.setForeground(tm.getInputText());
        seatGenderCombo.setBackground(tm.getInputBg());
        seatGenderCombo.setForeground(tm.getInputText());

        bookingTable.setBackground(tm.getTableBg());
        bookingTable.setForeground(tm.getTextPrimary());
        bookingTable.setGridColor(tm.getTableGrid());
        bookingTable.getTableHeader().setBackground(tm.getTableHeaderBg());
        bookingTable.getTableHeader().setForeground(tm.getTableHeaderText());

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    @Override
    public void dispose() {
        ThemeManager.getInstance().removeListener(this);
        super.dispose();
    }

    private void loadDropdownData() {
        SwingWorker<List<Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object> doInBackground() {
                List<Bus> buses = busDAO.getAllBuses();
                List<Passenger> passengers = passengerDAO.getAllPassengers();
                return List.of(buses, passengers);
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    List<Object> data = get();
                    List<Bus> buses = (List<Bus>) data.get(0);
                    List<Passenger> passengers = (List<Passenger>) data.get(1);

                    busCombo.removeAllItems();
                    for (Bus b : buses) {
                        busCombo.addItem(b);
                    }

                    passengerCombo.removeAllItems();
                    for (Passenger p : passengers) {
                        passengerCombo.addItem(p);
                    }

                    onBusSelected();
                    onPassengerSelected();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void onBusSelected() {
        Bus bus = (Bus) busCombo.getSelectedItem();
        if (bus == null) return;

        routeLabel.setText("Route: " + bus.getSource() + " ➔ " + bus.getDestination() + " (" + bus.getBusType() + ")");
        departureLabel.setText("Departure: " + bus.getFormattedDeparture());

        boolean isDeparted = bus.isDeparted();
        if (isDeparted) {
            busStatusBadge.setText("DEPARTED 🔒");
            busStatusBadge.setForeground(new Color(127, 140, 141));
            bookButton.setEnabled(false);
            bookButton.setToolTipText("Cannot book seats on a departed bus.");
        } else if ("CANCELLED".equalsIgnoreCase(bus.getStatus())) {
            busStatusBadge.setText("CANCELLED ❌");
            busStatusBadge.setForeground(new Color(231, 76, 60));
            bookButton.setEnabled(false);
            bookButton.setToolTipText("Cannot book seats on a cancelled schedule.");
        } else {
            busStatusBadge.setText("SCHEDULED ✓");
            busStatusBadge.setForeground(new Color(39, 174, 96));
            bookButton.setEnabled(true);
            bookButton.setToolTipText(null);
        }

        baseFareLabel.setText(String.format("Base Rate: $%.2f / seat", bus.getFare()));

        // Load seats for this bus
        SwingWorker<Map<Integer, Seat>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<Integer, Seat> doInBackground() {
                return bookingDAO.getSeatsForBus(bus.getBusId(), bus.getTotalSeats());
            }

            @Override
            protected void done() {
                try {
                    currentSeatMap = get();
                    applySeatingPreference();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void onPassengerSelected() {
        Passenger p = (Passenger) passengerCombo.getSelectedItem();
        if (p != null) {
            if (p.getGender() != null) {
                seatGenderCombo.setSelectedItem(p.getGender().toUpperCase());
            }
        }
        applySeatingPreference();
        updateFareCalculations();
    }

    private void applySeatingPreference() {
        if (currentSeatMap == null) return;
        Bus bus = (Bus) busCombo.getSelectedItem();
        int totalSeats = (bus != null) ? bus.getTotalSeats() : 40;
        String pref = (String) preferenceCombo.getSelectedItem();
        String gender = (String) seatGenderCombo.getSelectedItem();

        SeatService.applyPreferenceFilter(currentSeatMap, totalSeats, pref, gender);
        seatMapPanel.setSeats(totalSeats, currentSeatMap);
    }

    private void updateFareCalculations() {
        Bus bus = (Bus) busCombo.getSelectedItem();
        Passenger p = (Passenger) passengerCombo.getSelectedItem();
        double baseRate = (bus != null) ? bus.getFare() : 0.0;
        int count = currentlySelectedSeats.size();

        int concessionPercent = FareCalculator.getConcessionPercentage(p);
        double total = FareCalculator.calculateTotalFare(baseRate, count, concessionPercent);

        selectedSeatsLabel.setText("Selected Seats: " + (count == 0 ? "None (0/6)" : currentlySelectedSeats.toString() + " (" + count + "/6)"));
        if (concessionPercent > 0) {
            concessionLabel.setText("<html>Concession: <b style='color:#27ae60;'>100% (Disability Concession)</b></html>");
        } else {
            concessionLabel.setText("Concession: 0% (Standard Fare)");
        }
        totalFareLabel.setText(String.format("Total Fare: $%.2f", total));
    }

    private void bookSelectedSeats() {
        Bus bus = (Bus) busCombo.getSelectedItem();
        Passenger passenger = (Passenger) passengerCombo.getSelectedItem();
        String seatGender = (String) seatGenderCombo.getSelectedItem();

        if (bus == null || passenger == null) {
            JOptionPane.showMessageDialog(this, "Please select both a bus and a passenger.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (bus.isDeparted()) {
            JOptionPane.showMessageDialog(this, "Cannot book seats: The bus '" + bus.getBusName() + "' has already departed.", "Departure Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentlySelectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least 1 seat on the bus layout map.", "No Seats Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User currentUser = AuthService.getInstance().getCurrentUser();
        String username = (currentUser != null) ? currentUser.getUsername() : passenger.getName();
        if (!RateLimiter.getInstance().canBook(username)) {
            JOptionPane.showMessageDialog(this,
                    "Booking rate limit reached (max 5 bookings per minute). Please wait a moment before trying again.",
                    "Rate Limit Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int count = currentlySelectedSeats.size();
        int concessionPercent = FareCalculator.getConcessionPercentage(passenger);
        double total = FareCalculator.calculateTotalFare(bus.getFare(), count, concessionPercent);

        String message = String.format("Confirm Reservation:\n- Passenger: %s\n- Bus: %s (%s -> %s)\n- Seats: %s (Total: %d)\n- Concession: %d%%\n- Total Fare: $%.2f",
                passenger.getName(), bus.getBusName(), bus.getSource(), bus.getDestination(),
                currentlySelectedSeats.toString(), count, concessionPercent, total);

        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Seat Booking", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        bookButton.setEnabled(false);

        SwingWorker<BookingGroup, Void> worker = new SwingWorker<>() {
            @Override
            protected BookingGroup doInBackground() throws Exception {
                return bookingDAO.bookSeats(passenger.getPassengerId(), bus.getBusId(), new ArrayList<>(currentlySelectedSeats), seatGender);
            }

            @Override
            protected void done() {
                bookButton.setEnabled(true);
                try {
                    BookingGroup bg = get();
                    RateLimiter.getInstance().recordBooking(username);

                    JOptionPane.showMessageDialog(BookingManagement.this,
                            "Booking Successful!\nGroup ID: " + bg.getGroupId() + "\nSeats: " + bg.getSeatNumbersAsString(),
                            "Reservation Confirmed", JOptionPane.INFORMATION_MESSAGE);

                    seatMapPanel.clearSelection();
                    onBusSelected();
                    loadBookingsTable();

                    int printOpt = JOptionPane.showConfirmDialog(BookingManagement.this,
                            "Would you like to view & print the Boarding Pass now?",
                            "Print Boarding Pass", JOptionPane.YES_NO_OPTION);
                    if (printOpt == JOptionPane.YES_OPTION) {
                        TicketGeneration tg = new TicketGeneration();
                        tg.setVisible(true);
                        tg.loadBookingById(bg.getGroupId());
                    }

                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(BookingManagement.this,
                            "Booking Failed: " + cause.getMessage(),
                            "Reservation Error", JOptionPane.ERROR_MESSAGE);
                    onBusSelected();
                }
            }
        };

        worker.execute();
    }

    private void cancelSelectedBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int groupId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
        String currentStatus = tableModel.getValueAt(selectedRow, 7).toString();

        if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This reservation has already been cancelled.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel reservation Group #" + groupId + "?\nAll occupied seats will be released.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return bookingDAO.cancelBookingGroup(groupId);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(BookingManagement.this, "Reservation Group #" + groupId + " cancelled successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        onBusSelected();
                        loadBookingsTable();
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(BookingManagement.this, "Cancellation Error: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void openTicketForSelected() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int groupId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
        TicketGeneration tg = new TicketGeneration();
        tg.setVisible(true);
        tg.loadBookingById(groupId);
    }

    private void loadBookingsTable() {
        SwingWorker<List<BookingGroup>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BookingGroup> doInBackground() {
                return bookingDAO.getAllBookingGroups();
            }

            @Override
            protected void done() {
                try {
                    List<BookingGroup> list = get();
                    tableModel.setRowCount(0);
                    for (BookingGroup bg : list) {
                        String status = "CONFIRMED";
                        if (bg.getBookings() != null && !bg.getBookings().isEmpty()) {
                            status = bg.getBookings().get(0).getStatus();
                        }
                        tableModel.addRow(new Object[]{
                                bg.getGroupId(),
                                bg.getPassengerName(),
                                bg.getBusName(),
                                bg.getSource() + " ➔ " + bg.getDestination(),
                                bg.getSeatNumbersAsString(),
                                String.format("%.2f", bg.getTotalFare()),
                                bg.getBusDepartureTime() != null ? bg.getBusDepartureTime() : "-",
                                status
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}
