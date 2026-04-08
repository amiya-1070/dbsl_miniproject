package com.dbsl.shop.model;

public class UserSession {
    private final int userId;
    private final String fullName;
    private final String role;

    public UserSession(int userId, String fullName, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }
}
