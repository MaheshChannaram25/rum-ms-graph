package com.tsa.mtc.rum.delta;

import com.opencsv.bean.CsvDate;

import java.util.Date;

public class Appointment {
    private String appointmentId;
    private Integer hotelId;
    private String hotelName;
    private String opportunityId;
    private String userId;
    private String activityType;
    private String startDateTime;
    private String endDateTime;
    private String appointmentStatus;
    private Float durationMins;
    private Float durationDays;
    private Float durationHours;
    private Boolean isBillable;
    private String location;
    private String activityDetails;
    private String notes;
    private String isTrainerLocal;
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    private Date originalStartDate;
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    private Date originalEndDate;
    private String createdBy;
    private String createdDate;
    private String modifiedBy;
    private String modifiedDate;
    private String subject;
    private String seriesMasterId;
    private String eventType;

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getHotelId() {
        return hotelId;
    }

    public void setHotelId(Integer hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(String opportunityId) {
        this.opportunityId = opportunityId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public Float getDurationMins() {
        return durationMins;
    }

    public void setDurationMins(Float durationMins) {
        this.durationMins = durationMins;
    }

    public Float getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Float durationDays) {
        this.durationDays = durationDays;
    }

    public Float getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Float durationHours) {
        this.durationHours = durationHours;
    }

    public Boolean getBillable() {
        return isBillable;
    }

    public void setBillable(Boolean billable) {
        isBillable = billable;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getActivityDetails() {
        return activityDetails;
    }

    public void setActivityDetails(String activityDetails) {
        this.activityDetails = activityDetails;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIsTrainerLocal() {
        return isTrainerLocal;
    }

    public void setIsTrainerLocal(String isTrainerLocal) {
        this.isTrainerLocal = isTrainerLocal;
    }

    public Date getOriginalStartDate() {
        return originalStartDate;
    }

    public void setOriginalStartDate(Date originalStartDate) {
        this.originalStartDate = originalStartDate;
    }

    public Date getOriginalEndDate() {
        return originalEndDate;
    }

    public void setOriginalEndDate(Date originalEndDate) {
        this.originalEndDate = originalEndDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSeriesMasterId() {
        return seriesMasterId;
    }

    public void setSeriesMasterId(String seriesMasterId) {
        this.seriesMasterId = seriesMasterId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
