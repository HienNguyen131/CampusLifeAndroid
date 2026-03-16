package com.example.campuslife.entity;

import java.util.List;

public class CreateActivityRequest {
    public String name;
    public String type; // SUKIEN, MINIGAME, CONG_TAC_XA_HOI, CHUYEN_DE_DOANH_NGHIEP
    public String scoreType; // REN_LUYEN, CONG_TAC_XA_HOI, CHUYEN_DE
    public String description;
    public String startDate;
    public String endDate;
    public String registrationStartDate;
    public String registrationDeadline;
    public String location;
    public Integer ticketQuantity;
    public Double maxPoints;
    public Double penaltyPointsIncomplete;
    public String benefits;
    public String requirements;
    public String contactInfo;
    public Boolean requiresSubmission;
    public Boolean requiresApproval;
    public Boolean mandatoryForFacultyStudents;
    public Boolean isImportant;
    public Boolean isDraft;
    public String bannerUrl;
    public String shareLink;
    public List<Long> organizerIds;
}
