package gui;

import service.AuthService;
import util.AppIcon;
import util.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SignupFrame extends JFrame {

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleCombo;
    private ModernButton signupButton;
    private JLabel statusLabel;

    public SignupFrame() {
        setTitle("Bus Management & Reservation System - Registration");
        setSize(520, 560);
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

        // Header Banner
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBounds(0, 0, 520, 85);
        headerPanel.setBackground(tm.getHeaderBg());

        // Vector Bus Icon
        JLabel iconLabel = new JLabel(AppIcon.of(AppIcon.Type.BUS, 38, Color.WHITE));
        iconLabel.setBounds(25, 20, 50, 45);
        headerPanel.add(iconLabel);

        JLabel title = new JLabel("Join Bus Reservation System");
        title.setBounds(85, 18, 380, 28);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        headerPanel.add(title);

        JLabel subtitle = new JLabel("Create your account to book seats and manage travel");
        subtitle.setBounds(85, 48, 380, 18);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(tm.getHeaderSubtext());
        headerPanel.add(subtitle);

        mainPanel.add(headerPanel);

        // Form Fields
        int startY = 105;
        int gap = 58;

        // Username
        JLabel userLbl = new JLabel("Username (3-20 chars):");
        userLbl.setBounds(50, startY, 200, 20);
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLbl.setForeground(tm.getTextPrimary());
        usernameField = new JTextField();
        usernameField.setBounds(50, startY + 22, 410, 34);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameField.setBackground(tm.getInputBg());
        usernameField.setForeground(tm.getInputText());
        usernameField.setCaretColor(tm.getInputText());
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getInputBorder(), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        mainPanel.add(userLbl);
        mainPanel.add(usernameField);

        // Email
        JLabel emailLbl = new JLabel("Email Address:");
        emailLbl.setBounds(50, startY + gap, 200, 20);
        emailLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        emailLbl.setForeground(tm.getTextPrimary());
        emailField = new JTextField();
        emailField.setBounds(50, startY + gap + 22, 410, 34);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailField.setBackground(tm.getInputBg());
        emailField.setForeground(tm.getInputText());
        emailField.setCaretColor(tm.getInputText());
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getInputBorder(), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        mainPanel.add(emailLbl);
        mainPanel.add(emailField);

        // Password
        JLabel passLbl = new JLabel("Password (min 8 chars, uppercase, lowercase & number):");
        passLbl.setBounds(50, startY + (gap * 2), 410, 20);
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLbl.setForeground(tm.getTextPrimary());
        passwordField = new JPasswordField();
        passwordField.setBounds(50, startY + (gap * 2) + 22, 410, 34);
        passwordField.setBackground(tm.getInputBg());
        passwordField.setForeground(tm.getInputText());
        passwordField.setCaretColor(tm.getInputText());
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getInputBorder(), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        mainPanel.add(passLbl);
        mainPanel.add(passwordField);

        // Confirm Password
        JLabel confirmLbl = new JLabel("Confirm Password:");
        confirmLbl.setBounds(50, startY + (gap * 3), 200, 20);
        confirmLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        confirmLbl.setForeground(tm.getTextPrimary());
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(50, startY + (gap * 3) + 22, 230, 34);
        confirmPasswordField.setBackground(tm.getInputBg());
        confirmPasswordField.setForeground(tm.getInputText());
        confirmPasswordField.setCaretColor(tm.getInputText());
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getInputBorder(), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        mainPanel.add(confirmLbl);
        mainPanel.add(confirmPasswordField);

        // Role Selector (User / Admin)
        JLabel roleLbl = new JLabel("Account Role:");
        roleLbl.setBounds(300, startY + (gap * 3), 160, 20);
        roleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleLbl.setForeground(tm.getTextPrimary());
        roleCombo = new JComboBox<>(new String[]{"USER", "ADMIN"});
        roleCombo.setBounds(300, startY + (gap * 3) + 22, 160, 34);
        roleCombo.setBackground(tm.getInputBg());
        roleCombo.setForeground(tm.getInputText());
        mainPanel.add(roleLbl);
        mainPanel.add(roleCombo);

        // Status Feedback Label
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setBounds(50, startY + (gap * 4) + 8, 410, 22);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(tm.getDangerRed());
        mainPanel.add(statusLabel);

        // Signup Button (ModernButton)
        signupButton = new ModernButton(
                "Create Account",
                AppIcon.of(AppIcon.Type.PLUS, 15, Color.WHITE),
                tm.getSuccessGreen(),
                Color.WHITE
        );
        signupButton.setBounds(50, startY + (gap * 4) + 34, 410, 42);
        signupButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signupButton.setCornerRadius(8);
        signupButton.addActionListener(e -> performSignup());
        mainPanel.add(signupButton);

        // Back to Login Link
        JLabel backToLogin = new JLabel("<html>Already have an account? <span style='color:#38bdf8;'><b>Log in here</b></span></html>", SwingConstants.CENTER);
        backToLogin.setBounds(50, startY + (gap * 4) + 84, 410, 25);
        backToLogin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backToLogin.setForeground(tm.getTextPrimary());
        backToLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        mainPanel.add(backToLogin);

        add(mainPanel);
    }

    private void performSignup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        char[] password = passwordField.getPassword();
        char[] confirm = confirmPasswordField.getPassword();
        String role = (String) roleCombo.getSelectedItem();

        ThemeManager tm = ThemeManager.getInstance();
        statusLabel.setText("Creating account...");
        statusLabel.setForeground(tm.getPrimaryBlue());
        signupButton.setEnabled(false);

        SwingWorker<AuthService.AuthResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AuthService.AuthResult doInBackground() {
                return AuthService.getInstance().register(username, email, password, confirm, role);
            }

            @Override
            protected void done() {
                signupButton.setEnabled(true);
                try {
                    AuthService.AuthResult result = get();
                    if (result.success) {
                        JOptionPane.showMessageDialog(SignupFrame.this,
                                result.message,
                                "Account Created", JOptionPane.INFORMATION_MESSAGE);
                        new LoginFrame().setVisible(true);
                        dispose();
                    } else {
                        statusLabel.setText(result.message);
                        statusLabel.setForeground(ThemeManager.getInstance().getDangerRed());
                        JOptionPane.showMessageDialog(SignupFrame.this,
                                result.message,
                                "Registration Error", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Unexpected error occurred.");
                    ex.printStackTrace();
                }
            }
        };

        worker.execute();
    }
}
