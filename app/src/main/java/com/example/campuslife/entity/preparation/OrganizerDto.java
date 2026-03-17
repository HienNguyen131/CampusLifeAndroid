package com.example.campuslife.entity.preparation;

public class OrganizerDto {
    private Long studentId;
    private String fullName;

    public OrganizerDto() {
    }

    public OrganizerDto(Long studentId, String fullName) {
        this.studentId = studentId;
        this.fullName = fullName;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
