package com.terralink.data.model;

public class UserProfileResponse {

    private String userId;
    private String fullName;
    private String email;
    private String employeeNo;
    private String roleName;
    private String clientId;
    private String status;
    private boolean mfaEnabled;
    private String lastLogin;
    private String createdAt;


    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
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

    public String getClientId(){return clientId;}

    public String getStatus() {
        return status;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public String getCreatedAt() {
        return createdAt;
    }

}
