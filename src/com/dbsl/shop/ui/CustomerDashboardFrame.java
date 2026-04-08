package com.dbsl.shop.ui;

import com.dbsl.shop.model.UserSession;
import com.dbsl.shop.service.CustomerService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class CustomerDashboardFrame extends JFrame {
    private final UserSession session;
    private final CustomerService customerService = new CustomerService();

    private final JTable catalogTable = new JTable();
    private final JTable cartTable    = new JTable();
    private final JTable orderTable   = new JTable();

    private final JTextField searchField      = new JTextField(20);
    private final JTextField addressField     = new JTextField(24);
    private final JComboBox<String> paymentMethodBox = new JComboBox<>(new String[]{"UPI", "CARD", "COD", "NETBANKING"});
    private final JComboBox<String> categoryBox      = new JComboBox<>();
    private final JSpinner quantitySpinner    = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));

    // Profile fields
    private final JTextField profFullNameField = new JTextField();
    private final JTextField profUsernameField = new JTextField();
    private final JTextField profEmailField    = new JTextField();
    private final JTextField profPhoneField    = new JTextField();
    private final JTextField profAddressField  = new JTextField();
    private final JTextField profCityField     = new JTextField();
    private final JTextField profPincodeField  = new JTextField();
    private final JPasswordField profPasswordField = new JPasswordField();

    public CustomerDashboardFrame(UserSession session) {
        this.session = session;
        setTitle("Customer Dashboard - " + session.getFullName());
        setSize(1800, 1200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        header.add(Theme.titleLabel("Customer Dashboard"), BorderLayout.WEST);
        JLabel greeting = new JLabel("Welcome, " + session.getFullName());
        greeting.setFont(Theme.HEADER_FONT);
        greeting.setForeground(Theme.ACCENT_DARK);
        header.add(greeting, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        loadCategoryDropdown();

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.BODY_FONT);
        tabs.add("Catalog",          buildCatalogPanel());
        tabs.add("Cart & Checkout",  buildCartPanel());
        tabs.add("Order History",    buildHistoryPanel());
        tabs.add("My Profile",       buildProfilePanel());
        add(tabs, BorderLayout.CENTER);

        refreshAll();
        loadProfileFields();
    }

    private void loadCategoryDropdown() {
        try {
            List<String> categories = customerService.loadCategories();
            categoryBox.removeAllItems();
            for (String cat : categories) categoryBox.addItem(cat);
        } catch (SQLException e) {
            categoryBox.addItem("All Categories");
        }
    }

    // ── Catalog ───────────────────────────────────────────
    private JPanel buildCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        actions.setBackground(Theme.BACKGROUND);
        Theme.styleField(searchField);

        JLabel searchLabel   = new JLabel("Search");
        JLabel categoryLabel = new JLabel("Category");
        JLabel qtyLabel      = new JLabel("Qty");
        searchLabel  .setFont(Theme.BODY_FONT); searchLabel  .setForeground(Theme.TEXT);
        categoryLabel.setFont(Theme.BODY_FONT); categoryLabel.setForeground(Theme.TEXT);
        qtyLabel     .setFont(Theme.BODY_FONT); qtyLabel     .setForeground(Theme.TEXT);

        categoryBox.setFont(Theme.BODY_FONT);
        categoryBox.setBackground(Theme.CARD);
        categoryBox.setForeground(Theme.TEXT);

        JButton searchButton  = Theme.secondaryButton("Search");
        JButton refreshButton = Theme.secondaryButton("Refresh");
        JButton addButton     = Theme.primaryButton("Add Selected to Cart");
        searchButton .addActionListener(e -> loadCatalog());
        refreshButton.addActionListener(e -> refreshAll());
        addButton    .addActionListener(e -> addSelectedProduct());
        categoryBox  .addActionListener(e -> loadCatalog());

        actions.add(searchLabel);
        actions.add(searchField);
        actions.add(categoryLabel);
        actions.add(categoryBox);
        actions.add(qtyLabel);
        actions.add(quantitySpinner);
        actions.add(searchButton);
        actions.add(refreshButton);
        actions.add(addButton);

        catalogTable.setRowHeight(36);
        catalogTable.setFont(Theme.BODY_FONT);

        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(catalogTable), BorderLayout.CENTER);
        return panel;
    }

    // ── Cart ──────────────────────────────────────────────
    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));

        cartTable.setRowHeight(36);
        cartTable.setFont(Theme.BODY_FONT);

        JPanel checkoutCard = new JPanel(new GridBagLayout());
        Theme.styleCard(checkoutCard);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 12, 10, 12);
        c.fill   = GridBagConstraints.HORIZONTAL;

        Theme.styleField(addressField);

        String[] lbls = {"Shipping Address", "Payment Method", "Action", "Tip"};
        for (int i = 0; i < lbls.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0.3;
            JLabel lbl = new JLabel(lbls[i]);
            lbl.setFont(Theme.BODY_FONT);
            lbl.setForeground(Theme.TEXT);
            checkoutCard.add(lbl, c);
        }
        c.gridx = 1; c.gridy = 0; c.weightx = 0.7;
        checkoutCard.add(addressField, c);
        c.gridy = 1;
        checkoutCard.add(paymentMethodBox, c);
        c.gridy = 2;
        JButton placeOrderButton = Theme.primaryButton("Place Order");
        placeOrderButton.addActionListener(e -> placeOrder());
        checkoutCard.add(placeOrderButton, c);
        c.gridy = 3;
        JLabel tip = new JLabel("Select from catalog tab, then checkout here");
        tip.setFont(Theme.BODY_FONT);
        tip.setForeground(Theme.ACCENT);
        checkoutCard.add(tip, c);

        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        panel.add(checkoutCard,               BorderLayout.SOUTH);
        return panel;
    }

    // ── Order History ─────────────────────────────────────
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));
        orderTable.setRowHeight(36);
        orderTable.setFont(Theme.BODY_FONT);
        panel.add(new JScrollPane(orderTable), BorderLayout.CENTER);
        return panel;
    }

    // ── Profile ───────────────────────────────────────────
    private JPanel buildProfilePanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.BACKGROUND);

        JPanel card = new JPanel(new GridBagLayout());
        Theme.styleCard(card);
        card.setPreferredSize(new java.awt.Dimension(600, 580));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 14, 10, 14);
        c.fill   = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        JLabel heading = new JLabel("My Profile");
        heading.setFont(Theme.HEADER_FONT);
        heading.setForeground(Theme.ACCENT_DARK);
        card.add(heading, c);

        Theme.styleField(profFullNameField);
        Theme.styleField(profUsernameField);
        Theme.styleField(profEmailField);
        Theme.styleField(profPhoneField);
        Theme.styleField(profAddressField);
        Theme.styleField(profCityField);
        Theme.styleField(profPincodeField);
        profPasswordField.setFont(Theme.BODY_FONT);
        profPasswordField.setBackground(java.awt.Color.WHITE);
        profPasswordField.setForeground(Theme.TEXT);

        String[] labels = {"Full Name", "Username", "Email", "Phone",
                           "Address", "City", "Pincode", "New Password (blank to keep)"};
        JTextField[] fields = {profFullNameField, profUsernameField, profEmailField,
                               profPhoneField, profAddressField, profCityField, profPincodeField, null};

        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0; c.gridy = i + 1; c.gridwidth = 1; c.weightx = 0.35;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(Theme.BODY_FONT);
            lbl.setForeground(Theme.TEXT);
            card.add(lbl, c);
            c.gridx = 1; c.weightx = 0.65;
            if (i == labels.length - 1) card.add(profPasswordField, c);
            else                         card.add(fields[i], c);
        }

        c.gridx = 0; c.gridy = labels.length + 1; c.gridwidth = 2; c.weightx = 1.0;
        JButton saveButton = Theme.primaryButton("Save Changes");
        saveButton.addActionListener(e -> saveCustomerProfile());
        card.add(saveButton, c);

        c.gridy = labels.length + 2;
        wrapper.add(card);
        return wrapper;
    }

    private void loadProfileFields() {
        try {
            Map<String, String> p = customerService.loadCustomerProfile(session.getUserId());
            profFullNameField.setText(p.getOrDefault("full_name",       ""));
            profUsernameField.setText(p.getOrDefault("username",        ""));
            profEmailField   .setText(p.getOrDefault("email",           ""));
            profPhoneField   .setText(p.getOrDefault("phone",           ""));
            profAddressField .setText(p.getOrDefault("default_address", ""));
            profCityField    .setText(p.getOrDefault("city",            ""));
            profPincodeField .setText(p.getOrDefault("pincode",         ""));
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void saveCustomerProfile() {
        String username = profUsernameField.getText().trim();
        String email    = profEmailField.getText().trim();
        String phone    = profPhoneField.getText().trim();
        String fullName = profFullNameField.getText().trim();
        String address  = profAddressField.getText().trim();
        String city     = profCityField.getText().trim();
        String pincode  = profPincodeField.getText().trim();
        String password = new String(profPasswordField.getPassword()).trim();

        if (username.isBlank() || email.isBlank() || fullName.isBlank()) {
            JOptionPane.showMessageDialog(this, "Full name, username and email are required.");
            return;
        }
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.");
            return;
        }
        if (!pincode.isBlank() && !pincode.matches("^[0-9]{6}$")) {
            JOptionPane.showMessageDialog(this, "Pincode must be 6 digits.");
            return;
        }
        try {
            customerService.updateCustomerProfile(
                    session.getUserId(), username, email,
                    phone.isBlank()   ? null : phone,
                    fullName,
                    password.isBlank() ? null : password,
                    address, city, pincode);
            JOptionPane.showMessageDialog(this, "Profile updated successfully.");
            profPasswordField.setText("");
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    // ── Helpers ───────────────────────────────────────────
    private void loadCatalog() {
        try {
            String cat = (String) categoryBox.getSelectedItem();
            catalogTable.setModel(customerService.loadCatalog(searchField.getText(), cat));
        } catch (SQLException e) { showDatabaseError(e); }
    }

    private void addSelectedProduct() {
        int row = catalogTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product from the catalog first.");
            return;
        }
        int productId = Integer.parseInt(String.valueOf(catalogTable.getValueAt(row, 0)));
        int quantity  = (Integer) quantitySpinner.getValue();
        try {
            customerService.addToCart(session.getUserId(), productId, quantity);
            refreshAll();
            JOptionPane.showMessageDialog(this, "Product added to cart.");
        } catch (SQLException e) { showDatabaseError(e); }
    }

    private void placeOrder() {
        if (addressField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter the shipping address before checkout.");
            return;
        }
        try {
            String result = customerService.placeOrder(
                    session.getUserId(),
                    paymentMethodBox.getSelectedItem().toString(),
                    addressField.getText().trim());
            refreshAll();
            JOptionPane.showMessageDialog(this, result);
        } catch (SQLException e) { showDatabaseError(e); }
    }

    private void refreshAll() {
        loadCatalog();
        try {
            cartTable .setModel(customerService.loadCart(session.getUserId()));
            orderTable.setModel(customerService.loadOrderHistory(session.getUserId()));
        } catch (SQLException e) { showDatabaseError(e); }
    }

    private void showDatabaseError(SQLException e) {
        JOptionPane.showMessageDialog(this,
                "Database operation failed.\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
