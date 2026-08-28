package com.terralink.data.model;

public class KycDocumentResponse {
    private int id;
    private String docType;
    private String fileUrl;
    private boolean verified;
    private String verifiedAt;

    public int getId() {
        return id;
    }

    public String getDocType() {
        return docType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getVerifiedAt() {
        return verifiedAt;
    }
}
