package com.terralink.data.model;

public class LoginRequest {
    private String identifier;
    private String password;

    public LoginRequest(String loginIdentifier, String password) {
        this.identifier = loginIdentifier;
        this.password = password;
    }
    public String getIdentifier() {
        return identifier;
    }
    public String getPassword(){
        return password;
    }
}
