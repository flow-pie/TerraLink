package com.terralink.data.model;

import java.util.Date;

public class AssetResponse {
    private long id;
    private long clientId;
    private String assetType;
    private String description;
    private int quantity;
    private double estimatedValue;
    private String verificationStatus;
    private Date verifiedAt;
    private Long verifiedBy;
    private Date recordedAt;

    public long getId() {
        return id;
    }

    public long getClientId() {
        return clientId;
    }

    public String getAssetType() {
        return assetType;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getEstimatedValue() {
        return estimatedValue;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public Date getVerifiedAt() {
        return verifiedAt;
    }

    public Long getVerifiedBy() {
        return verifiedBy;
    }

    public Date getRecordedAt() {
        return recordedAt;
    }
}
