package com.example.campuslife.entity;


public class ActivityParticipationRequest {

    private String ticketCode;

    public ActivityParticipationRequest(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }
}
