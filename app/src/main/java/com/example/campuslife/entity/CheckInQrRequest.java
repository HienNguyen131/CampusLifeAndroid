package com.example.campuslife.entity;

public class CheckInQrRequest {
    private String checkInCode;

    public CheckInQrRequest(String checkInCode) {
        this.checkInCode = checkInCode;
    }
    public String getCheckInCode() {
        return checkInCode;
    }

    public void setCheckInCode(String checkInCode) {
        this.checkInCode = checkInCode;
    }
}
