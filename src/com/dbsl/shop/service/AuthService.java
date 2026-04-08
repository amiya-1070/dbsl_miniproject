package com.dbsl.shop.service;

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
}
