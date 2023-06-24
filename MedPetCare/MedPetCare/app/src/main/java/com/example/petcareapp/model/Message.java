package com.example.petcareapp.model;

import java.util.Date;

public class Message {
    private String sender;
    private String message;
    private Date time;
    private boolean isCurrentUser;
    private boolean isRead;
    private String messageId;

    public Message(String sender, String message, Date time, boolean isCurrentUser, boolean isRead, String messageId) {
        this.sender = sender;
        this.message = message;
        this.time = time;
        this.isCurrentUser = isCurrentUser;
        this.isRead = isRead;
        this.messageId = messageId;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Date getTime() {
        return time;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}

