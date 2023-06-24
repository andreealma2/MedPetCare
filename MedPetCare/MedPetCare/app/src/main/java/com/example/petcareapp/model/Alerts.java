package com.example.petcareapp.model;

import java.util.Date;

public class Alerts {

    private String petName;
    private String alertType;
    private Date dateTime;
    private String alertId;

    public Alerts() {
    }

    public Alerts(String petName, String alertType, Date dateTime) {
        this.petName = petName;
        this.alertType = alertType;
        this.dateTime = dateTime;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }
}
