package com.atikafrds.caretaker;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by t-atika.firdaus on 15/07/17.
 */

public class Notification {
    String userId;
    String caretakerId;
    String userName;
    String userPhoneNumber;
    String caretakerPhoneNumber;
    double lat;
    double lng;
    String knownAddress;
    Timestamp timestamp;

    public Notification() {

    }

    public Notification(String userId, String caretakerId, String userName, String userPhoneNumber,
                        String caretakerPhoneNumber, double lat, double lng, String knownAddress,
                        Timestamp timestamp) {
        this.userId = userId;
        this.caretakerId = caretakerId;
        this.userName = userName;
        this.userPhoneNumber = userPhoneNumber;
        this.caretakerPhoneNumber = caretakerPhoneNumber;
        this.lat = lat;
        this.lng = lng;
        this.knownAddress = knownAddress;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCaretakerId() {
        return caretakerId;
    }

    public void setCaretakerId(String caretakerId) {
        this.caretakerId = caretakerId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhoneNumber() {
            return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getCaretakerPhoneNumber() {
        return caretakerPhoneNumber;
    }

    public void setCaretakerPhoneNumber(String caretakerPhoneNumber) {
        this.caretakerPhoneNumber = caretakerPhoneNumber;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getKnownAddress() {
        return knownAddress;
    }

    public void setKnownAddress(String knownAddress) {
        this.knownAddress = knownAddress;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

//    public int compareTo(Notification notification) {
//
//        Timestamp compareQuantity = ((Notification) notification).getTimestamp();
//
//        //ascending order
//        return this.timestamp - compareQuantity;
//
//        //descending order
//        //return compareQuantity - this.quantity;
//
//    }
}
