package com.terralink.data.model;

public class CreateAssetRequest {
    private String assetType;
    private String description;
    private int quantity;
    private double estimatedValue;

    public CreateAssetRequest(String assetType, String description, int quantity, double estimatedValue) {
        this.assetType = assetType;
        this.description = description;
        this.quantity = quantity;
        this.estimatedValue = estimatedValue;
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
}
