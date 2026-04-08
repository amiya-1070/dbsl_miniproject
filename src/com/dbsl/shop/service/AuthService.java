package com.dbsl.shop.service;

import java.sql.PreparedStatement;
import com.dbsl.shop.db.OracleConnectionManager;
import com.dbsl.shop.model.UserSession;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class AuthService {
    public UserSession login(String username, String password, String role) throws SQLException {
        try (Connection connection = OracleConnectionManager.getConnection();
             CallableStatement statement = connection.prepareCall("{call shop_portal_pkg.authenticate_user(?,?,?,?,?)}")) {
            statement.setString(1, username.trim());
            statement.setString(2, password);
            statement.setString(3, role);
            statement.registerOutParameter(4, Types.INTEGER);
            statement.registerOutParameter(5, Types.VARCHAR);
            statement.execute();
            return new UserSession(statement.getInt(4), statement.getString(5), role);
        }
    }
    public void register(String username, String password, String name, String email, String phone) throws SQLException {
    String sql = """
            INSERT INTO app_user (user_id, username, password_hash, role, full_name, email, phone, status, created_at)
            VALUES (seq_app_user.NEXTVAL, ?, STANDARD_HASH(?, 'SHA256'), 'CUSTOMER', ?, ?, ?, 'ACTIVE', SYSDATE)
            """;
    try (Connection connection = OracleConnectionManager.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setString(1, username);
        statement.setString(2, password);
        statement.setString(3, name);
        statement.setString(4, email);
        statement.setString(5, phone);
        statement.executeUpdate();
        connection.commit();
    }
}
}
