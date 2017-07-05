package com.atikafrds.caretaker;

import android.support.annotation.Nullable;

/**
 * Created by t-atika.firdaus on 22/06/17.
 */

public class User {
    private String fullname;
    private String email;
    private String phoneNumber;
    private String partnerId;
    private double lat;
    private double lng;

    public User() {

    }

    public User(String fullname, String email, String phoneNumber, @Nullable String partnerId,
            double lat, double lng) {
        this.fullname = fullname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.partnerId = partnerId;
        this.lat = lat;
        this.lng = lng;
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
