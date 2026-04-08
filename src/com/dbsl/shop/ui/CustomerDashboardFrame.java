package com.dbsl.shop.ui;

import com.dbsl.shop.model.UserSession;
import com.dbsl.shop.service.CustomerService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
    private final JTable cartTable = new JTable();
    private final JTable orderTable = new JTable();
    private final JTextField searchField = new JTextField(20);
    private final JTextField addressField = new JTextField(24);
    private final JComboBox<String> paymentMethodBox = new JComboBox<>(new String[]{"UPI", "CARD", "COD", "NETBANKING"});
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));

    public CustomerDashboardFrame(UserSession session) {
        this.session = session;
        setTitle("Customer Dashboard - " + session.getFullName());
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        header.add(Theme.titleLabel("Customer Dashboard"), BorderLayout.WEST);
        JLabel greeting = new JLabel("Welcome, " + session.getFullName());
        greeting.setFont(Theme.HEADER_FONT);
        greeting.setForeground(Theme.ACCENT_DARK);
        header.add(greeting, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Catalog", buildCatalogPanel());
        tabbedPane.add("Cart & Checkout", buildCartPanel());
        tabbedPane.add("Order History", buildHistoryPanel());
        add(tabbedPane, BorderLayout.CENTER);

        refreshAll();
    }

    private JPanel buildCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        Theme.styleField(searchField);
        JButton searchButton = Theme.secondaryButton("Search");
        JButton refreshButton = Theme.secondaryButton("Refresh");
        JButton addButton = Theme.primaryButton("Add Selected Product");

        searchButton.addActionListener(event -> loadCatalog());
        refreshButton.addActionListener(event -> refreshAll());
        addButton.addActionListener(event -> addSelectedProduct());

        actions.add(new JLabel("Search"));
        actions.add(searchField);
        actions.add(new JLabel("Qty"));
        actions.add(quantitySpinner);
        actions.add(searchButton);
        actions.add(refreshButton);
        actions.add(addButton);

        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(catalogTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel checkoutCard = new JPanel(new GridLayout(4, 2, 10, 10));
        Theme.styleCard(checkoutCard);
        Theme.styleField(addressField);
        checkoutCard.add(new JLabel("Shipping Address"));
        checkoutCard.add(addressField);
        checkoutCard.add(new JLabel("Payment Method"));
        checkoutCard.add(paymentMethodBox);
        checkoutCard.add(new JLabel("Action"));
        JButton placeOrderButton = Theme.primaryButton("Place Order");
        placeOrderButton.addActionListener(event -> placeOrder());
        checkoutCard.add(placeOrderButton);
        checkoutCard.add(new JLabel("Tip"));
        checkoutCard.add(new JLabel("Select from catalog, then checkout from here"));

        panel.add(top, BorderLayout.CENTER);
        panel.add(checkoutCard, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));
        panel.add(new JScrollPane(orderTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadCatalog() {
        try {
            catalogTable.setModel(customerService.loadCatalog(searchField.getText()));
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void addSelectedProduct() {
        int row = catalogTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product from the catalog table.");
            return;
        }

        int productId = Integer.parseInt(String.valueOf(catalogTable.getValueAt(row, 0)));
        int quantity = (Integer) quantitySpinner.getValue();
        try {
            customerService.addToCart(session.getUserId(), productId, quantity);
            refreshAll();
            JOptionPane.showMessageDialog(this, "Product added to cart.");
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void placeOrder() {
        if (addressField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter the shipping address before checkout.");
            return;
        }

        try {
            String result = customerService.placeOrder(session.getUserId(),
                    paymentMethodBox.getSelectedItem().toString(),
                    addressField.getText().trim());
            refreshAll();
            JOptionPane.showMessageDialog(this, result);
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void refreshAll() {
        loadCatalog();
        try {
            cartTable.setModel(customerService.loadCart(session.getUserId()));
            orderTable.setModel(customerService.loadOrderHistory(session.getUserId()));
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void showDatabaseError(SQLException exception) {
        JOptionPane.showMessageDialog(this,
                "Database operation failed.\n" + exception.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
