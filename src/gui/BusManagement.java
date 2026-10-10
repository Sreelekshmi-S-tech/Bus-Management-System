package gui;

import dao.BusDAO;
import model.Bus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class BusManagement extends JFrame {

    private JTextField idField, nameField, sourceField, destinationField, seatsField, fareField;
    private JTable busTable;
    private DefaultTableModel tableModel;
    private BusDAO busDAO;

    public BusManagement() {
        busDAO = new BusDAO();

        setTitle("Bus Reservation System - Bus Management");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        JLabel title = new JLabel("Bus Management");
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
        formPanel.setBorder(BorderFactory.createTitledBorder("Bus Details"));

        JLabel idLabel = new JLabel("Bus ID:");
        idLabel.setBounds(25, 35, 120, 25);
        idField = new JTextField();
        idField.setBounds(150, 35, 230, 28);

        JLabel nameLabel = new JLabel("Bus Name:");
        nameLabel.setBounds(25, 75, 120, 25);
        nameField = new JTextField();
        nameField.setBounds(150, 75, 230, 28);

        JLabel sourceLabel = new JLabel("Source:");
        sourceLabel.setBounds(25, 115, 120, 25);
        sourceField = new JTextField();
        sourceField.setBounds(150, 115, 230, 28);

        JLabel destinationLabel = new JLabel("Destination:");
        destinationLabel.setBounds(25, 155, 120, 25);
        destinationField = new JTextField();
        destinationField.setBounds(150, 155, 230, 28);

        JLabel seatsLabel = new JLabel("Total Seats:");
        seatsLabel.setBounds(25, 195, 120, 25);
        seatsField = new JTextField("40");
        seatsField.setBounds(150, 195, 230, 28);

        JLabel fareLabel = new JLabel("Fare ($):");
        fareLabel.setBounds(25, 235, 120, 25);
        fareField = new JTextField("50.0");
        fareField.setBounds(150, 235, 230, 28);

        // Buttons
        JButton addButton = new JButton("Add");
        addButton.setBounds(25, 290, 80, 32);
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.BLACK);

        JButton updateButton = new JButton("Update");
        updateButton.setBounds(115, 290, 85, 32);
        updateButton.setBackground(new Color(52, 152, 219));
        updateButton.setForeground(Color.BLACK);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setBounds(210, 290, 80, 32);
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.BLACK);

        JButton clearButton = new JButton("Clear");
        clearButton.setBounds(300, 290, 80, 32);

        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(sourceLabel);
        formPanel.add(sourceField);
        formPanel.add(destinationLabel);
        formPanel.add(destinationField);
        formPanel.add(seatsLabel);
        formPanel.add(seatsField);
        formPanel.add(fareLabel);
        formPanel.add(fareField);

        formPanel.add(addButton);
        formPanel.add(updateButton);
        formPanel.add(deleteButton);
        formPanel.add(clearButton);

        contentPanel.add(formPanel);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Registered Buses"));

        String[] columns = {"ID", "Name", "Source", "Destination", "Seats", "Fare"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        busTable = new JTable(tableModel);
        busTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        busTable.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(busTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh Table");
        tablePanel.add(refreshButton, BorderLayout.SOUTH);

        contentPanel.add(tablePanel);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Events
        addButton.addActionListener(e -> addBus());
        updateButton.addActionListener(e -> updateBus());
        deleteButton.addActionListener(e -> deleteBus());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> loadBusTable());

        busTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = busTable.getSelectedRow();
                if (selectedRow >= 0) {
                    idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    idField.setEditable(false);
                    nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    sourceField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    destinationField.setText(tableModel.getValueAt(selectedRow, 3).toString());
                    seatsField.setText(tableModel.getValueAt(selectedRow, 4).toString());
                    fareField.setText(tableModel.getValueAt(selectedRow, 5).toString());
                }
            }
        });

        loadBusTable();
    }

    private void loadBusTable() {
        tableModel.setRowCount(0);
        List<Bus> list = busDAO.getAllBuses();
        for (Bus b : list) {
            tableModel.addRow(new Object[]{
                    b.getBusId(),
                    b.getBusName(),
                    b.getSource(),
                    b.getDestination(),
                    b.getTotalSeats(),
                    b.getFare()
            });
        }
    }

    private void addBus() {
        try {
            if (idField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty() ||
                sourceField.getText().trim().isEmpty() || destinationField.getText().trim().isEmpty() ||
                seatsField.getText().trim().isEmpty() || fareField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            String source = sourceField.getText().trim();
            String dest = destinationField.getText().trim();
            int seats = Integer.parseInt(seatsField.getText().trim());
            double fare = Double.parseDouble(fareField.getText().trim());

            if (busDAO.getBusById(id) != null) {
                JOptionPane.showMessageDialog(this, "Bus ID already exists! Please use a unique ID.", "Duplicate ID", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Bus bus = new Bus(id, name, source, dest, seats, fare);
            if (busDAO.addBus(bus)) {
                JOptionPane.showMessageDialog(this, "Bus added successfully!");
                clearFields();
                loadBusTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add bus.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID, Seats, and Fare must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBus() {
        try {
            if (idField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a bus from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            String source = sourceField.getText().trim();
            String dest = destinationField.getText().trim();
            int seats = Integer.parseInt(seatsField.getText().trim());
            double fare = Double.parseDouble(fareField.getText().trim());

            Bus bus = new Bus(id, name, source, dest, seats, fare);
            if (busDAO.updateBus(bus)) {
                JOptionPane.showMessageDialog(this, "Bus updated successfully!");
                clearFields();
                loadBusTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update bus.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Seats and Fare must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBus() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a bus from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this bus?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(idField.getText().trim());
            if (busDAO.deleteBus(id)) {
                JOptionPane.showMessageDialog(this, "Bus deleted successfully!");
                clearFields();
                loadBusTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete bus.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        idField.setText("");
        idField.setEditable(true);
        nameField.setText("");
        sourceField.setText("");
        destinationField.setText("");
        seatsField.setText("40");
        fareField.setText("50.0");
        busTable.clearSelection();
    }
}