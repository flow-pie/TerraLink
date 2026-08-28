package com.terralink.data.model;

import com.google.gson.annotations.SerializedName;

public class ClientListItemResponse {
    private int id;
    private String fullName;
    private String phone;
    private String nationalId;
    
    @SerializedName("employeeNo")
    private String clientNo;
    
    @SerializedName("verificationStatus")
    private String status;
    
    private String userStatus;
    private String createdAt;

    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getClientNo() { return clientNo; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
    public String getUserStatus() { return userStatus; }
    public String getCreatedAt() { return createdAt; }
    public String getNationalId() { return nationalId; }
}
