package com.example.campuslife.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class Activity implements Serializable {
    public Long id;

    @SerializedName("type")
    public String type;

    @SerializedName("scoreType")
    public String scoreType;

    public String name;
    public String description;

    public String startDate;
    public String endDate;

    @SerializedName("requiresSubmission")
    public boolean requiresSubmission;

    public BigDecimal maxPoints;

    public String registrationStartDate;
    public String registrationDeadline;

    public String shareLink;

    @SerializedName(value = "important", alternate = {"isImportant"})
    public boolean isImportant;

    public String bannerUrl;
    public String location;

    @SerializedName(value = "deleted", alternate = {"isDeleted"})
    public boolean isDeleted;

    public Integer ticketQuantity;
    public String benefits;
    public String requirements;
    public String contactInfo;

    public boolean mandatoryForFacultyStudents;

    public BigDecimal penaltyPointsIncomplete;

    public List<Long> organizerIds;


    public String createdAt;
    public String updatedAt;

    public String createdBy;
    public String lastModifiedBy;
    private long participantCount;
    private long remainingDays;

    public long getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(long participantCount) {
        this.participantCount = participantCount;
    }

    public long getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(long remainingDays) {
        this.remainingDays = remainingDays;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScoreType() {
        return scoreType;
    }

    public void setScoreType(String scoreType) {
        this.scoreType = scoreType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isRequiresSubmission() {
        return requiresSubmission;
    }
    private ActivitySeries series;

    public void setRequiresSubmission(boolean requiresSubmission) {
        this.requiresSubmission = requiresSubmission;
    }

    public BigDecimal getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(BigDecimal maxPoints) {
        this.maxPoints = maxPoints;
    }

    public String getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(String registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(String registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public String getShareLink() {
        return shareLink;
    }

    public void setShareLink(String shareLink) {
        this.shareLink = shareLink;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Integer getTicketQuantity() {
        return ticketQuantity;
    }

    public void setTicketQuantity(Integer ticketQuantity) {
        this.ticketQuantity = ticketQuantity;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public boolean isMandatoryForFacultyStudents() {
        return mandatoryForFacultyStudents;
    }

    public void setMandatoryForFacultyStudents(boolean mandatoryForFacultyStudents) {
        this.mandatoryForFacultyStudents = mandatoryForFacultyStudents;
    }

    public BigDecimal getPenaltyPointsIncomplete() {
        return penaltyPointsIncomplete;
    }

    public void setPenaltyPointsIncomplete(BigDecimal penaltyPointsIncomplete) {
        this.penaltyPointsIncomplete = penaltyPointsIncomplete;
    }


    public List<Long> getOrganizerIds() {
        return organizerIds;
    }

    public void setOrganizerIds(List<Long> organizerIds) {
        this.organizerIds = organizerIds;
    }

    public ActivitySeries getSeries() {
        return series;
    }

    public void setSeries(ActivitySeries series) {
        this.series = series;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}
