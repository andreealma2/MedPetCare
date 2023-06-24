package com.example.petcareapp.model;

import java.util.Date;

public class Consultation {

    private String userName;
    private String userId;
    private String petName;
    private String petType;
    private String petBreed;
    private String petId;
    private String subject;
    private String recommendation;
    private Date consultationDate;
    private String consultationId;

    public Consultation() {
    }

    public Consultation(String userName, String userId, String petName, String petType, String petBreed, String petId, String subject, String recommendation, Date consultationDate) {
        this.userName = userName;
        this.userId = userId;
        this.petName = petName;
        this.petType = petType;
        this.petBreed = petBreed;
        this.petId = petId;
        this.subject = subject;
        this.recommendation = recommendation;
        this.consultationDate = consultationDate;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPetType() {
        return petType;
    }

    public void setPetType(String petType) {
        this.petType = petType;
    }

    public String getPetBreed() {
        return petBreed;
    }

    public void setPetBreed(String petBreed) {
        this.petBreed = petBreed;
    }

    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public Date getConsultationDate() {
        return consultationDate;
    }

    public void setConsultationDate(Date consultationDate) {
        this.consultationDate = consultationDate;
    }

    public String getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(String consultationId) {
        this.consultationId = consultationId;
    }
}
