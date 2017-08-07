package com.atikafrds.caretaker;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by t-atika.firdaus on 22/06/17.
 */

public class NotificationFragment extends Fragment {
    DatabaseReference notifReference;
    NotificationAdapter notificationAdapter;
    ListView listView;

    public static NotificationFragment newInstance() {
        NotificationFragment fragment = new NotificationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notification_fragment, container, false);
        listView = (ListView) view.findViewById(R.id.listView);

        notifReference = FirebaseDatabase.getInstance().getReference("notification");
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final ArrayList<Notification> notifications = new ArrayList<>();

        if (user != null) {
            notifReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.child("caretakerId").getValue().toString().equals(user.getUid())) {
                            Notification notif = new Notification();
                            notif.setUserId(data.child("userId").getValue().toString());
                            notif.setCaretakerId(user.getUid());
                            notif.setUserName(data.child("userName").getValue().toString());
                            notif.setUserPhoneNumber(data.child("userPhoneNumber").getValue().toString());
                            notif.setCaretakerPhoneNumber(data.child("caretakerPhoneNumber").getValue().toString());
                            notif.setKnownAddress(data.child("knownAddress").getValue().toString());
                            notif.setLat(Double.parseDouble(data.child("lat").getValue().toString()));
                            notif.setLng(Double.parseDouble(data.child("lng").getValue().toString()));
                            notif.setTimestamp(new Timestamp(Long.parseLong(data.child("timestamp").child("time").getValue().toString())));
                            notifications.add(notif);
                        }
                    }
                    notificationAdapter = new NotificationAdapter(getActivity(), R.layout.notification_list_item, notifications);
                    listView.setAdapter(notificationAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(CaretakerActivity.TAG, "Failed to read database.", databaseError.toException());
                }
            });
        }

        return view;
    }
}
