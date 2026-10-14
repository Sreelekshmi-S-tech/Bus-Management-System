package gui;

import database.DBConnection;
import model.User;
import service.AuthService;
import util.AppIcon;
import util.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Dashboard extends JFrame implements ThemeManager.ThemeChangeListener {

    private JLabel totalBusesLabel;
    private JLabel totalPassengersLabel;
    private JLabel activeBookingsLabel;
    private JLabel todayDeparturesLabel;
    private JLabel userBadgeLabel;

    private JPanel mainPanel;
    private JPanel headerPanel;
    private JLabel titleLabel;
    private JLabel subTitleLabel;
    private ModernButton themeToggleBtn;
    private ModernButton logoutBtn;

    private final List<JPanel> statCards = new ArrayList<>();
    private final List<JLabel> statTitleLabels = new ArrayList<>();
    private final List<AppIcon> statIcons = new ArrayList<>();
    private final List<JLabel> statIconLabels = new ArrayList<>();

    private final List<JPanel> navCards = new ArrayList<>();
    private final List<JLabel> navTitleLabels = new ArrayList<>();
    private final List<JLabel> navDescLabels = new ArrayList<>();
    private final List<AppIcon> navIcons = new ArrayList<>();
    private final List<JLabel> navIconLabels = new ArrayList<>();
    private final List<ModernButton> navButtons = new ArrayList<>();

    public Dashboard() {
        setTitle("Bus Management & Reservation System - Main Dashboard");
        setSize(1020, 720);
        setMinimumSize(new Dimension(960, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        refreshStats();

        // Register for real-time theme updates
        ThemeManager.getInstance().addListener(this);
        applyTheme(ThemeManager.getInstance().isDarkMode());
    }

    private void initUI() {
        ThemeManager tm = ThemeManager.getInstance();
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(tm.getBgColor());

        // ==========================================
        // 1. Header Strip with Vector Logo & Branding
        // ==========================================
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(tm.getHeaderBg());
        headerPanel.setPreferredSize(new Dimension(1020, 88));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        headerLeft.setOpaque(false);

        // Vector Bus Logo (Pure Java2D drawing, never missing glyph)
        JLabel busLogo = new JLabel(AppIcon.of(AppIcon.Type.BUS, 42, Color.WHITE));
        headerLeft.add(busLogo);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        titleLabel = new JLabel("Bus Reservation & Fleet System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 21));
        titleLabel.setForeground(Color.WHITE);

        subTitleLabel = new JLabel("Central Control Center & Real-Time Booking Terminal");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitleLabel.setForeground(tm.getHeaderSubtext());
        titleBlock.add(titleLabel);
        titleBlock.add(subTitleLabel);
        headerLeft.add(titleBlock);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        headerRight.setOpaque(false);

        User currentUser = AuthService.getInstance().getCurrentUser();
        String username = (currentUser != null) ? currentUser.getUsername() : "Guest";
        String role = (currentUser != null) ? currentUser.getRole() : "USER";

        // Vector Avatar User Badge (Fixes missing glyph '▯')
        userBadgeLabel = new JLabel(username + " [" + role + "]", AppIcon.of(AppIcon.Type.USER, 16, Color.WHITE), SwingConstants.LEFT);
        userBadgeLabel.setIconTextGap(8);
        userBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userBadgeLabel.setForeground(Color.WHITE);
        userBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(56, 189, 248), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        headerRight.add(userBadgeLabel);

        // Dark / Light Mode Toggle Button
        boolean isDark = tm.isDarkMode();
        themeToggleBtn = new ModernButton(
                isDark ? "Light Mode" : "Dark Mode",
                AppIcon.of(isDark ? AppIcon.Type.SUN : AppIcon.Type.MOON, 15, Color.WHITE),
                new Color(40, 56, 85),
                Color.WHITE
        );
        themeToggleBtn.setPreferredSize(new Dimension(130, 36));
        themeToggleBtn.setCornerRadius(6);
        themeToggleBtn.addActionListener(e -> tm.toggleTheme());
        headerRight.add(themeToggleBtn);

        // Logout Button
        logoutBtn = new ModernButton(
                "Logout",
                AppIcon.of(AppIcon.Type.LOGOUT, 14, Color.WHITE),
                tm.getDangerRed(),
                Color.WHITE
        );
        logoutBtn.setPreferredSize(new Dimension(105, 36));
        logoutBtn.setCornerRadius(6);
        logoutBtn.addActionListener(e -> {
            AuthService.getInstance().logout();
            new LoginFrame().setVisible(true);
            dispose();
        });
        headerRight.add(logoutBtn);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. Content Center Panel
        // ==========================================
        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(18, 30, 20, 30));

        // KPI Stat Cards Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 18, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(960, 95));

        totalBusesLabel = new JLabel("...", SwingConstants.LEFT);
        totalPassengersLabel = new JLabel("...", SwingConstants.LEFT);
        activeBookingsLabel = new JLabel("...", SwingConstants.LEFT);
        todayDeparturesLabel = new JLabel("...", SwingConstants.LEFT);

        statsPanel.add(createStatCard("Total Buses", totalBusesLabel, tm.getPrimaryBlue(), AppIcon.Type.BUS));
        statsPanel.add(createStatCard("Registered Passengers", totalPassengersLabel, tm.getSuccessGreen(), AppIcon.Type.USERS));
        statsPanel.add(createStatCard("Active Bookings", activeBookingsLabel, tm.getPurpleAccent(), AppIcon.Type.TICKET));
        statsPanel.add(createStatCard("Today's Departures", todayDeparturesLabel, tm.getOrangeAccent(), AppIcon.Type.CLOCK));

        contentPanel.add(statsPanel, BorderLayout.NORTH);

        // Navigation Action Cards Panel
        JPanel navGrid = new JPanel(new GridLayout(2, 2, 22, 22));
        navGrid.setOpaque(false);

        boolean isAdmin = AuthService.getInstance().isAdmin();

        // 1. Passenger Management Card
        navGrid.add(createNavCard(
                "Passenger Management",
                "Register, update, and search passenger profiles with disability status",
                AppIcon.Type.USER,
                tm.getPrimaryBlue(),
                isAdmin,
                () -> new PassengerManagement().setVisible(true)
        ));

        // 2. Bus Fleet Management Card
        navGrid.add(createNavCard(
                "Bus Fleet Management",
                "Manage bus schedules, routes, seating types, and live departure statuses",
                AppIcon.Type.BUS_FRONT,
                tm.getSuccessGreen(),
                isAdmin,
                () -> new BusManagement().setVisible(true)
        ));

        // 3. Seat Booking & Reservation Card
        navGrid.add(createNavCard(
                "Seat Booking & Reservation",
                "Interactive seat map with neighbour-aware gender views & multi-seat booking",
                AppIcon.Type.SEAT,
                tm.getPurpleAccent(),
                true, // Available for all
                () -> new BookingManagement().setVisible(true)
        ));

        // 4. Boarding Pass & Ticket Generation Card
        navGrid.add(createNavCard(
                "Boarding Pass & Tickets",
                "Generate, view, and print official boarding pass tickets with concessions",
                AppIcon.Type.TICKET,
                tm.getOrangeAccent(),
                true, // Available for all
                () -> new TicketGeneration().setVisible(true)
        ));

        contentPanel.add(navGrid, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, AppIcon.Type iconType) {
        ThemeManager tm = ThemeManager.getInstance();
        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setBackground(tm.getCardBg());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        statCards.add(card);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(tm.getTextSecondary());
        statTitleLabels.add(titleLbl);

        AppIcon icon = AppIcon.of(iconType, 22, accentColor);
        JLabel iconLbl = new JLabel(icon);
        statIcons.add(icon);
        statIconLabels.add(iconLbl);

        top.add(titleLbl, BorderLayout.WEST);
        top.add(iconLbl, BorderLayout.EAST);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createNavCard(String title, String description, AppIcon.Type iconType, Color accent, boolean enabled, Runnable action) {
        ThemeManager tm = ThemeManager.getInstance();
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(tm.getCardBg());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        navCards.add(card);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        header.setOpaque(false);

        AppIcon icon = AppIcon.of(iconType, 36, accent);
        JLabel iconLbl = new JLabel(icon);
        navIcons.add(icon);
        navIconLabels.add(iconLbl);
        header.add(iconLbl);

        JPanel textBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        textBlock.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(tm.getTextPrimary());
        navTitleLabels.add(titleLbl);

        JLabel descLbl = new JLabel("<html><body style='width: 320px;'>" + description + "</body></html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLbl.setForeground(tm.getTextSecondary());
        navDescLabels.add(descLbl);

        textBlock.add(titleLbl);
        textBlock.add(descLbl);
        header.add(textBlock);

        // Modern Button with Vector Arrow / Lock Icon
        ModernButton openBtn = new ModernButton(
                enabled ? "Open Module" : "Admin Only",
                enabled ? AppIcon.of(AppIcon.Type.ARROW_RIGHT, 14, Color.WHITE) : AppIcon.of(AppIcon.Type.LOCK, 14, Color.WHITE),
                enabled ? accent : new Color(150, 160, 175),
                Color.WHITE
        );
        openBtn.setPreferredSize(new Dimension(0, 38));
        openBtn.setCornerRadius(8);
        openBtn.setEnabled(enabled);
        if (enabled) {
            openBtn.addActionListener(e -> action.run());
        }
        navButtons.add(openBtn);

        card.add(header, BorderLayout.CENTER);
        card.add(openBtn, BorderLayout.SOUTH);
        return card;
    }

    @Override
    public void onThemeChanged(boolean isDark) {
        applyTheme(isDark);
    }

    private void applyTheme(boolean isDark) {
        ThemeManager tm = ThemeManager.getInstance();

        // 1. Overall frame & header
        mainPanel.setBackground(tm.getBgColor());
        headerPanel.setBackground(tm.getHeaderBg());
        subTitleLabel.setForeground(tm.getHeaderSubtext());

        // 2. Toggle button state
        themeToggleBtn.setText(isDark ? "Light Mode" : "Dark Mode");
        themeToggleBtn.setIcon(AppIcon.of(isDark ? AppIcon.Type.SUN : AppIcon.Type.MOON, 15, Color.WHITE));
        themeToggleBtn.setColors(isDark ? new Color(51, 65, 85) : new Color(40, 56, 85), Color.WHITE);

        // 3. Stat cards
        for (JPanel card : statCards) {
            card.setBackground(tm.getCardBg());
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                    BorderFactory.createEmptyBorder(14, 18, 14, 18)
            ));
        }
        for (JLabel titleLbl : statTitleLabels) {
            titleLbl.setForeground(tm.getTextSecondary());
        }

        // 4. Nav cards
        for (JPanel card : navCards) {
            card.setBackground(tm.getCardBg());
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                    BorderFactory.createEmptyBorder(16, 20, 16, 20)
            ));
        }
        for (JLabel titleLbl : navTitleLabels) {
            titleLbl.setForeground(tm.getTextPrimary());
        }
        for (JLabel descLbl : navDescLabels) {
            descLbl.setForeground(tm.getTextSecondary());
        }

        // Revalidate and repaint everything smoothly
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    @Override
    public void dispose() {
        ThemeManager.getInstance().removeListener(this);
        super.dispose();
    }

    public void refreshStats() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                int buses = 0, passengers = 0, bookings = 0, todayDeps = 0;
                try (Connection con = DBConnection.getConnection()) {
                    // Total Buses
                    try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bus");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) buses = rs.getInt(1);
                    }
                    // Total Passengers
                    try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM passenger");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) passengers = rs.getInt(1);
                    }
                    // Active Bookings
                    try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM booking WHERE status = 'BOOKED'");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) bookings = rs.getInt(1);
                    }
                    // Today's Departures
                    try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bus WHERE DATE(departure_time) = CURDATE()");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) todayDeps = rs.getInt(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new int[]{buses, passengers, bookings, todayDeps};
            }

            @Override
            protected void done() {
                try {
                    int[] stats = get();
                    totalBusesLabel.setText(String.valueOf(stats[0]));
                    totalPassengersLabel.setText(String.valueOf(stats[1]));
                    activeBookingsLabel.setText(String.valueOf(stats[2]));
                    todayDeparturesLabel.setText(String.valueOf(stats[3]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}