package com.atikafrds.caretaker;

import android.support.annotation.Nullable;

/**
 * Created by t-atika.firdaus on 22/06/17.
 */

public class User {
    private String id;
    private String fullname;
    private String email;
    private String phoneNumber;
    private String partnerId;
    private double lat;
    private double lng;

    public User() {
        this.id = "";
        this.fullname = "";
        this.email = "";
        this.phoneNumber = "";
        this.partnerId = "";
        this.lat = 0;
        this.lng = 0;
    }

    public User(String id, String fullname, String email, String phoneNumber, String partnerId,
            double lat, double lng) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.partnerId = partnerId;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPartner(String partnerId) {
        this.partnerId = partnerId;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
