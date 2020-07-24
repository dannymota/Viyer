package com.example.viyer.models;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class Message {
    private String content;
    private Date createdAt;
    private String fromUid;

    public Message() {
    }

    public String getContent() {
        return content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getFromUid() {
        return fromUid;
    }
}
