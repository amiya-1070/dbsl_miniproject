package com.dbsl.shop.ui;

import com.dbsl.shop.model.UserSession;
import com.dbsl.shop.service.CustomerService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
    private final JTable addrTable 	  = new JTable();

    private final JTextField  searchField       = new JTextField(20);
    private final JComboBox<String> paymentMethodBox = new JComboBox<>(new String[]{"UPI","CARD","COD","NETBANKING"});
    private final JComboBox<String> categoryBox      = new JComboBox<>();
    private final JComboBox<String> addressChooser   = new JComboBox<>();
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1,1,20,1));

    // Profile fields
    private final JTextField  profFullNameField = new JTextField();
    private final JTextField  profUsernameField = new JTextField();
    private final JTextField  profEmailField    = new JTextField();
    private final JTextField  profPhoneField    = new JTextField();
    private final JPasswordField profPasswordField = new JPasswordField();

    // Header greeting label — updated when name changes
    private final JLabel greetingLabel = new JLabel();

    // Address list cache
    private List<Map<String, String>> addressList = new java.util.ArrayList<>();

    public CustomerDashboardFrame(UserSession session) {
        this.session = session;
        setTitle("Customer Dashboard - " + session.getFullName());
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        header.add(Theme.titleLabel("Customer Dashboard"), BorderLayout.WEST);
        greetingLabel.setText("Welcome, " + session.getFullName());
        greetingLabel.setFont(Theme.HEADER_FONT);
        greetingLabel.setForeground(Theme.ACCENT_DARK);
        header.add(greetingLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        loadCategoryDropdown();
        refreshAddressChooser();

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.BODY_FONT);
        tabs.add("Catalog",         buildCatalogPanel());
        tabs.add("Cart & Checkout", buildCartPanel());
        tabs.add("Order History",   buildHistoryPanel());
        tabs.add("My Profile",      buildProfilePanel());
        add(tabs, BorderLayout.CENTER);

        refreshAll();
        loadProfileFields();
    }

    // ── Category dropdown ─────────────────────────────────
    private void loadCategoryDropdown() {
        try {
            List<String> categories = customerService.loadCategories();
            categoryBox.removeAllItems();
            for (String c : categories) categoryBox.addItem(c);
        } catch (SQLException e) {
            categoryBox.addItem("All Categories");
        }
    }
    
    private void refreshAddressChooser() {
		try {
		    addressList = customerService.loadAddresses(session.getUserId());
		    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
		    for (Map<String, String> a : addressList) {
		        String label = a.get("address_line") + ", " + a.get("city")
		                + (a.get("state") != null && !a.get("state").isBlank()
		                   ? ", " + a.get("state") : "")
		                + " - " + a.get("pincode")
		                + ("1".equals(a.get("is_default")) ? " (default)" : "");
		        model.addElement(label);
		    }
		    model.addElement("+ Enter new address…");
		    addressChooser.setModel(model);
		    for (int i = 0; i < addressList.size(); i++) {
		        if ("1".equals(addressList.get(i).get("is_default"))) {
		            addressChooser.setSelectedIndex(i);
		            break;
		        }
		    }
		} catch (SQLException e) {
		    addressChooser.removeAllItems();
		    addressChooser.addItem("+ Enter new address…");
		}
	}

    // ── Address chooser ───────────────────────────────────
	private void refreshAddresses(JTable table) {
		try {
		    addressList = customerService.loadAddresses(session.getUserId());
		    String[] cols = {"Address", "City", "State", "Country", "Pincode", "Default"};
		    Object[][] data = new Object[addressList.size()][6];
		    for (int i = 0; i < addressList.size(); i++) {
		        Map<String, String> a = addressList.get(i);
		        data[i][0] = a.get("address_line");
		        data[i][1] = a.get("city");
		        data[i][2] = a.get("state");
		        data[i][3] = a.get("country");
		        data[i][4] = a.get("pincode");
		        data[i][5] = "1".equals(a.get("is_default")) ? "✓" : "";
		    }
		    table.setModel(new javax.swing.table.DefaultTableModel(data, cols) {
		        @Override public boolean isCellEditable(int r, int col) { return false; }
		    });
		    table.setRowHeight(40);
		    table.setFont(Theme.BODY_FONT);
		} catch (SQLException e) { showDatabaseError(e); }
	}

    // ── Catalog panel ─────────────────────────────────────
    private JPanel buildCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        actions.setBackground(Theme.BACKGROUND);
        Theme.styleField(searchField);
        categoryBox.setFont(Theme.BODY_FONT);
        categoryBox.setForeground(Theme.TEXT);

        JLabel sl = lbl("Search");   JLabel cl = lbl("Category");  JLabel ql = lbl("Qty");
        JButton searchBtn  = Theme.secondaryButton("Search");
        JButton refreshBtn = Theme.secondaryButton("Refresh");
        JButton addBtn     = Theme.primaryButton("Add Selected to Cart");
        searchBtn .addActionListener(e -> loadCatalog());
        refreshBtn.addActionListener(e -> refreshAll());
        addBtn    .addActionListener(e -> addSelectedProduct());
        categoryBox.addActionListener(e -> loadCatalog());

        actions.add(sl); actions.add(searchField);
        actions.add(cl); actions.add(categoryBox);
        actions.add(ql); actions.add(quantitySpinner);
        actions.add(searchBtn); actions.add(refreshBtn); actions.add(addBtn);

        catalogTable.setRowHeight(40);
        catalogTable.setFont(Theme.BODY_FONT);
        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(catalogTable), BorderLayout.CENTER);
        return panel;
    }

    // ── Cart panel ────────────────────────────────────────
    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));

        cartTable.setRowHeight(40);
        cartTable.setFont(Theme.BODY_FONT);

        // Cart action buttons
        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        cartActions.setBackground(Theme.BACKGROUND);
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 0, 50, 1));
        JButton updateBtn = Theme.secondaryButton("Update Quantity");
        JButton removeBtn = Theme.secondaryButton("Remove Item");
        updateBtn.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a cart item first."); return; }
            int productId = Integer.parseInt(String.valueOf(cartTable.getValueAt(row, 1)));
            int qty = (Integer) qtySpinner.getValue();
            try { customerService.updateCartItem(session.getUserId(), productId, qty); refreshAll(); }
            catch (SQLException ex) { showDatabaseError(ex); }
        });
        removeBtn.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select a cart item first."); return; }
            int productId = Integer.parseInt(String.valueOf(cartTable.getValueAt(row, 1)));
            try { customerService.removeCartItem(session.getUserId(), productId); refreshAll(); }
            catch (SQLException ex) { showDatabaseError(ex); }
        });
        cartActions.add(lbl("New Qty:")); cartActions.add(qtySpinner);
        cartActions.add(updateBtn); cartActions.add(removeBtn);

        // Checkout card
        JPanel checkoutCard = new JPanel(new GridBagLayout());
        Theme.styleCard(checkoutCard);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,12,10,12); c.fill = GridBagConstraints.HORIZONTAL;

        addressChooser.setFont(Theme.BODY_FONT);
        addressChooser.setForeground(Theme.TEXT);

        // When "Enter new address" is chosen show a dialog
        addressChooser.addActionListener(e -> {
            if ("+ Enter new address…".equals(addressChooser.getSelectedItem())) {
                showAddAddressDialog(true);
            }
        });

        c.gridx=0; c.gridy=0; c.weightx=0.35; checkoutCard.add(lbl("Delivery Address"), c);
        c.gridx=1; c.weightx=0.65;             checkoutCard.add(addressChooser, c);
        c.gridx=0; c.gridy=1; c.weightx=0.35; checkoutCard.add(lbl("Payment Method"), c);
        c.gridx=1; c.weightx=0.65;             checkoutCard.add(paymentMethodBox, c);
        c.gridx=0; c.gridy=2; c.gridwidth=2;
        JButton placeOrderBtn = Theme.primaryButton("Place Order");
        placeOrderBtn.addActionListener(e -> placeOrder());
        checkoutCard.add(placeOrderBtn, c);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(Theme.BACKGROUND);
        south.add(cartActions,  BorderLayout.NORTH);
        south.add(checkoutCard, BorderLayout.CENTER);

        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    // ── Order History panel ───────────────────────────────
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));
        orderTable.setRowHeight(40);
        orderTable.setFont(Theme.BODY_FONT);
        panel.add(new JScrollPane(orderTable), BorderLayout.CENTER);
        return panel;
    }

    // ── Profile panel ─────────────────────────────────────
    private JPanel buildProfilePanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BACKGROUND);
        outer.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        // Left: edit basic info
        JPanel infoCard = new JPanel(new GridBagLayout());
        Theme.styleCard(infoCard);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,14,10,14); c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0; c.gridwidth=2;
        JLabel heading = new JLabel("Account Details");
        heading.setFont(Theme.HEADER_FONT); heading.setForeground(Theme.ACCENT_DARK);
        infoCard.add(heading, c);

        Theme.styleField(profFullNameField); Theme.styleField(profUsernameField);
        Theme.styleField(profEmailField);    Theme.styleField(profPhoneField);
        profPasswordField.setFont(Theme.BODY_FONT);
        profPasswordField.setBackground(java.awt.Color.WHITE);
        profPasswordField.setForeground(Theme.TEXT);

        String[] lbls   = {"Full Name","Username","Email","Phone","New Password (blank = no change)"};
        Object[]  flds   = {profFullNameField, profUsernameField, profEmailField, profPhoneField, profPasswordField};
        for (int i = 0; i < lbls.length; i++) {
            c.gridx=0; c.gridy=i+1; c.gridwidth=1; c.weightx=0.35;
            infoCard.add(lbl(lbls[i]), c);
            c.gridx=1; c.weightx=0.65;
            infoCard.add((java.awt.Component) flds[i], c);
        }
        c.gridx=0; c.gridy=lbls.length+1; c.gridwidth=2; c.weightx=1.0;
        JButton saveBtn = Theme.primaryButton("Save Account Changes");
        saveBtn.addActionListener(e -> saveCustomerProfile());
        infoCard.add(saveBtn, c);

        // Right: address book
        JPanel addrCard = new JPanel(new BorderLayout());
        Theme.styleCard(addrCard);

        JLabel addrHeading = new JLabel("Address Book");
        addrHeading.setFont(Theme.HEADER_FONT);
        addrHeading.setForeground(Theme.ACCENT_DARK);
        addrHeading.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));

        addrTable.setRowHeight(40);
        addrTable.setFont(Theme.BODY_FONT);

        JPanel addrButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        addrButtons.setBackground(Theme.CARD);
		JButton addAddrBtn    = Theme.primaryButton("Add Address");
		JButton editAddrBtn   = Theme.secondaryButton("Edit Selected");
		JButton setDefaultBtn = Theme.secondaryButton("Set as Default");
		JButton deleteAddrBtn = Theme.secondaryButton("Delete");
        addAddrBtn.addActionListener(e -> showAddAddressDialog(false));
        
        setDefaultBtn.addActionListener(e -> {
            int row = addrTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select an address first."); return; }
            int addressId = Integer.parseInt(addressList.get(row).get("address_id"));
            try {
                customerService.setDefaultAddress(session.getUserId(), addressId);
                refreshAddresses(addrTable);
                refreshAddressChooser();
            } catch (SQLException ex) { showDatabaseError(ex); }
        });
        deleteAddrBtn.addActionListener(e -> {
            int row = addrTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select an address first."); return; }
            int addressId = Integer.parseInt(addressList.get(row).get("address_id"));
            try {
                customerService.deleteAddress(addressId, session.getUserId());
                refreshAddresses(addrTable);
                refreshAddressChooser();
            } catch (SQLException ex) { showDatabaseError(ex); }
        });

		editAddrBtn.addActionListener(e -> {
			int row = addrTable.getSelectedRow();
			if (row == -1) { JOptionPane.showMessageDialog(this, "Select an address to edit."); return; }
			showEditAddressDialog(row);
		});

		addrButtons.add(addAddrBtn); addrButtons.add(editAddrBtn);
		addrButtons.add(setDefaultBtn); addrButtons.add(deleteAddrBtn);

        addrCard.add(addrHeading,             BorderLayout.NORTH);
        addrCard.add(new JScrollPane(addrTable), BorderLayout.CENTER);
        addrCard.add(addrButtons,             BorderLayout.SOUTH);

        refreshAddresses(addrTable);

        JPanel split = new JPanel(new java.awt.GridLayout(1, 2, 20, 0));
        split.setBackground(Theme.BACKGROUND);
        split.add(infoCard);
        split.add(addrCard);
        outer.add(split, BorderLayout.CENTER);
        return outer;
    }


	private void showAddAddressDialog(boolean fromCheckout) {
		JTextField addrField    = new JTextField(24);
		JTextField cityField    = new JTextField(16);
		JTextField stateField   = new JTextField(16);
		JTextField countryField = new JTextField(16);
		JTextField pincodeField = new JTextField(6);
		JCheckBox  defaultBox   = new JCheckBox("Set as default address");
		countryField.setText("India");
		Theme.styleField(addrField);  Theme.styleField(cityField);
		Theme.styleField(stateField); Theme.styleField(countryField);
		Theme.styleField(pincodeField);

		JPanel form = new JPanel(new GridBagLayout());
		form.setBackground(Theme.BACKGROUND);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(8,10,8,10); c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx=0; c.gridy=0; form.add(lbl("Address Line"), c); c.gridx=1; form.add(addrField,    c);
		c.gridx=0; c.gridy=1; form.add(lbl("City"),         c); c.gridx=1; form.add(cityField,    c);
		c.gridx=0; c.gridy=2; form.add(lbl("State"),        c); c.gridx=1; form.add(stateField,   c);
		c.gridx=0; c.gridy=3; form.add(lbl("Country"),      c); c.gridx=1; form.add(countryField, c);
		c.gridx=0; c.gridy=4; form.add(lbl("Pincode"),      c); c.gridx=1; form.add(pincodeField, c);
		c.gridx=0; c.gridy=5; c.gridwidth=2;                    form.add(defaultBox,              c);

		int result = JOptionPane.showConfirmDialog(this, form, "Add New Address",
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (result == JOptionPane.OK_OPTION) {
		    String addr    = addrField.getText().trim();
		    String city    = cityField.getText().trim();
		    String state   = stateField.getText().trim();
		    String country = countryField.getText().trim();
		    String pincode = pincodeField.getText().trim();
		    if (addr.isBlank() || city.isBlank() || pincode.isBlank()) {
		        JOptionPane.showMessageDialog(this, "Address, city and pincode are required.");
		        refreshAddressChooser();
		        return;
		    }
		    if (!pincode.matches("^[0-9]{6}$")) {
		        JOptionPane.showMessageDialog(this, "Pincode must be 6 digits.");
		        refreshAddressChooser();
		        return;
		    }
		    try {
		        customerService.addAddress(session.getUserId(), addr, city,
		                state.isBlank() ? null : state,
		                country.isBlank() ? "India" : country,
		                pincode, defaultBox.isSelected());
		        refreshAddressChooser();
		        refreshAddresses(addrTable);   // now works because addrTable is a class field
		        JOptionPane.showMessageDialog(this, "Address added.");
		    } catch (SQLException e) { showDatabaseError(e); }
		} else {
		    refreshAddressChooser();
		}
	}
	
	private void showEditAddressDialog(int rowIndex) {
		Map<String, String> existing = addressList.get(rowIndex);
		int addressId = Integer.parseInt(existing.get("address_id"));

		JTextField addrField    = new JTextField(existing.getOrDefault("address_line", ""), 24);
		JTextField cityField    = new JTextField(existing.getOrDefault("city",         ""), 16);
		JTextField stateField   = new JTextField(existing.getOrDefault("state",        ""), 16);
		JTextField countryField = new JTextField(existing.getOrDefault("country",      "India"), 16);
		JTextField pincodeField = new JTextField(existing.getOrDefault("pincode",      ""), 6);
		JCheckBox  defaultBox   = new JCheckBox("Set as default address",
		                                        "1".equals(existing.get("is_default")));
		Theme.styleField(addrField);  Theme.styleField(cityField);
		Theme.styleField(stateField); Theme.styleField(countryField);
		Theme.styleField(pincodeField);

		JPanel form = new JPanel(new GridBagLayout());
		form.setBackground(Theme.BACKGROUND);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(8,10,8,10); c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx=0; c.gridy=0; form.add(lbl("Address Line"), c); c.gridx=1; form.add(addrField,    c);
		c.gridx=0; c.gridy=1; form.add(lbl("City"),         c); c.gridx=1; form.add(cityField,    c);
		c.gridx=0; c.gridy=2; form.add(lbl("State"),        c); c.gridx=1; form.add(stateField,   c);
		c.gridx=0; c.gridy=3; form.add(lbl("Country"),      c); c.gridx=1; form.add(countryField, c);
		c.gridx=0; c.gridy=4; form.add(lbl("Pincode"),      c); c.gridx=1; form.add(pincodeField, c);
		c.gridx=0; c.gridy=5; c.gridwidth=2;                    form.add(defaultBox,              c);

		int result = JOptionPane.showConfirmDialog(this, form, "Edit Address",
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (result == JOptionPane.OK_OPTION) {
		    String addr    = addrField.getText().trim();
		    String city    = cityField.getText().trim();
		    String state   = stateField.getText().trim();
		    String country = countryField.getText().trim();
		    String pincode = pincodeField.getText().trim();

		    if (addr.isBlank() || city.isBlank() || pincode.isBlank()) {
		        JOptionPane.showMessageDialog(this, "Address, city and pincode are required.");
		        return;
		    }
		    if (!pincode.matches("^[0-9]{6}$")) {
		        JOptionPane.showMessageDialog(this, "Pincode must be 6 digits.");
		        return;
		    }
		    try {
		        customerService.updateAddress(addressId, session.getUserId(), addr, city,
		                state.isBlank()   ? null    : state,
		                country.isBlank() ? "India" : country,
		                pincode, defaultBox.isSelected());
		        refreshAddresses(addrTable);   // updates address book immediately
		        refreshAddressChooser();       // updates cart dropdown immediately
		        JOptionPane.showMessageDialog(this, "Address updated.");
		    } catch (SQLException e) { showDatabaseError(e); }
		}
	}
	
	
    private void loadProfileFields() {
        try {
            Map<String, String> p = customerService.loadCustomerProfile(session.getUserId());
            profFullNameField.setText(p.getOrDefault("full_name", ""));
            profUsernameField.setText(p.getOrDefault("username",  ""));
            profEmailField   .setText(p.getOrDefault("email",     ""));
            profPhoneField   .setText(p.getOrDefault("phone",     ""));
        } catch (SQLException e) { showDatabaseError(e); }
    }

    private void saveCustomerProfile() {
        String username = profUsernameField.getText().trim();
        String email    = profEmailField.getText().trim();
        String phone    = profPhoneField.getText().trim();
        String fullName = profFullNameField.getText().trim();
        String password = new String(profPasswordField.getPassword()).trim();

        if (username.isBlank() || email.isBlank() || fullName.isBlank()) {
            JOptionPane.showMessageDialog(this, "Full name, username and email are required.");
            return;
        }
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.");
            return;
        }
        try {
            customerService.updateCustomerProfile(session.getUserId(), username, email,
                    phone.isBlank() ? null : phone, fullName,
                    password.isBlank() ? null : password);
            // Update session and header live
            session.setFullName(fullName);
            greetingLabel.setText("Welcome, " + fullName);
            setTitle("Customer Dashboard - " + fullName);
            JOptionPane.showMessageDialog(this, "Profile updated successfully.");
            profPasswordField.setText("");
        } catch (SQLException e) { showDatabaseError(e); }
    }

    // ── Order placement + invoice ─────────────────────────
    private void placeOrder() {
        int selectedIdx = addressChooser.getSelectedIndex();
        if (selectedIdx < 0 || selectedIdx >= addressList.size()) {
            JOptionPane.showMessageDialog(this, "Please select or add a delivery address.");
            return;
        }
        Map<String, String> chosen = addressList.get(selectedIdx);
        String shippingAddress = chosen.get("address_line") + ", "
                + chosen.get("city") + " - " + chosen.get("pincode");
        try {
            String raw = customerService.placeOrder(session.getUserId(),
                    paymentMethodBox.getSelectedItem().toString(), shippingAddress);
            String[] parts     = raw.split("\\|");
            int    orderId     = Integer.parseInt(parts[0]);
            int    invoiceId   = Integer.parseInt(parts[1]);
            double total       = Double.parseDouble(parts[2]);
            String address     = parts[3];
            String payment     = parts[4];
            refreshAll();
            saveInvoice(orderId, invoiceId, total, address, payment);
            JOptionPane.showMessageDialog(this,
                    "Order placed!\nOrder ID: " + orderId
                    + "\nInvoice ID: " + invoiceId
                    + "\nTotal: ₹" + String.format("%.2f", total)
                    + "\n\nInvoice saved to ~/dbsl_miniproject/invoices/");
        } catch (SQLException e) { showDatabaseError(e); }
    }

    private void saveInvoice(int orderId, int invoiceId, double total,
                              String address, String payment) {
        try {
            Map<String, Object> data = customerService.loadOrderForInvoice(session.getUserId(), orderId);
            File dir = new File(System.getProperty("user.home") + "/dbsl_miniproject/invoices");
            dir.mkdirs();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File file = new File(dir, "invoice_" + orderId + "_" + timestamp + ".txt");

            try (FileWriter fw = new FileWriter(file)) {
                fw.write("=======================================================\n");
                fw.write("          ONLINE SHOPPING PORTAL - INVOICE             \n");
                fw.write("=======================================================\n");
                fw.write("Invoice ID   : " + invoiceId + "\n");
                fw.write("Order ID     : " + orderId   + "\n");
                fw.write("Date         : " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n");
                fw.write("-------------------------------------------------------\n");
                fw.write("Customer     : " + data.getOrDefault("full_name", "") + "\n");
                fw.write("Email        : " + data.getOrDefault("email",     "") + "\n");
                fw.write("Phone        : " + data.getOrDefault("phone",     "") + "\n");
                fw.write("Ship To      : " + address  + "\n");
                fw.write("Payment      : " + payment  + "\n");
                fw.write("-------------------------------------------------------\n");
                fw.write(String.format("%-40s %6s %10s %12s\n",
                        "Product", "Qty", "Unit Price", "Line Total"));
                fw.write("-------------------------------------------------------\n");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items =
                        (List<Map<String, Object>>) data.get("items");
                if (items != null) {
                    for (Map<String, Object> item : items) {
                        fw.write(String.format("%-40s %6s %10s %12s\n",
                                item.get("product_name"),
                                item.get("quantity"),
                                "₹" + String.format("%.2f", item.get("unit_price")),
                                "₹" + String.format("%.2f", item.get("line_total"))));
                    }
                }
                fw.write("-------------------------------------------------------\n");
                fw.write(String.format("%-58s %12s\n", "TOTAL",
                        "₹" + String.format("%.2f", total)));
                fw.write("=======================================================\n");
                fw.write("        Thank you for shopping with us!                \n");
                fw.write("=======================================================\n");
            }
        } catch (IOException | SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Order placed but invoice could not be saved:\n" + e.getMessage(),
                    "Invoice Warning", JOptionPane.WARNING_MESSAGE);
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
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a product first."); return; }
        int productId = Integer.parseInt(String.valueOf(catalogTable.getValueAt(row, 0)));
        int quantity  = (Integer) quantitySpinner.getValue();
        try {
            customerService.addToCart(session.getUserId(), productId, quantity);
            refreshAll();
            JOptionPane.showMessageDialog(this, "Product added to cart.");
        } catch (SQLException e) { showDatabaseError(e); }
    }

    private void refreshAll() {
        loadCatalog();
        try {
            cartTable .setModel(customerService.loadCart(session.getUserId()));
            orderTable.setModel(customerService.loadOrderHistory(session.getUserId()));
        } catch (SQLException e) { showDatabaseError(e); }
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.BODY_FONT);
        l.setForeground(Theme.TEXT);
        return l;
    }

    private void showDatabaseError(SQLException e) {
        JOptionPane.showMessageDialog(this,
                "Database operation failed.\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
