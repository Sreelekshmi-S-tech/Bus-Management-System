package gui;

import dao.BookingDAO;
import model.BookingGroup;
import util.AppIcon;
import util.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TicketGeneration extends JFrame implements ThemeManager.ThemeChangeListener {

    private final BookingDAO bookingDAO = new BookingDAO();

    private JTextField searchField;
    private JTextArea ticketPreviewArea;
    private ModernButton printButton;
    private ModernButton exportButton;
    private ModernButton searchBtn;
    private ModernButton closeBtn;

    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel searchPanel;
    private JLabel searchLbl;

    private BookingGroup currentBookingGroup;

    public TicketGeneration() {
        setTitle("Official Boarding Pass & E-Ticket Generator - Bus Reservation System");
        setSize(780, 720);
        setMinimumSize(new Dimension(720, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();

        ThemeManager.getInstance().addListener(this);
        applyTheme(ThemeManager.getInstance().isDarkMode());
    }

    private void initUI() {
        ThemeManager tm = ThemeManager.getInstance();
        mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(tm.getBgColor());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Header Strip
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(tm.getHeaderBg());
        headerPanel.setPreferredSize(new Dimension(740, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("Official Boarding Pass & E-Ticket Generator", AppIcon.of(AppIcon.Type.TICKET, 22, Color.WHITE), SwingConstants.LEFT);
        title.setIconTextGap(10);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Search Bar Panel
        searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        searchPanel.setBackground(tm.getCardBg());
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        searchLbl = new JLabel("Enter Booking Group ID:");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchLbl.setForeground(tm.getTextPrimary());

        searchField = new JTextField(12);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBackground(tm.getInputBg());
        searchField.setForeground(tm.getInputText());
        searchField.setCaretColor(tm.getInputText());
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getInputBorder(), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        searchBtn = new ModernButton("Search & Load Ticket", AppIcon.of(AppIcon.Type.SEARCH, 13, Color.WHITE), tm.getPrimaryBlue(), Color.WHITE);
        searchBtn.setCornerRadius(6);
        searchBtn.addActionListener(e -> searchBooking());
        searchField.addActionListener(e -> searchBooking());

        searchPanel.add(searchLbl);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        // Center: Ticket Receipt Preview
        ticketPreviewArea = new JTextArea();
        ticketPreviewArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ticketPreviewArea.setEditable(false);
        ticketPreviewArea.setBackground(tm.isDarkMode() ? new Color(20, 28, 44) : new Color(254, 254, 252));
        ticketPreviewArea.setForeground(tm.isDarkMode() ? new Color(226, 232, 240) : new Color(30, 30, 30));
        ticketPreviewArea.setMargin(new Insets(20, 25, 20, 25));

        ticketPreviewArea.setText("\n\n\n\n                  ================================================\n" +
                                 "                  Please enter a Booking Group ID above and click\n" +
                                 "                  'Search & Load Ticket' to generate boarding pass.\n" +
                                 "                  ================================================");

        JScrollPane scrollPane = new JScrollPane(ticketPreviewArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(tm.getCardBorder()));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom Actions Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setOpaque(false);

        printButton = new ModernButton("Print Boarding Pass", AppIcon.of(AppIcon.Type.PRINT, 14, Color.WHITE), tm.getSuccessGreen(), Color.WHITE);
        printButton.setCornerRadius(6);
        printButton.setEnabled(false);
        printButton.addActionListener(e -> printTicket());

        exportButton = new ModernButton("Save Receipt as TXT", AppIcon.of(AppIcon.Type.CHECK, 14, Color.WHITE), new Color(71, 85, 105), Color.WHITE);
        exportButton.setCornerRadius(6);
        exportButton.setEnabled(false);
        exportButton.addActionListener(e -> exportToTxt());

        closeBtn = new ModernButton("Close", null, new Color(120, 130, 145), Color.WHITE);
        closeBtn.setCornerRadius(6);
        closeBtn.addActionListener(e -> dispose());

        bottomPanel.add(exportButton);
        bottomPanel.add(printButton);
        bottomPanel.add(closeBtn);
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

        searchPanel.setBackground(tm.getCardBg());
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        searchLbl.setForeground(tm.getTextPrimary());

        searchField.setBackground(tm.getInputBg());
        searchField.setForeground(tm.getInputText());
        searchField.setCaretColor(tm.getInputText());

        ticketPreviewArea.setBackground(isDark ? new Color(20, 28, 44) : new Color(254, 254, 252));
        ticketPreviewArea.setForeground(isDark ? new Color(226, 232, 240) : new Color(30, 30, 30));

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    @Override
    public void dispose() {
        ThemeManager.getInstance().removeListener(this);
        super.dispose();
    }

    public void loadBookingById(int groupId) {
        searchField.setText(String.valueOf(groupId));
        searchBooking();
    }

    private void searchBooking() {
        String input = searchField.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Booking Group ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int groupId;
        try {
            groupId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Booking Group ID must be a numeric number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<BookingGroup, Void> worker = new SwingWorker<>() {
            @Override
            protected BookingGroup doInBackground() {
                return bookingDAO.getBookingGroupById(groupId);
            }

            @Override
            protected void done() {
                try {
                    currentBookingGroup = get();
                    if (currentBookingGroup == null) {
                        JOptionPane.showMessageDialog(TicketGeneration.this, "No booking found with Group ID #" + groupId, "Not Found", JOptionPane.INFORMATION_MESSAGE);
                        ticketPreviewArea.setText("\n\n                  No booking record found for Group ID #" + groupId);
                        printButton.setEnabled(false);
                        exportButton.setEnabled(false);
                        return;
                    }

                    renderTicket(currentBookingGroup);
                    printButton.setEnabled(true);
                    exportButton.setEnabled(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void renderTicket(BookingGroup bg) {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("                       BUS MANAGEMENT & EXPRESS LINES                          \n");
        sb.append("                             OFFICIAL BOARDING PASS                             \n");
        sb.append("================================================================================\n\n");

        sb.append(String.format("  BOOKING GROUP ID : #%-15d  BOOKING DATE: %s\n",
                bg.getGroupId(),
                bg.getBookingTime() != null ? bg.getBookingTime().toString().substring(0, 19) : "N/A"));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("  PASSENGER DETAILS:\n");
        sb.append(String.format("    Name   : %-30s Gender : %s\n", bg.getPassengerName(), bg.getPassengerGender()));
        sb.append(String.format("    Phone  : %-30s ID No  : #%d\n", bg.getPassengerPhone(), bg.getPassengerId()));

        if (bg.isHasDisability()) {
            sb.append(String.format("    Concession Status : SPECIAL DISABILITY CONCESSION APPLIED (100%%)\n"));
            sb.append(String.format("    Certificate No    : %s\n", bg.getDisabilityIdNo() != null ? bg.getDisabilityIdNo() : "VERIFIED"));
        } else {
            sb.append("    Concession Status : Standard Fare (0% Concession)\n");
        }

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("  TRAVEL & ROUTE ITINERARY:\n");
        sb.append(String.format("    Bus Service    : %-30s Bus ID : #%d\n", bg.getBusName(), bg.getBusId()));
        sb.append(String.format("    Origin City    : %-30s Status : %s\n", bg.getSource(), bg.getBusStatus()));
        sb.append(String.format("    Destination    : %-30s\n", bg.getDestination()));
        sb.append(String.format("    Departure Time : %-30s\n", bg.getBusDepartureTime() != null ? bg.getBusDepartureTime() : "SCHEDULED"));

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("  SEAT RESERVATION & FARE BREAKDOWN:\n");
        sb.append(String.format("    Reserved Seats : %s  (Total: %d seat(s))\n",
                bg.getSeatNumbersAsString(),
                bg.getSeatNumbers() != null ? bg.getSeatNumbers().size() : 0));
        sb.append(String.format("    Base Fare/Seat : $%.2f\n", bg.getBaseFare()));

        if (bg.isHasDisability()) {
            sb.append("    Concession     : Disability Concession (100% Free Travel)\n");
            sb.append("    Total Fare Due : $0.00 (PAID / WAIVED)\n");
        } else {
            sb.append(String.format("    Total Fare Due : $%.2f (PAID)\n", bg.getTotalFare()));
        }

        sb.append("\n================================================================================\n");
        sb.append("  SECURITY & BOARDING INSTRUCTIONS:\n");
        sb.append("  - Please present this digital or printed boarding pass 15 minutes prior to departure.\n");
        sb.append("  - Valid government photo ID is mandatory along with this ticket.\n");
        sb.append("  - If travelling under disability concession, valid disability card is required.\n");
        sb.append("  - Luggage allowance: 2 standard bags per passenger.\n");
        sb.append("================================================================================\n");
        sb.append("           [ BARCODE: *BUS-").append(bg.getGroupId()).append("-").append(bg.getBusId()).append("* ]            SAFE JOURNEY!\n");
        sb.append("================================================================================\n");

        ticketPreviewArea.setText(sb.toString());
        ticketPreviewArea.setCaretPosition(0);
    }

    private void printTicket() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Bus_Boarding_Pass_Group_" + (currentBookingGroup != null ? currentBookingGroup.getGroupId() : "Receipt"));

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                g2d.scale(0.82, 0.82);
                ticketPreviewArea.printAll(g2d);
                return PAGE_EXISTS;
            }
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "Boarding pass sent to printer / PDF exporter successfully!", "Printing Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(), "Printer Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToTxt() {
        if (currentBookingGroup == null) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Boarding_Pass_Group_" + currentBookingGroup.getGroupId() + ".txt"));
        int userChoice = fileChooser.showSaveDialog(this);

        if (userChoice == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(ticketPreviewArea.getText());
                JOptionPane.showMessageDialog(this, "Ticket receipt saved to:\n" + fileToSave.getAbsolutePath(), "Receipt Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to save file: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
