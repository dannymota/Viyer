package com.example.viyer.models;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;
import java.util.List;

public class Chatroom {
    private String productId;
    private String recentMessage;

    public String getRecentMessage() {
        return recentMessage;
    }

    private List<String> uids;
    private Date updatedAt;

    public Date getUpdatedAt() {
        return updatedAt;
    }

    @DocumentId
    private String documentId;

    public String getDocumentId() {
        return documentId;
    }

    public String getProductId() {
        return productId;
    }

    public List<String> getUids() {
        return uids;
    }
}
