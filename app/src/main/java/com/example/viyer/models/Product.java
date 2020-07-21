package com.example.viyer.models;

import org.json.JSONArray;
import org.parceler.Parcel;

import java.util.List;

@Parcel
public class Product {
    String title;
    String description;
    String price;
    List<String> photoUrls;

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
