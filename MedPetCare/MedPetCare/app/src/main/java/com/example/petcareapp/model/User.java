package com.example.petcareapp.model;

public class User {
    private String Nume;
    private String Prenume;
    private String Email;
    private String Parola;
    private String UserId;
    private String UserType;
    private int unreadCount;

    public User() {
    }

    public User(String nume, String prenume, String email, String parola, String userId, String userType) {
        Nume = nume;
        Prenume = prenume;
        Email = email;
        Parola = parola;
        UserId = userId;
        UserType = userType;
    }

    public String getNume() {
        return Nume;
    }

    public void setNume(String nume) {
        Nume = nume;
    }

    public String getPrenume() {
        return Prenume;
    }

    public void setPrenume(String prenume) {
        Prenume = prenume;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getParola() {
        return Parola;
    }

    public void setParola(String parola) {
        Parola = parola;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getUserType() {
        return UserType;
    }

    public void setUserType(String userType) {
        UserType = userType;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
