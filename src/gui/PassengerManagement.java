package gui;

import dao.PassengerDAO;
import model.Passenger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PassengerManagement extends JFrame {

    private JTextField idField, nameField, ageField, phoneField;
    private JComboBox<String> genderBox;
    private JTable passengerTable;
    private DefaultTableModel tableModel;
    private PassengerDAO passengerDAO;

    public PassengerManagement() {
        passengerDAO = new PassengerDAO();

        setTitle("Bus Reservation System - Passenger Management");
        setSize(900, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        JLabel title = new JLabel("Passenger Management");
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
        formPanel.setBorder(BorderFactory.createTitledBorder("Passenger Details"));

        JLabel idLabel = new JLabel("Passenger ID:");
        idLabel.setBounds(25, 40, 120, 25);
        idField = new JTextField();
        idField.setBounds(150, 40, 230, 28);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setBounds(25, 85, 120, 25);
        nameField = new JTextField();
        nameField.setBounds(150, 85, 230, 28);

        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setBounds(25, 130, 120, 25);
        ageField = new JTextField();
        ageField.setBounds(150, 130, 230, 28);

        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setBounds(25, 175, 120, 25);
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderBox.setBounds(150, 175, 230, 28);

        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneLabel.setBounds(25, 220, 120, 25);
        phoneField = new JTextField();
        phoneField.setBounds(150, 220, 230, 28);

        // Buttons
        JButton addButton = new JButton("Add");
        addButton.setBounds(25, 280, 80, 32);
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.BLACK);

        JButton updateButton = new JButton("Update");
        updateButton.setBounds(115, 280, 85, 32);
        updateButton.setBackground(new Color(52, 152, 219));
        updateButton.setForeground(Color.BLACK);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setBounds(210, 280, 80, 32);
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.BLACK);

        JButton clearButton = new JButton("Clear");
        clearButton.setBounds(300, 280, 80, 32);

        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(ageLabel);
        formPanel.add(ageField);
        formPanel.add(genderLabel);
        formPanel.add(genderBox);
        formPanel.add(phoneLabel);
        formPanel.add(phoneField);
        formPanel.add(addButton);
        formPanel.add(updateButton);
        formPanel.add(deleteButton);
        formPanel.add(clearButton);

        contentPanel.add(formPanel);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Registered Passengers"));

        String[] columns = {"ID", "Name", "Age", "Gender", "Phone"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        passengerTable = new JTable(tableModel);
        passengerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passengerTable.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(passengerTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh Table");
        tablePanel.add(refreshButton, BorderLayout.SOUTH);

        contentPanel.add(tablePanel);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Events
        addButton.addActionListener(e -> addPassenger());
        updateButton.addActionListener(e -> updatePassenger());
        deleteButton.addActionListener(e -> deletePassenger());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> loadPassengerTable());

        passengerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = passengerTable.getSelectedRow();
                if (selectedRow >= 0) {
                    idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    idField.setEditable(false);
                    nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    ageField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    genderBox.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
                    phoneField.setText(tableModel.getValueAt(selectedRow, 4).toString());
                }
            }
        });

        loadPassengerTable();
    }

    private void loadPassengerTable() {
        tableModel.setRowCount(0);
        List<Passenger> list = passengerDAO.getAllPassengers();
        for (Passenger p : list) {
            tableModel.addRow(new Object[]{
                    p.getPassengerId(),
                    p.getName(),
                    p.getAge(),
                    p.getGender(),
                    p.getPhoneNumber()
            });
        }
    }

    private void addPassenger() {
        try {
            if (idField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty() ||
                ageField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            String gender = genderBox.getSelectedItem().toString();
            String phone = phoneField.getText().trim();

            if (passengerDAO.getPassengerById(id) != null) {
                JOptionPane.showMessageDialog(this, "Passenger ID already exists! Please use a unique ID.", "Duplicate ID", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Passenger passenger = new Passenger(id, name, age, gender, phone);
            if (passengerDAO.addPassenger(passenger)) {
                JOptionPane.showMessageDialog(this, "Passenger added successfully!");
                clearFields();
                loadPassengerTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add passenger.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID and Age must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePassenger() {
        try {
            if (idField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a passenger from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            String gender = genderBox.getSelectedItem().toString();
            String phone = phoneField.getText().trim();

            Passenger passenger = new Passenger(id, name, age, gender, phone);
            if (passengerDAO.updatePassenger(passenger)) {
                JOptionPane.showMessageDialog(this, "Passenger updated successfully!");
                clearFields();
                loadPassengerTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update passenger.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePassenger() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a passenger from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this passenger?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(idField.getText().trim());
            if (passengerDAO.deletePassenger(id)) {
                JOptionPane.showMessageDialog(this, "Passenger deleted successfully!");
                clearFields();
                loadPassengerTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete passenger.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        idField.setText("");
        idField.setEditable(true);
        nameField.setText("");
        ageField.setText("");
        genderBox.setSelectedIndex(0);
        phoneField.setText("");
        passengerTable.clearSelection();
    }
}