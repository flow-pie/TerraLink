package com.terralink.data.model;

import java.time.LocalDateTime;

public class UserProfileResponse {

    private String id;
    private String username;
    private String email;
    private String employeeNo;
    private String roleName;
    private String status;
    private boolean mfaEnabled;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;


    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getStatus() {
        return status;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
