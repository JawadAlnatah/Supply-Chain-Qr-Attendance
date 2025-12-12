package com.team.supplychain.models;

import java.time.LocalDateTime;

/**
 * Model class representing an audit log entry
 * Matches the audit_logs database table schema
 */
public class AuditLog {
    private int logId;
    private String logCode;
    private LocalDateTime timestamp;
    private Integer userId;  // Nullable for system/anonymous actions
    private String username;
    private String actionType;  // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, BACKUP, READ
    private String module;
    private String description;
    private String ipAddress;
    private String result;  // SUCCESS, FAILED, WARNING

    /**
     * Default constructor
     */
    public AuditLog() {
    }

    /**
     * Constructor with all fields
     */
    public AuditLog(int logId, String logCode, LocalDateTime timestamp, Integer userId,
                    String username, String actionType, String module, String description,
                    String ipAddress, String result) {
        this.logId = logId;
        this.logCode = logCode;
        this.timestamp = timestamp;
        this.userId = userId;
        this.username = username;
        this.actionType = actionType;
        this.module = module;
        this.description = description;
        this.ipAddress = ipAddress;
        this.result = result;
    }

    /**
     * Constructor without logId and logCode (for creating new logs)
     */
    public AuditLog(LocalDateTime timestamp, Integer userId, String username,
                    String actionType, String module, String description,
                    String ipAddress, String result) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.username = username;
        this.actionType = actionType;
        this.module = module;
        this.description = description;
        this.ipAddress = ipAddress;
        this.result = result;
    }

    // Getters and Setters

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getLogCode() {
        return logCode;
    }

    public void setLogCode(String logCode) {
        this.logCode = logCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "logId=" + logId +
                ", logCode='" + logCode + '\'' +
                ", timestamp=" + timestamp +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", actionType='" + actionType + '\'' +
                ", module='" + module + '\'' +
                ", description='" + description + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
