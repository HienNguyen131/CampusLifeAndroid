package com.example.campuslife.api;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {

    @SerializedName(value = "status", alternate = {"success"})
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName(value = "body", alternate = {"data"})
    private T data;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
