package com.dbsl.shop.service;

import com.dbsl.shop.db.OracleConnectionManager;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class CustomerService {

    public DefaultTableModel loadCatalog(String searchText, String categoryName) throws SQLException {
        String sql = """
                SELECT product_id, product_name, category_name, unit_price, stock_qty, status
                FROM vw_product_catalog
                WHERE (LOWER(product_name) LIKE ? OR LOWER(category_name) LIKE ?)
                """;
        if (categoryName != null && !categoryName.equals("All Categories")) {
            sql += " AND LOWER(category_name) = ?";
        }
        sql += " ORDER BY category_name, product_name";

        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + searchText.toLowerCase().trim() + "%";
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            if (categoryName != null && !categoryName.equals("All Categories")) {
                statement.setString(3, categoryName.toLowerCase());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return buildTableModel(resultSet);
            }
        }
    }

    public DefaultTableModel loadCatalog(String searchText) throws SQLException {
        return loadCatalog(searchText, null);
    }

    public List<String> loadCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        String sql = "SELECT category_name FROM category ORDER BY display_order, category_name";
        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(resultSet.getString("category_name"));
            }
        }
        return categories;
    }

    public DefaultTableModel loadCart(int customerId) throws SQLException {
        String sql = """
                SELECT ci.cart_item_id, p.product_id, p.product_name,
                       ci.quantity, ci.unit_price, ci.line_total
                FROM shopping_cart sc
                JOIN cart_item ci ON ci.cart_id = sc.cart_id
                JOIN product p ON p.product_id = ci.product_id
                WHERE sc.customer_id = ?
                ORDER BY p.product_name
                """;
        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return buildTableModel(resultSet);
            }
        }
    }

    public DefaultTableModel loadOrderHistory(int customerId) throws SQLException {
        String sql = """
                SELECT order_number, order_date, status, payment_status, total_amount
                FROM vw_customer_order_history
                WHERE customer_id = ?
                ORDER BY order_date DESC
                """;
        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return buildTableModel(resultSet);
            }
        }
    }

    public void addToCart(int customerId, int productId, int quantity) throws SQLException {
        try (Connection connection = OracleConnectionManager.getConnection();
             CallableStatement statement = connection.prepareCall("{call shop_portal_pkg.add_to_cart(?,?,?)}")) {
            statement.setInt(1, customerId);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);
            statement.execute();
            connection.commit();
        }
    }

    public void updateCartItem(int customerId, int productId, int quantity) throws SQLException {
        try (Connection connection = OracleConnectionManager.getConnection();
             CallableStatement statement = connection.prepareCall("{call shop_portal_pkg.update_cart_quantity(?,?,?)}")) {
            statement.setInt(1, customerId);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);
            statement.execute();
            connection.commit();
        }
    }

    public void removeCartItem(int customerId, int productId) throws SQLException {
        // passing 0 quantity triggers delete in the package
        updateCartItem(customerId, productId, 0);
    }

    public String placeOrder(int customerId, String paymentMethod, String address) throws SQLException {
        try (Connection connection = OracleConnectionManager.getConnection();
             CallableStatement statement = connection.prepareCall("{call shop_portal_pkg.place_order(?,?,?,?,?,?)}")) {
            statement.setInt(1, customerId);
            statement.setString(2, paymentMethod);
            statement.setString(3, address);
            statement.registerOutParameter(4, Types.INTEGER);
            statement.registerOutParameter(5, Types.INTEGER);
            statement.registerOutParameter(6, Types.DOUBLE);
            statement.execute();
            connection.commit();
            int orderId     = statement.getInt(4);
            int invoiceId   = statement.getInt(5);
            double total    = statement.getDouble(6);
            return orderId + "|" + invoiceId + "|" + total + "|" + address + "|" + paymentMethod;
        }
    }

    public Map<String, Object> loadOrderForInvoice(int customerId, int orderId) throws SQLException {
        Map<String, Object> invoice = new LinkedHashMap<>();
        String orderSql = """
                SELECT co.order_number, co.order_date, co.status,
                       co.shipping_address, co.total_amount,
                       u.full_name, u.email, u.phone
                FROM customer_order co
                JOIN app_user u ON u.user_id = co.customer_id
                WHERE co.order_id = ? AND co.customer_id = ?
                """;
        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement st = connection.prepareStatement(orderSql)) {
            st.setInt(1, orderId);
            st.setInt(2, customerId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    invoice.put("order_number",    rs.getString("order_number"));
                    invoice.put("order_date",      rs.getString("order_date"));
                    invoice.put("status",          rs.getString("status"));
                    invoice.put("shipping_address",rs.getString("shipping_address"));
                    invoice.put("total_amount",    rs.getDouble("total_amount"));
                    invoice.put("full_name",       rs.getString("full_name"));
                    invoice.put("email",           rs.getString("email"));
                    invoice.put("phone",           rs.getString("phone"));
                }
            }
        }
        String itemSql = """
                SELECT p.product_name, oi.quantity, oi.unit_price, oi.line_total
                FROM order_item oi
                JOIN product p ON p.product_id = oi.product_id
                WHERE oi.order_id = ?
                """;
        List<Map<String, Object>> items = new ArrayList<>();
        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement st = connection.prepareStatement(itemSql)) {
            st.setInt(1, orderId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("product_name", rs.getString("product_name"));
                    item.put("quantity",     rs.getInt("quantity"));
                    item.put("unit_price",   rs.getDouble("unit_price"));
                    item.put("line_total",   rs.getDouble("line_total"));
                    items.add(item);
                }
            }
        }
        invoice.put("items", items);
        return invoice;
    }

    // ── Address management ────────────────────────────────

	public List<Map<String, String>> loadAddresses(int customerId) throws SQLException {
		List<Map<String, String>> list = new ArrayList<>();
		String sql = """
		        SELECT address_id, address_line, city, state, country, pincode, is_default
		        FROM customer_address
		        WHERE customer_id = ?
		        ORDER BY is_default DESC, address_id
		        """;
		try (Connection connection = OracleConnectionManager.getConnection();
		     PreparedStatement st = connection.prepareStatement(sql)) {
		    st.setInt(1, customerId);
		    try (ResultSet rs = st.executeQuery()) {
		        while (rs.next()) {
		            Map<String, String> m = new LinkedHashMap<>();
		            m.put("address_id",   rs.getString("address_id"));
		            m.put("address_line", rs.getString("address_line"));
		            m.put("city",         rs.getString("city"));
		            m.put("state",        nullSafe(rs.getString("state")));
		            m.put("country",      nullSafe(rs.getString("country")));
		            m.put("pincode",      rs.getString("pincode"));
		            m.put("is_default",   rs.getString("is_default"));
		            list.add(m);
		        }
		    }
		}
		return list;
	}

	public void addAddress(int customerId, String addressLine, String city, String state,
		                   String country, String pincode, boolean isDefault) throws SQLException {
		try (Connection connection = OracleConnectionManager.getConnection()) {
		    if (isDefault) {
		        try (PreparedStatement st = connection.prepareStatement(
		                "UPDATE customer_address SET is_default=0 WHERE customer_id=?")) {
		            st.setInt(1, customerId);
		            st.executeUpdate();
		        }
		    }
		    String sql = """
		            INSERT INTO customer_address
		            (address_id, customer_id, address_line, city, state, country, pincode, is_default)
		            VALUES (seq_cust_address.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)
		            """;
		    try (PreparedStatement st = connection.prepareStatement(sql)) {
		        st.setInt(1, customerId);
		        st.setString(2, addressLine);
		        st.setString(3, city);
		        st.setString(4, state);
		        st.setString(5, country);
		        st.setString(6, pincode);
		        st.setInt(7, isDefault ? 1 : 0);
		        st.executeUpdate();
		    }
		    connection.commit();
		}
	}
	
    public void setDefaultAddress(int customerId, int addressId) throws SQLException {
        try (Connection connection = OracleConnectionManager.getConnection()) {
            try (PreparedStatement st = connection.prepareStatement(
                    "UPDATE customer_address SET is_default=0 WHERE customer_id=?")) {
                st.setInt(1, customerId);
                st.executeUpdate();
            }
            try (PreparedStatement st = connection.prepareStatement(
                    "UPDATE customer_address SET is_default=1 WHERE address_id=? AND customer_id=?")) {
                st.setInt(1, addressId);
                st.setInt(2, customerId);
                st.executeUpdate();
            }
            connection.commit();
        }
    }

    public void deleteAddress(int addressId, int customerId) throws SQLException {
        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement st = connection.prepareStatement(
                     "DELETE FROM customer_address WHERE address_id=? AND customer_id=?")) {
            st.setInt(1, addressId);
            st.setInt(2, customerId);
            st.executeUpdate();
            connection.commit();
        }
    }
    
    public void updateAddress(int addressId, int customerId, String addressLine, String city,
                          String state, String country, String pincode, boolean isDefault) throws SQLException {
		try (Connection connection = OracleConnectionManager.getConnection()) {
		    if (isDefault) {
		        try (PreparedStatement st = connection.prepareStatement(
		                "UPDATE customer_address SET is_default=0 WHERE customer_id=?")) {
		            st.setInt(1, customerId);
		            st.executeUpdate();
		        }
		    }
		    String sql = """
		            UPDATE customer_address
		            SET address_line=?, city=?, state=?, country=?, pincode=?, is_default=?
		            WHERE address_id=? AND customer_id=?
		            """;
		    try (PreparedStatement st = connection.prepareStatement(sql)) {
		        st.setString(1, addressLine);
		        st.setString(2, city);
		        st.setString(3, state);
		        st.setString(4, country);
		        st.setString(5, pincode);
		        st.setInt(6, isDefault ? 1 : 0);
		        st.setInt(7, addressId);
		        st.setInt(8, customerId);
		        st.executeUpdate();
		    }
		    connection.commit();
		}
	}

    // ── Profile ───────────────────────────────────────────

    public Map<String, String> loadCustomerProfile(int userId) throws SQLException {
        String sql = """
                SELECT u.username, u.email, u.phone, u.full_name
                FROM app_user u
                WHERE u.user_id = ?
                """;
        try (Connection connection = OracleConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                Map<String, String> map = new LinkedHashMap<>();
                if (resultSet.next()) {
                    map.put("username",  nullSafe(resultSet.getString("username")));
                    map.put("email",     nullSafe(resultSet.getString("email")));
                    map.put("phone",     nullSafe(resultSet.getString("phone")));
                    map.put("full_name", nullSafe(resultSet.getString("full_name")));
                }
                return map;
            }
        }
    }

    public void updateCustomerProfile(int userId, String username, String email,
                                      String phone, String fullName,
                                      String newPassword) throws SQLException {
        try (Connection connection = OracleConnectionManager.getConnection()) {
            if (newPassword != null && !newPassword.isBlank()) {
                String sql = """
                        UPDATE app_user SET username=?, email=?, phone=?, full_name=?,
                        password_hash=STANDARD_HASH(?, 'SHA256') WHERE user_id=?
                        """;
                try (PreparedStatement st = connection.prepareStatement(sql)) {
                    st.setString(1, username);
                    st.setString(2, email);
                    st.setString(3, phone);
                    st.setString(4, fullName);
                    st.setString(5, newPassword);
                    st.setInt(6, userId);
                    st.executeUpdate();
                }
            } else {
                String sql = "UPDATE app_user SET username=?, email=?, phone=?, full_name=? WHERE user_id=?";
                try (PreparedStatement st = connection.prepareStatement(sql)) {
                    st.setString(1, username);
                    st.setString(2, email);
                    st.setString(3, phone);
                    st.setString(4, fullName);
                    st.setInt(5, userId);
                    st.executeUpdate();
                }
            }
            connection.commit();
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private DefaultTableModel buildTableModel(ResultSet resultSet) throws SQLException {
        Vector<String> columnNames = new Vector<>();
        int columnCount = resultSet.getMetaData().getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(resultSet.getMetaData().getColumnLabel(column));
        }
        Vector<Vector<Object>> rows = new Vector<>();
        while (resultSet.next()) {
            Vector<Object> row = new Vector<>();
            for (int column = 1; column <= columnCount; column++) {
                row.add(resultSet.getObject(column));
            }
            rows.add(row);
        }
        return new DefaultTableModel(rows, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
