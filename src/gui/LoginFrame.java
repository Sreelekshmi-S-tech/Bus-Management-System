package gui;

import service.AuthService;
import util.AppIcon;
import util.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private ModernButton loginButton;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("Bus Management & Reservation System - Login");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        ThemeManager tm = ThemeManager.getInstance();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(tm.getBgColor());

        // Header Strip with Vector Bus Logo & Branding
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 500, 100);
        headerPanel.setBackground(tm.getHeaderBg());

        // Vector Bus Logo (Never missing glyph)
        JLabel iconLabel = new JLabel(AppIcon.of(AppIcon.Type.BUS, 44, Color.WHITE));
        iconLabel.setBounds(30, 25, 55, 50);
        headerPanel.add(iconLabel);

        JLabel title = new JLabel("Bus Reservation System");
        title.setBounds(95, 25, 360, 30);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        headerPanel.add(title);

        JLabel subtitle = new JLabel("Sign in to book seats, manage fleets & print tickets");
        subtitle.setBounds(95, 58, 360, 20);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(tm.getHeaderSubtext());
        headerPanel.add(subtitle);

        mainPanel.add(headerPanel);

        // Form Fields
        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(70, 125, 100, 25);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(tm.getTextPrimary());

        usernameField = new JTextField("admin");
        usernameField.setBounds(70, 153, 360, 38);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBackground(tm.getInputBg());
        usernameField.setForeground(tm.getInputText());
        usernameField.setCaretColor(tm.getInputText());
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getInputBorder(), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(70, 202, 100, 25);
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLabel.setForeground(tm.getTextPrimary());

        passwordField = new JPasswordField("1234");
        passwordField.setBounds(70, 230, 360, 38);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBackground(tm.getInputBg());
        passwordField.setForeground(tm.getInputText());
        passwordField.setCaretColor(tm.getInputText());
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getInputBorder(), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        // Status Label for Feedback
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setBounds(70, 274, 360, 24);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(tm.getDangerRed());

        // Modern Sign In Button (Fixes Windows white-on-white text bug)
        loginButton = new ModernButton(
                "Sign In",
                AppIcon.of(AppIcon.Type.CHECK, 16, Color.WHITE),
                tm.getPrimaryBlue(),
                Color.WHITE
        );
        loginButton.setBounds(70, 305, 360, 44);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginButton.setCornerRadius(8);
        loginButton.addActionListener(e -> performLogin());

        // Enter key listeners
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> performLogin());

        // Create Account Link
        JLabel signupLink = new JLabel("<html>Don't have an account? <span style='color:#38bdf8;'><b>Create Account</b></span></html>", SwingConstants.CENTER);
        signupLink.setBounds(70, 365, 360, 25);
        signupLink.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        signupLink.setForeground(tm.getTextPrimary());
        signupLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new SignupFrame().setVisible(true);
                dispose();
            }
        });

        // Default credentials hint
        JLabel hintLabel = new JLabel("Default Admin: admin / 1234  |  User: john_doe / User@1234", SwingConstants.CENTER);
        hintLabel.setBounds(20, 405, 460, 20);
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(tm.getTextSecondary());

        mainPanel.add(userLabel);
        mainPanel.add(usernameField);
        mainPanel.add(passLabel);
        mainPanel.add(passwordField);
        mainPanel.add(statusLabel);
        mainPanel.add(loginButton);
        mainPanel.add(signupLink);
        mainPanel.add(hintLabel);

        add(mainPanel);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        ThemeManager tm = ThemeManager.getInstance();
        statusLabel.setText("Verifying credentials...");
        statusLabel.setForeground(tm.getPrimaryBlue());
        loginButton.setEnabled(false);

        SwingWorker<AuthService.AuthResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AuthService.AuthResult doInBackground() {
                return AuthService.getInstance().login(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    AuthService.AuthResult result = get();
                    if (result.success) {
                        statusLabel.setText("");
                        new Dashboard().setVisible(true);
                        dispose();
                    } else {
                        statusLabel.setText(result.message);
                        statusLabel.setForeground(ThemeManager.getInstance().getDangerRed());
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                result.message,
                                "Authentication Notice",
                                result.lockSecondsRemaining > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Login failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        };

        worker.execute();
    }
}