package com.atikafrds.caretaker;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by t-atika.firdaus on 03/07/17.
 */

public class FirebaseHelper {

    DatabaseReference databaseReference;
    Boolean saved = null;
    ArrayList<User> users = new ArrayList<>();

    public FirebaseHelper(DatabaseReference db) {
        this.databaseReference = db;
    }

    public Boolean save(User user) {
        if (user == null) {
            saved = false;
        } else {
            try {
                databaseReference.child("Spacecraft").push().setValue(user);
                saved = true;
            } catch (DatabaseException e) {
                e.printStackTrace();
                saved = false;
            }
        }

        return saved;
    }

    public ArrayList<User> retrieve() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return users;
    }

    private void fetchData(DataSnapshot dataSnapshot) {
        users.clear();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            User user = new User(ds.child("fullname").getValue().toString(), ds.child("email").
                    getValue().toString(), ds.child("phoneNumber").getValue().toString(), null,
                    Double.parseDouble(ds.child("lat").getValue().toString()), Double.parseDouble(
                    ds.child("lng").getValue().toString()));
            users.add(user);
        }
    }
}