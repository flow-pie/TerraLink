package com.terralink.data.model;

public class VerifyAssetRequest {
    private double estimatedValue;

    public VerifyAssetRequest(double estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public double getEstimatedValue() {
        return estimatedValue;
    }
}
