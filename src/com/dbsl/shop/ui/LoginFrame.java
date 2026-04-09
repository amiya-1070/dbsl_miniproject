package com.dbsl.shop.ui;

import com.dbsl.shop.model.UserSession;
import com.dbsl.shop.service.AuthService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("Online Shopping Portal");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 10, 28));
        header.add(Theme.titleLabel("Online Shopping Portal"), BorderLayout.WEST);

        JLabel subtitle = new JLabel("Secure Oracle DB mini project with separate customer and admin access");
        subtitle.setForeground(Theme.ACCENT_DARK);
        subtitle.setFont(Theme.BODY_FONT);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
        switchPanel.setBackground(Theme.BACKGROUND);
        JButton customerButton = Theme.primaryButton("Customer Login");
        JButton adminButton    = Theme.primaryButton("Admin Login");
        JButton signupButton   = Theme.primaryButton("Sign Up");
        customerButton.addActionListener(e -> cardLayout.show(cardPanel, "CUSTOMER"));
        adminButton   .addActionListener(e -> cardLayout.show(cardPanel, "ADMIN"));
        signupButton  .addActionListener(e -> cardLayout.show(cardPanel, "SIGNUP"));
        switchPanel.add(customerButton);
        switchPanel.add(adminButton);
        switchPanel.add(signupButton);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BACKGROUND);
        center.setBorder(BorderFactory.createEmptyBorder(8, 28, 28, 28));
        center.add(switchPanel, BorderLayout.NORTH);

        cardPanel.setBackground(Theme.BACKGROUND);
        cardPanel.add(buildLoginCard("CUSTOMER"), "CUSTOMER");
        cardPanel.add(buildLoginCard("ADMIN"),    "ADMIN");
        cardPanel.add(buildSignupCard(),           "SIGNUP");
        center.add(cardPanel, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
        cardLayout.show(cardPanel, "CUSTOMER");
    }

    private JPanel buildLoginCard(String role) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.BACKGROUND);
        JPanel card = new JPanel(new GridBagLayout());
        Theme.styleCard(card);
        card.setPreferredSize(new java.awt.Dimension(420, 260));

        GridBagConstraints c = new GridBagConstraints();
        c.insets    = new Insets(10, 10, 10, 10);
        c.fill      = GridBagConstraints.HORIZONTAL;
        c.gridx     = 0;
        c.gridy     = 0;
        c.gridwidth = 2;

        JLabel heading = new JLabel(role.equals("CUSTOMER") ? "Customer Login" : "Admin Login");
        heading.setFont(Theme.HEADER_FONT);
        heading.setForeground(Theme.ACCENT_DARK);
        card.add(heading, c);

        c.gridy++; c.gridwidth = 1;
        JLabel userLabel = new JLabel("Username");
        userLabel.setForeground(Theme.TEXT);
        userLabel.setFont(Theme.BODY_FONT);
        card.add(userLabel, c);

        JTextField usernameField = new JTextField();
        Theme.styleField(usernameField);
        c.gridx = 1;
        card.add(usernameField, c);

        c.gridx = 0; c.gridy++;
        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(Theme.TEXT);
        passLabel.setFont(Theme.BODY_FONT);
        card.add(passLabel, c);

        JPasswordField passwordField = new JPasswordField();
        Theme.styleField(passwordField);
        c.gridx = 1;
        card.add(passwordField, c);

        c.gridx = 0; c.gridy++; c.gridwidth = 2;
        JButton loginButton = Theme.primaryButton("Login as " + role);
        loginButton.addActionListener(e -> attemptLogin(role, usernameField.getText(), new String(passwordField.getPassword())));
        card.add(loginButton, c);

        wrapper.add(card);
        return wrapper;
    }

    private JPanel buildSignupCard() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.BACKGROUND);
        JPanel card = new JPanel(new GridBagLayout());
        Theme.styleCard(card);
        card.setPreferredSize(new java.awt.Dimension(460, 360));

        GridBagConstraints c = new GridBagConstraints();
        c.insets    = new Insets(8, 10, 8, 10);
        c.fill      = GridBagConstraints.HORIZONTAL;
        c.gridx     = 0;
        c.gridy     = 0;
        c.gridwidth = 2;

        JLabel heading = new JLabel("Create Account");
        heading.setFont(Theme.HEADER_FONT);
        heading.setForeground(Theme.ACCENT_DARK);
        card.add(heading, c);

        // Name
        c.gridy++; c.gridwidth = 1;
        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setForeground(Theme.TEXT);
        nameLabel.setFont(Theme.BODY_FONT);
        card.add(nameLabel, c);
        JTextField nameField = new JTextField();
        Theme.styleField(nameField);
        c.gridx = 1; card.add(nameField, c);

        // Username
        c.gridx = 0; c.gridy++;
        JLabel userLabel = new JLabel("Username");
        userLabel.setForeground(Theme.TEXT);
        userLabel.setFont(Theme.BODY_FONT);
        card.add(userLabel, c);
        JTextField usernameField = new JTextField();
        Theme.styleField(usernameField);
        c.gridx = 1; card.add(usernameField, c);

        // Email
        c.gridx = 0; c.gridy++;
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setForeground(Theme.TEXT);
        emailLabel.setFont(Theme.BODY_FONT);
        card.add(emailLabel, c);
        JTextField emailField = new JTextField();
        Theme.styleField(emailField);
        c.gridx = 1; card.add(emailField, c);

        // Phone
        c.gridx = 0; c.gridy++;
        JLabel phoneLabel = new JLabel("Phone Number");
        phoneLabel.setForeground(Theme.TEXT);
        phoneLabel.setFont(Theme.BODY_FONT);
        card.add(phoneLabel, c);
        JTextField phoneField = new JTextField();
        Theme.styleField(phoneField);
        c.gridx = 1; card.add(phoneField, c);

        // Password
        c.gridx = 0; c.gridy++;
        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(Theme.TEXT);
        passLabel.setFont(Theme.BODY_FONT);
        card.add(passLabel, c);
        JPasswordField passwordField = new JPasswordField();
        Theme.styleField(passwordField);
        c.gridx = 1; card.add(passwordField, c);

        // Sign up button
        c.gridx = 0; c.gridy++; c.gridwidth = 2;
        JButton signupButton = Theme.primaryButton("Create Account");
        signupButton.addActionListener(e -> attemptSignup(
                nameField.getText().trim(),
                usernameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                new String(passwordField.getPassword())
        ));
        card.add(signupButton, c);

        wrapper.add(card);
        return wrapper;
    }

    private void attemptSignup(String name, String username, String email, String phone, String password) {
        // Basic validation
        if (name.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Name, username, email and password are required.");
            return;
        }
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.");
            return;
        }
        if (!phone.isBlank() && !phone.matches("^[0-9]{10}$")) {
            JOptionPane.showMessageDialog(this, "Phone number must be 10 digits.");
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.");
            return;
        }

        try {
            authService.register(username, password, name, email, phone.isBlank() ? null : phone);
            JOptionPane.showMessageDialog(this, "Account created! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(cardPanel, "CUSTOMER");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Sign-up failed.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attemptLogin(String role, String username, String password) {
        if (username.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Username and password are required.");
            return;
        }
        try {
            UserSession session = authService.login(username, password, role);
            dispose();
            if ("CUSTOMER".equals(session.getRole())) {
                new CustomerDashboardFrame(session).setVisible(true);
            } else {
                new AdminDashboardFrame(session).setVisible(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Login failed.\n" + ex.getMessage(),
                    "Database Response",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
