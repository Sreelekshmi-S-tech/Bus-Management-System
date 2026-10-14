package gui;

import dao.PassengerDAO;
import model.Passenger;
import util.AppIcon;
import util.ThemeManager;
import util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PassengerManagement extends JFrame implements ThemeManager.ThemeChangeListener {

    private final PassengerDAO passengerDAO = new PassengerDAO();

    private JTextField idField;
    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<String> genderCombo;
    private JTextField phoneField;
    private JCheckBox disabilityCheckBox;
    private JTextField disabilityIdField;

    private JTable passengerTable;
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

    public PassengerManagement() {
        setTitle("Passenger Management Subsystem - Bus Reservation System");
        setSize(980, 640);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadPassengers();

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
        headerPanel.setPreferredSize(new Dimension(940, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("Passenger Profile & Disability Concession Management", AppIcon.of(AppIcon.Type.USER, 22, Color.WHITE), SwingConstants.LEFT);
        title.setIconTextGap(10);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);

        refreshBtn = new ModernButton("Refresh", AppIcon.of(AppIcon.Type.REFRESH, 13, Color.WHITE), tm.getPrimaryBlue(), Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(105, 32));
        refreshBtn.setCornerRadius(6);
        refreshBtn.addActionListener(e -> loadPassengers());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center split: Form on Left, Table on Right
        JPanel centerPanel = new JPanel(new BorderLayout(15, 0));
        centerPanel.setOpaque(false);

        // Left Form Panel
        formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setPreferredSize(new Dimension(360, 480));
        formPanel.setBackground(tm.getCardBg());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getCardBorder(), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        formTitle = new JLabel("Passenger Information");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(tm.getTextPrimary());
        formTitle.setBounds(20, 12, 300, 20);
        formPanel.add(formTitle);

        int y = 42;
        int step = 52;

        // ID Field
        JLabel idLbl = new JLabel("Passenger ID:");
        idLbl.setBounds(20, y, 120, 18);
        idLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        idLbl.setForeground(tm.getTextPrimary());
        idField = new JTextField();
        idField.setBounds(20, y + 18, 310, 28);
        idField.setEditable(false);
        idField.setBackground(tm.getInputBg());
        idField.setForeground(tm.getInputText());
        formPanel.add(idLbl);
        formPanel.add(idField);

        // Name
        y += step;
        JLabel nameLbl = new JLabel("Full Name:");
        nameLbl.setBounds(20, y, 120, 18);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLbl.setForeground(tm.getTextPrimary());
        nameField = new JTextField();
        nameField.setBounds(20, y + 18, 310, 28);
        nameField.setBackground(tm.getInputBg());
        nameField.setForeground(tm.getInputText());
        nameField.setCaretColor(tm.getInputText());
        formPanel.add(nameLbl);
        formPanel.add(nameField);

        // Age & Gender
        y += step;
        JLabel ageLbl = new JLabel("Age (1-120):");
        ageLbl.setBounds(20, y, 100, 18);
        ageLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ageLbl.setForeground(tm.getTextPrimary());
        ageField = new JTextField();
        ageField.setBounds(20, y + 18, 140, 28);
        ageField.setBackground(tm.getInputBg());
        ageField.setForeground(tm.getInputText());
        ageField.setCaretColor(tm.getInputText());
        formPanel.add(ageLbl);
        formPanel.add(ageField);

        JLabel genderLbl = new JLabel("Gender:");
        genderLbl.setBounds(180, y, 100, 18);
        genderLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        genderLbl.setForeground(tm.getTextPrimary());
        genderCombo = new JComboBox<>(new String[]{"MALE", "FEMALE", "OTHERS"});
        genderCombo.setBounds(180, y + 18, 150, 28);
        genderCombo.setBackground(tm.getInputBg());
        genderCombo.setForeground(tm.getInputText());
        formPanel.add(genderLbl);
        formPanel.add(genderCombo);

        // Phone
        y += step;
        JLabel phoneLbl = new JLabel("Phone (10 digits):");
        phoneLbl.setBounds(20, y, 150, 18);
        phoneLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        phoneLbl.setForeground(tm.getTextPrimary());
        phoneField = new JTextField();
        phoneField.setBounds(20, y + 18, 310, 28);
        phoneField.setBackground(tm.getInputBg());
        phoneField.setForeground(tm.getInputText());
        phoneField.setCaretColor(tm.getInputText());
        formPanel.add(phoneLbl);
        formPanel.add(phoneField);

        // Disability Checkbox & ID Field
        y += step;
        disabilityCheckBox = new JCheckBox("Has Disability (Eligible for Concession)");
        disabilityCheckBox.setBounds(20, y, 310, 22);
        disabilityCheckBox.setFont(new Font("Segoe UI", Font.BOLD, 11));
        disabilityCheckBox.setForeground(tm.getPrimaryBlue());
        disabilityCheckBox.setOpaque(false);
        formPanel.add(disabilityCheckBox);

        JLabel disIdLbl = new JLabel("Disability ID / Certificate No:");
        disIdLbl.setBounds(20, y + 24, 250, 16);
        disIdLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        disIdLbl.setForeground(tm.getTextSecondary());
        disabilityIdField = new JTextField();
        disabilityIdField.setBounds(20, y + 42, 310, 28);
        disabilityIdField.setEnabled(false);
        disabilityIdField.setBackground(tm.getInputBg());
        disabilityIdField.setForeground(tm.getInputText());
        disabilityIdField.setCaretColor(tm.getInputText());
        formPanel.add(disIdLbl);
        formPanel.add(disabilityIdField);

        disabilityCheckBox.addActionListener(e -> {
            boolean checked = disabilityCheckBox.isSelected();
            disabilityIdField.setEnabled(checked);
            if (!checked) {
                disabilityIdField.setText("");
            }
        });

        // Action Modern Buttons
        y += 82;
        addBtn = new ModernButton("Add", AppIcon.of(AppIcon.Type.PLUS, 13, Color.WHITE), tm.getSuccessGreen(), Color.WHITE);
        addBtn.setBounds(20, y, 95, 34);
        addBtn.addActionListener(e -> addPassenger());

        updateBtn = new ModernButton("Update", AppIcon.of(AppIcon.Type.CHECK, 13, Color.WHITE), tm.getPrimaryBlue(), Color.WHITE);
        updateBtn.setBounds(125, y, 95, 34);
        updateBtn.addActionListener(e -> updatePassenger());

        deleteBtn = new ModernButton("Delete", AppIcon.of(AppIcon.Type.TRASH, 13, Color.WHITE), tm.getDangerRed(), Color.WHITE);
        deleteBtn.setBounds(230, y, 100, 34);
        deleteBtn.addActionListener(e -> deletePassenger());

        clearBtn = new ModernButton("Clear Form", AppIcon.of(AppIcon.Type.REFRESH, 12, Color.WHITE), new Color(120, 130, 145), Color.WHITE);
        clearBtn.setBounds(20, y + 42, 310, 30);
        clearBtn.addActionListener(e -> clearForm());

        formPanel.add(addBtn);
        formPanel.add(updateBtn);
        formPanel.add(deleteBtn);
        formPanel.add(clearBtn);

        centerPanel.add(formPanel, BorderLayout.WEST);

        // Right Table Panel
        String[] columns = {"ID", "Name", "Age", "Gender", "Phone", "Disability?", "Disability ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        passengerTable = new JTable(tableModel);
        passengerTable.setRowHeight(28);
        passengerTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passengerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        passengerTable.getTableHeader().setBackground(tm.getTableHeaderBg());
        passengerTable.getTableHeader().setForeground(tm.getTableHeaderText());
        passengerTable.setBackground(tm.getTableBg());
        passengerTable.setForeground(tm.getTextPrimary());
        passengerTable.setGridColor(tm.getTableGrid());
        passengerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        passengerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && passengerTable.getSelectedRow() != -1) {
                int r = passengerTable.getSelectedRow();
                idField.setText(tableModel.getValueAt(r, 0).toString());
                nameField.setText(tableModel.getValueAt(r, 1).toString());
                ageField.setText(tableModel.getValueAt(r, 2).toString());
                genderCombo.setSelectedItem(tableModel.getValueAt(r, 3).toString());
                phoneField.setText(tableModel.getValueAt(r, 4).toString());
                boolean hasDis = "YES".equalsIgnoreCase(tableModel.getValueAt(r, 5).toString());
                disabilityCheckBox.setSelected(hasDis);
                disabilityIdField.setEnabled(hasDis);
                Object idObj = tableModel.getValueAt(r, 6);
                disabilityIdField.setText(idObj != null ? idObj.toString() : "");
            }
        });

        JScrollPane scrollPane = new JScrollPane(passengerTable);
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

        passengerTable.setBackground(tm.getTableBg());
        passengerTable.setForeground(tm.getTextPrimary());
        passengerTable.setGridColor(tm.getTableGrid());
        passengerTable.getTableHeader().setBackground(tm.getTableHeaderBg());
        passengerTable.getTableHeader().setForeground(tm.getTableHeaderText());

        idField.setBackground(tm.getInputBg());
        idField.setForeground(tm.getInputText());
        nameField.setBackground(tm.getInputBg());
        nameField.setForeground(tm.getInputText());
        nameField.setCaretColor(tm.getInputText());
        ageField.setBackground(tm.getInputBg());
        ageField.setForeground(tm.getInputText());
        ageField.setCaretColor(tm.getInputText());
        phoneField.setBackground(tm.getInputBg());
        phoneField.setForeground(tm.getInputText());
        phoneField.setCaretColor(tm.getInputText());
        disabilityIdField.setBackground(tm.getInputBg());
        disabilityIdField.setForeground(tm.getInputText());
        disabilityIdField.setCaretColor(tm.getInputText());

        genderCombo.setBackground(tm.getInputBg());
        genderCombo.setForeground(tm.getInputText());

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
        ageField.setText("");
        phoneField.setText("");
        genderCombo.setSelectedIndex(0);
        disabilityCheckBox.setSelected(false);
        disabilityIdField.setText("");
        disabilityIdField.setEnabled(false);
        passengerTable.clearSelection();
    }

    private void loadPassengers() {
        SwingWorker<List<Passenger>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Passenger> doInBackground() {
                return passengerDAO.getAllPassengers();
            }

            @Override
            protected void done() {
                try {
                    List<Passenger> list = get();
                    tableModel.setRowCount(0);
                    for (Passenger p : list) {
                        tableModel.addRow(new Object[]{
                                p.getPassengerId(),
                                p.getName(),
                                p.getAge(),
                                p.getGender(),
                                p.getPhoneNumber(),
                                p.isHasDisability() ? "YES" : "NO",
                                p.getDisabilityIdNo() != null ? p.getDisabilityIdNo() : "-"
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void addPassenger() {
        String name = nameField.getText().trim();
        String ageStr = ageField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String phone = phoneField.getText().trim();
        boolean hasDisability = disabilityCheckBox.isSelected();
        String disId = disabilityIdField.getText().trim();

        if (name.isEmpty() || ageStr.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required passenger fields.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (!Validator.isValidAge(age)) {
                JOptionPane.showMessageDialog(this, "Age must be between 1 and 120.", "Invalid Age", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric age.", "Invalid Age", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Validator.isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Phone number must be a valid 10-digit number.", "Invalid Phone", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (hasDisability && disId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Disability ID / Certificate number is mandatory when disability is checked.", "Missing Disability ID", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int newId = passengerDAO.getNextPassengerId();
        Passenger p = new Passenger(newId, null, name, age, gender, phone, hasDisability, hasDisability ? disId : null);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return passengerDAO.addPassenger(p);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(PassengerManagement.this, "Passenger registered successfully! (ID: " + newId + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadPassengers();
                    } else {
                        JOptionPane.showMessageDialog(PassengerManagement.this, "Failed to register passenger.", "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void updatePassenger() {
        String idStr = idField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a passenger from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idStr);
        String name = nameField.getText().trim();
        String ageStr = ageField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String phone = phoneField.getText().trim();
        boolean hasDisability = disabilityCheckBox.isSelected();
        String disId = disabilityIdField.getText().trim();

        if (hasDisability && disId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Disability ID is mandatory when disability is checked.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int age = Integer.parseInt(ageStr);
        Passenger p = new Passenger(id, null, name, age, gender, phone, hasDisability, hasDisability ? disId : null);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return passengerDAO.updatePassenger(p);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(PassengerManagement.this, "Passenger updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadPassengers();
                    } else {
                        JOptionPane.showMessageDialog(PassengerManagement.this, "Failed to update passenger.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void deletePassenger() {
        String idStr = idField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a passenger to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete passenger ID " + idStr + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int id = Integer.parseInt(idStr);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return passengerDAO.deletePassenger(id);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(PassengerManagement.this, "Passenger deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadPassengers();
                    } else {
                        JOptionPane.showMessageDialog(PassengerManagement.this, "Failed to delete passenger.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}