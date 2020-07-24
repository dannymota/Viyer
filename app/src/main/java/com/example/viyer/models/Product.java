package com.example.viyer.models;

import com.google.firebase.firestore.DocumentId;

import org.json.JSONArray;
import org.parceler.Parcel;

import java.util.List;

@Parcel
public class Product {
    private String title;
    private String description;
    private String price;
    private String uid;

    public String getUid() {
        return uid;
    }

    private List<String> photoUrls;

    @DocumentId
    private String documentId;

    public String getDocumentId() {
        return documentId;
    }

    public Product() {
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }
}
