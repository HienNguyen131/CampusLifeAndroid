package com.example.campuslife.entity;

import java.util.List;

public class SubmissionResponse {
    private Long id;
    private String content;
    private List<String> fileUrls;
    private String submittedAt;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getFileUrls() {
        return fileUrls;
    }
    public void setFileUrls(List<String> fileUrls) {
        this.fileUrls = fileUrls;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }
    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }
}
