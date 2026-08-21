package com.terralink.ui.common;

import com.terralink.data.model.LoginResponse;
import com.terralink.ui.auth.LoginStatus;

import retrofit2.Response;

public class Resource<T> {
    private final LoginStatus status;
    private final T data;
    private final String message;

    private Resource(
            LoginStatus status,
            T data,
            String message
    ){
        this.status =status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> loading(){
        return new Resource<>(
                LoginStatus.LOADING,
                null,
                null
        );
    }

    public static <T> Resource<T> success(T data){
        return new Resource<>(
            LoginStatus.SUCCESS,
            data,
    null
        );
    }

    public static <T> Resource<T> error (String message){
        return new Resource<>(
                LoginStatus.ERROR,
                null,
                message
        );
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public LoginStatus getStatus() {
        return status;
    }
}
