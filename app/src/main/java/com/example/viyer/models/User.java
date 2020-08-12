package com.example.viyer.models;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;
import java.util.List;

public class User {
    private String email;
    @DocumentId
    private String documentId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String location;
    private Date updatedAt;
    private Boolean id;

    public Boolean getId() {
        return id;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getEmail() {
        return email;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLocation() {
        return location;
    }
}
