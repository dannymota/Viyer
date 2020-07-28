package com.example.viyer.models;

import com.google.firebase.firestore.DocumentId;

import org.json.JSONArray;
import org.parceler.Parcel;

import java.util.List;

@Parcel
public class Product {
    private String title;
    private String description;
    private int price;
    private String uid;
    private Boolean locked;
    private List<String> likes;

    public List<String> getLikes() {
        return likes;
    }

    public Boolean getLocked() {
        return locked;
    }

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

    public int getPrice() {
        return price;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }
}
