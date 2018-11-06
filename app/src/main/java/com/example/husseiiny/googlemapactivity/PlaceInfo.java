package com.example.husseiiny.googlemapactivity;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

public class PlaceInfo {
    String placeName;
    String address;
    String phoneNumber;
    Uri website;
    float rating;

    public PlaceInfo(String placeName, String address,
                     String phoneNumber, Uri website , float rating) {
        this.placeName = placeName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.website = website;
        this.rating = rating;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public Uri getWebsite() {
        return website;
    }

    public float getRating() {
        return rating;
    }
}
