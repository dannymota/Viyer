package com.example.viyer.models;

import android.location.Location;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Offer {
    private String buyerUid;
    private int offer;
    private String productId;
    private boolean status;
    private boolean response;
    private Date date;
    private String address;
    private Boolean view;

    public Boolean getView() {
        return view;
    }

    public String getAddress() {
        return address;
    }

    public Date getDate() {
        return date;
    }

    public boolean isResponse() {
        return response;
    }

    public boolean isStatus() {
        return status;
    }

    private GeoPoint location;

    public GeoPoint getLocation() {
        return location;
    }

    @DocumentId
    private String documentId;

    public Offer() {
    }

    public String getBuyerUid() {
        return buyerUid;
    }

    public int getOffer() {
        return offer;
    }

    public String getProductId() {
        return productId;
    }

    public String getDocumentId() {
        return documentId;
    }
}
