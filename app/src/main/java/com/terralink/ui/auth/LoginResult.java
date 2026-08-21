package com.terralink.ui.auth;

import kotlin.text.UStringsKt;

// Represents the state of a login operation.
// It tells the UI whether login succeeded or failed.
public class LoginResult {

    private final LoginStatus status;
    private  final String message;

    public LoginResult(LoginStatus status, String message ){
        this.status = status;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public LoginStatus getStatus() {
        return status;
    }


}
