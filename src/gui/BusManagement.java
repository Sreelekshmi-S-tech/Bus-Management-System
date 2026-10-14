package gui;

import dao.BusDAO;
import model.Bus;
import util.AppIcon;
import util.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class BusManagement extends JFrame implements ThemeManager.ThemeChangeListener {

    private final BusDAO busDAO = new BusDAO();

    private JTextField idField;
    private JTextField nameField;
    private JTextField sourceField;
    private JTextField destField;
    private JTextField seatsField;
    private JTextField fareField;
    private JTextField departureField;
    private JComboBox<String> busTypeCombo;
    private JComboBox<String> statusCombo;

    private JTable busTable;
    private DefaultTableModel tableModel;

    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel formPanel;
    private JLabel formTitle;
    private ModernButton refreshBtn;
    private ModernButton addBtn;
    private ModernButton updateBtn;
    private ModernButton deleteBtn;
    private ModernButton clearBtn;

    public BusManagement() {
        setTitle("Bus Fleet Management Subsystem - Bus Reservation System");
        setSize(1040, 660);
        setMinimumSize(new Dimension(980, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadBuses();

        ThemeManager.getInstance().addListener(this);
        applyTheme(ThemeManager.getInstance().isDarkMode());
    }

    private void initUI() {
        ThemeManager tm = ThemeManager.getInstance();
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(tm.getBgColor());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Header Strip
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(tm.getHeaderBg());
        headerPanel.setPreferredSize(new Dimension(980, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("Bus Fleet, Schedules & Departure Management", AppIcon.of(AppIcon.Type.BUS_FRONT, 22, Color.WHITE), SwingConstants.LEFT);
        title.setIconTextGap(10);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);

        refreshBtn = new ModernButton("Refresh / Sync", AppIcon.of(AppIcon.Type.REFRESH, 13, Color.WHITE), tm.getPrimaryBlue(), Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(140, 32));
        refreshBtn.setCornerRadius(6);
        refreshBtn.addActionListener(e -> loadBuses());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Split
        JPanel centerPanel = new JPanel(new BorderLayout(15, 0));
        centerPanel.setOpaque(false);

        // Left Form Panel
        formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setPreferredSize(new Dimension(380, 520));
        formPanel.setBackground(tm.getCardBg());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        formTitle = new JLabel("Bus Details & Schedule");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(tm.getTextPrimary());
        formTitle.setBounds(20, 10, 320, 20);
        formPanel.add(formTitle);

        int y = 38;
        int step = 48;

        // Bus ID
        JLabel idLbl = new JLabel("Bus ID:");
        idLbl.setBounds(20, y, 100, 16);
        idLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        idLbl.setForeground(tm.getTextPrimary());
        idField = new JTextField();
        idField.setBounds(20, y + 16, 330, 26);
        idField.setEditable(false);
        idField.setBackground(tm.getInputBg());
        idField.setForeground(tm.getInputText());
        formPanel.add(idLbl);
        formPanel.add(idField);

        // Bus Name
        y += step;
        JLabel nameLbl = new JLabel("Bus Name / Service:");
        nameLbl.setBounds(20, y, 160, 16);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nameLbl.setForeground(tm.getTextPrimary());
        nameField = new JTextField();
        nameField.setBounds(20, y + 16, 330, 26);
        nameField.setBackground(tm.getInputBg());
        nameField.setForeground(tm.getInputText());
        nameField.setCaretColor(tm.getInputText());
        formPanel.add(nameLbl);
        formPanel.add(nameField);

        // Source & Destination
        y += step;
        JLabel srcLbl = new JLabel("Source City:");
        srcLbl.setBounds(20, y, 120, 16);
        srcLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        srcLbl.setForeground(tm.getTextPrimary());
        sourceField = new JTextField();
        sourceField.setBounds(20, y + 16, 155, 26);
        sourceField.setBackground(tm.getInputBg());
        sourceField.setForeground(tm.getInputText());
        sourceField.setCaretColor(tm.getInputText());
        formPanel.add(srcLbl);
        formPanel.add(sourceField);

        JLabel destLbl = new JLabel("Destination City:");
        destLbl.setBounds(195, y, 120, 16);
        destLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        destLbl.setForeground(tm.getTextPrimary());
        destField = new JTextField();
        destField.setBounds(195, y + 16, 155, 26);
        destField.setBackground(tm.getInputBg());
        destField.setForeground(tm.getInputText());
        destField.setCaretColor(tm.getInputText());
        formPanel.add(destLbl);
        formPanel.add(destField);

        // Total Seats & Fare
        y += step;
        JLabel seatsLbl = new JLabel("Total Capacity:");
        seatsLbl.setBounds(20, y, 120, 16);
        seatsLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        seatsLbl.setForeground(tm.getTextPrimary());
        seatsField = new JTextField("40");
        seatsField.setBounds(20, y + 16, 155, 26);
        seatsField.setBackground(tm.getInputBg());
        seatsField.setForeground(tm.getInputText());
        seatsField.setCaretColor(tm.getInputText());
        formPanel.add(seatsLbl);
        formPanel.add(seatsField);

        JLabel fareLbl = new JLabel("Base Fare ($):");
        fareLbl.setBounds(195, y, 120, 16);
        fareLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        fareLbl.setForeground(tm.getTextPrimary());
        fareField = new JTextField("45.00");
        fareField.setBounds(195, y + 16, 155, 26);
        fareField.setBackground(tm.getInputBg());
        fareField.setForeground(tm.getInputText());
        fareField.setCaretColor(tm.getInputText());
        formPanel.add(fareLbl);
        formPanel.add(fareField);

        // Departure Time
        y += step;
        JLabel depLbl = new JLabel("Departure (YYYY-MM-DD HH:MM):");
        depLbl.setBounds(20, y, 250, 16);
        depLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        depLbl.setForeground(tm.getTextPrimary());
        departureField = new JTextField(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).format(Bus.FORMATTER));
        departureField.setBounds(20, y + 16, 330, 26);
        departureField.setBackground(tm.getInputBg());
        departureField.setForeground(tm.getInputText());
        departureField.setCaretColor(tm.getInputText());
        formPanel.add(depLbl);
        formPanel.add(departureField);

        // Bus Type & Status
        y += step;
        JLabel typeLbl = new JLabel("Bus Configuration:");
        typeLbl.setBounds(20, y, 140, 16);
        typeLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        typeLbl.setForeground(tm.getTextPrimary());
        busTypeCombo = new JComboBox<>(new String[]{"SEATER 2+2", "SLEEPER 2+1", "AC DELUXE 2+2"});
        busTypeCombo.setBounds(20, y + 16, 155, 26);
        busTypeCombo.setBackground(tm.getInputBg());
        busTypeCombo.setForeground(tm.getInputText());
        formPanel.add(typeLbl);
        formPanel.add(busTypeCombo);

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setBounds(195, y, 100, 16);
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusLbl.setForeground(tm.getTextPrimary());
        statusCombo = new JComboBox<>(new String[]{"SCHEDULED", "DEPARTED", "CANCELLED"});
        statusCombo.setBounds(195, y + 16, 155, 26);
        statusCombo.setBackground(tm.getInputBg());
        statusCombo.setForeground(tm.getInputText());
        formPanel.add(statusLbl);
        formPanel.add(statusCombo);

        // Action Buttons
        y += 56;
        addBtn = new ModernButton("Add Bus", AppIcon.of(AppIcon.Type.PLUS, 13, Color.WHITE), tm.getSuccessGreen(), Color.WHITE);
        addBtn.setBounds(20, y, 100, 32);
        addBtn.addActionListener(e -> addBus());

        updateBtn = new ModernButton("Update", AppIcon.of(AppIcon.Type.CHECK, 13, Color.WHITE), tm.getPrimaryBlue(), Color.WHITE);
        updateBtn.setBounds(135, y, 100, 32);
        updateBtn.addActionListener(e -> updateBus());

        deleteBtn = new ModernButton("Delete", AppIcon.of(AppIcon.Type.TRASH, 13, Color.WHITE), tm.getDangerRed(), Color.WHITE);
        deleteBtn.setBounds(250, y, 100, 32);
        deleteBtn.addActionListener(e -> deleteBus());

        clearBtn = new ModernButton("Clear Form", AppIcon.of(AppIcon.Type.REFRESH, 12, Color.WHITE), new Color(120, 130, 145), Color.WHITE);
        clearBtn.setBounds(20, y + 38, 330, 28);
        clearBtn.addActionListener(e -> clearForm());

        formPanel.add(addBtn);
        formPanel.add(updateBtn);
        formPanel.add(deleteBtn);
        formPanel.add(clearBtn);

        centerPanel.add(formPanel, BorderLayout.WEST);

        // Right Table
        String[] columns = {"ID", "Name", "Source", "Destination", "Seats", "Fare ($)", "Departure", "Type", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        busTable = new JTable(tableModel);
        busTable.setRowHeight(28);
        busTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        busTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        busTable.getTableHeader().setBackground(tm.getTableHeaderBg());
        busTable.getTableHeader().setForeground(tm.getTableHeaderText());
        busTable.setBackground(tm.getTableBg());
        busTable.setForeground(tm.getTextPrimary());
        busTable.setGridColor(tm.getTableGrid());
        busTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer for Status column (index 8)
        busTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String val = (value != null) ? value.toString() : "";
                if ("SCHEDULED".equalsIgnoreCase(val)) {
                    c.setForeground(new Color(39, 174, 96));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if ("DEPARTED".equalsIgnoreCase(val)) {
                    c.setForeground(new Color(127, 140, 141));
                    setFont(getFont().deriveFont(Font.ITALIC));
                } else if ("CANCELLED".equalsIgnoreCase(val)) {
                    c.setForeground(new Color(231, 76, 60));
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return c;
            }
        });

        busTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && busTable.getSelectedRow() != -1) {
                int r = busTable.getSelectedRow();
                idField.setText(tableModel.getValueAt(r, 0).toString());
                nameField.setText(tableModel.getValueAt(r, 1).toString());
                sourceField.setText(tableModel.getValueAt(r, 2).toString());
                destField.setText(tableModel.getValueAt(r, 3).toString());
                seatsField.setText(tableModel.getValueAt(r, 4).toString());
                fareField.setText(tableModel.getValueAt(r, 5).toString());
                departureField.setText(tableModel.getValueAt(r, 6).toString());
                busTypeCombo.setSelectedItem(tableModel.getValueAt(r, 7).toString());
                statusCombo.setSelectedItem(tableModel.getValueAt(r, 8).toString());
            }
        });

        JScrollPane scrollPane = new JScrollPane(busTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(tm.getCardBorder()));
        scrollPane.getViewport().setBackground(tm.getTableBg());
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
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
        formPanel.setBackground(tm.getCardBg());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        formTitle.setForeground(tm.getTextPrimary());

        busTable.setBackground(tm.getTableBg());
        busTable.setForeground(tm.getTextPrimary());
        busTable.setGridColor(tm.getTableGrid());
        busTable.getTableHeader().setBackground(tm.getTableHeaderBg());
        busTable.getTableHeader().setForeground(tm.getTableHeaderText());

        idField.setBackground(tm.getInputBg());
        idField.setForeground(tm.getInputText());
        nameField.setBackground(tm.getInputBg());
        nameField.setForeground(tm.getInputText());
        nameField.setCaretColor(tm.getInputText());
        sourceField.setBackground(tm.getInputBg());
        sourceField.setForeground(tm.getInputText());
        sourceField.setCaretColor(tm.getInputText());
        destField.setBackground(tm.getInputBg());
        destField.setForeground(tm.getInputText());
        destField.setCaretColor(tm.getInputText());
        seatsField.setBackground(tm.getInputBg());
        seatsField.setForeground(tm.getInputText());
        seatsField.setCaretColor(tm.getInputText());
        fareField.setBackground(tm.getInputBg());
        fareField.setForeground(tm.getInputText());
        fareField.setCaretColor(tm.getInputText());
        departureField.setBackground(tm.getInputBg());
        departureField.setForeground(tm.getInputText());
        departureField.setCaretColor(tm.getInputText());

        busTypeCombo.setBackground(tm.getInputBg());
        busTypeCombo.setForeground(tm.getInputText());
        statusCombo.setBackground(tm.getInputBg());
        statusCombo.setForeground(tm.getInputText());

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    @Override
    public void dispose() {
        ThemeManager.getInstance().removeListener(this);
        super.dispose();
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        sourceField.setText("");
        destField.setText("");
        seatsField.setText("40");
        fareField.setText("45.00");
        departureField.setText(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).format(Bus.FORMATTER));
        busTypeCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        busTable.clearSelection();
    }

    private void loadBuses() {
        SwingWorker<List<Bus>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Bus> doInBackground() {
                return busDAO.getAllBuses();
            }

            @Override
            protected void done() {
                try {
                    List<Bus> list = get();
                    tableModel.setRowCount(0);
                    for (Bus b : list) {
                        tableModel.addRow(new Object[]{
                                b.getBusId(),
                                b.getBusName(),
                                b.getSource(),
                                b.getDestination(),
                                b.getTotalSeats(),
                                String.format("%.2f", b.getFare()),
                                b.getFormattedDeparture(),
                                b.getBusType(),
                                b.getStatus()
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void addBus() {
        String name = nameField.getText().trim();
        String src = sourceField.getText().trim();
        String dest = destField.getText().trim();
        String seatsStr = seatsField.getText().trim();
        String fareStr = fareField.getText().trim();
        String depStr = departureField.getText().trim();
        String type = (String) busTypeCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();

        if (name.isEmpty() || src.isEmpty() || dest.isEmpty() || seatsStr.isEmpty() || fareStr.isEmpty() || depStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all bus information fields.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int seats;
        double fare;
        LocalDateTime depTime;

        try {
            seats = Integer.parseInt(seatsStr);
            if (seats <= 0 || seats > 80) {
                JOptionPane.showMessageDialog(this, "Total seats must be between 1 and 80.", "Invalid Seats", JOptionPane.WARNING_MESSAGE);
                return;
            }
            fare = Double.parseDouble(fareStr);
            if (fare < 0) {
                JOptionPane.showMessageDialog(this, "Fare cannot be negative.", "Invalid Fare", JOptionPane.WARNING_MESSAGE);
                return;
            }
            depTime = LocalDateTime.parse(depStr, Bus.FORMATTER);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Seats and Fare must be valid numeric values.", "Format Error", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (DateTimeParseException dtpe) {
            JOptionPane.showMessageDialog(this, "Departure time must match format 'YYYY-MM-DD HH:MM' (e.g., 2026-10-15 08:30)", "Date Format Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int newId = busDAO.getNextBusId();
        Bus b = new Bus(newId, name, src, dest, seats, fare, depTime, type, status);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return busDAO.addBus(b);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(BusManagement.this, "Bus fleet entry added successfully! (ID: " + newId + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadBuses();
                    } else {
                        JOptionPane.showMessageDialog(BusManagement.this, "Failed to add bus record.", "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void updateBus() {
        String idStr = idField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a bus from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idStr);
        String name = nameField.getText().trim();
        String src = sourceField.getText().trim();
        String dest = destField.getText().trim();
        int seats = Integer.parseInt(seatsField.getText().trim());
        double fare = Double.parseDouble(fareField.getText().trim());
        LocalDateTime depTime = LocalDateTime.parse(departureField.getText().trim(), Bus.FORMATTER);
        String type = (String) busTypeCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();

        Bus b = new Bus(id, name, src, dest, seats, fare, depTime, type, status);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return busDAO.updateBus(b);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(BusManagement.this, "Bus details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadBuses();
                    } else {
                        JOptionPane.showMessageDialog(BusManagement.this, "Failed to update bus details.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void deleteBus() {
        String idStr = idField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a bus to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Bus ID " + idStr + "?\nAll associated bookings will also be removed.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int id = Integer.parseInt(idStr);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return busDAO.deleteBus(id);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(BusManagement.this, "Bus deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadBuses();
                    } else {
                        JOptionPane.showMessageDialog(BusManagement.this, "Failed to delete bus.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}