package com.atikafrds.caretaker;

import android.support.annotation.Nullable;

/**
 * Created by t-atika.firdaus on 22/06/17.
 */

public class User {
    private String fullname;
    private String email;
    private String phoneNumber;
    private User partner;

    public User() {

    }

    public User(String fullname, String email, String phoneNumber, @Nullable User partner) {
        this.fullname = fullname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.partner = partner;
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

    public User getPartner() {
        return partner;
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

    public void setPartner(User partner) {
        this.partner = partner;
    }
}
