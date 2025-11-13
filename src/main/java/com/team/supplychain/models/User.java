package com.team.supplychain.models;

import java.time.LocalDateTime;

/**
 * User model class representing system users
 */
public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    
    // Default constructor
    public User() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with parameters
    public User(String username, String email, String role) {
        this();
        this.username = username;
        this.email = email;
        this.role = role;
    }
    
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    public boolean isManager() {
        return "MANAGER".equalsIgnoreCase(role);
    }
    
    public boolean isEmployee() {
        return "EMPLOYEE".equalsIgnoreCase(role);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + getFullName() + '\'' +
                '}';
    }
}