package com.atikafrds.caretaker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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

import java.util.ArrayList;

import static com.atikafrds.caretaker.CaretakerActivity.currentUserId;
import static com.atikafrds.caretaker.CaretakerActivity.partnerId;
import static com.atikafrds.caretaker.CaretakerActivity.userRole;

public class PartnerFragment extends Fragment implements View.OnClickListener {
    private DatabaseReference dbReference;
    private String partnerName, partnerEmail, partnerPhoneNumber;
    public static TextView partnerNameView, partnerEmailView, partnerPhoneNumberView;
    private ArrayList<User> userList;
    private ListView listView;
    private Button changePartner;
    private String currentUserKey, currentPartnerKey = "";
    private User currentUser = new User(), currentPartner = new User();

    public static PartnerFragment newInstance() {
        PartnerFragment fragment = new PartnerFragment();
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.partner_fragment, container, false);
        listView = (ListView) view.findViewById(R.id.select_partner_list_view);
        partnerNameView = (TextView) view.findViewById(R.id.partnerFullname);
        partnerEmailView = (TextView) view.findViewById(R.id.partnerEmail);
        partnerPhoneNumberView = (TextView) view.findViewById(R.id.partnerPhoneNumber);

        FirebaseDatabase.getInstance().getReference(userRole == UserRole.DEVICE_USER ? "users"
            : "caretakers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.child("id").getValue().toString().equals(currentUserId)) {
                        currentUserKey = data.getKey();
                        currentUser.setId(data.child("id").getValue().toString());
                        currentUser.setFullname(data.child("fullname").getValue().toString());
                        currentUser.setEmail(data.child("email").getValue().toString());
                        currentUser.setPhoneNumber(data.child("phoneNumber").getValue().toString());
                        currentUser.setLat(Double.parseDouble(data.child("lat").getValue().toString()));
                        currentUser.setLng(Double.parseDouble(data.child("lng").getValue().toString()));
                        currentUser.setPartner(data.child("partnerId").getValue().toString());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference(userRole == UserRole.DEVICE_USER ? "caretakers"
                : "users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (!partnerId.isEmpty() && data.child("id").getValue().toString().equals(partnerId)) {
                        currentPartnerKey = data.getKey();
                        currentPartner.setId(data.child("id").getValue().toString());
                        currentPartner.setFullname(data.child("fullname").getValue().toString());
                        currentPartner.setEmail(data.child("email").getValue().toString());
                        currentPartner.setPhoneNumber(data.child("phoneNumber").getValue().toString());
                        currentPartner.setLat(Double.parseDouble(data.child("lat").getValue().toString()));
                        currentPartner.setLng(Double.parseDouble(data.child("lng").getValue().toString()));
                        currentPartner.setPartner(data.child("partnerId").getValue().toString());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        changePartner = (Button) view.findViewById(R.id.changePartner);
        changePartner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                partnerId = "";

                currentUser.setPartner("");
                if (!currentUserKey.isEmpty()) {
                    FirebaseDatabase.getInstance().getReference(userRole == UserRole.DEVICE_USER ? "users"
                        : "caretakers").child(currentUserKey).setValue(currentUser);
                }

                currentPartner.setPartner("");
                if (!currentPartnerKey.isEmpty()) {
                    FirebaseDatabase.getInstance().getReference(userRole == UserRole.DEVICE_USER ? "caretakers"
                            : "users").child(currentPartnerKey).setValue(currentPartner);
                }

                view.findViewById(R.id.select_partner_list_view).setVisibility(View.VISIBLE);
                view.findViewById(R.id.section1).setVisibility(View.GONE);
                view.findViewById(R.id.section2).setVisibility(View.GONE);
            }
        });

        if (userRole == UserRole.DEVICE_USER) {
            dbReference = FirebaseDatabase.getInstance().getReference("caretakers");
        } else {
            dbReference = FirebaseDatabase.getInstance().getReference("users");
        }

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            dbReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userList = new ArrayList<>();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        User newUser = new User();
                        newUser.setFullname(data.child("fullname").getValue().toString());
                        newUser.setId(data.child("id").getValue().toString());
                        newUser.setPhoneNumber(data.child("phoneNumber").getValue().toString());
                        newUser.setEmail(data.child("email").getValue().toString());
                        newUser.setPartner(data.child("partnerId").getValue().toString());
                        newUser.setLat(Double.parseDouble(data.child("lat").getValue().toString()));
                        newUser.setLng(Double.parseDouble(data.child("lng").getValue().toString()));
                        userList.add(newUser);

                        if (newUser.getId().equals(partnerId)) {
                            partnerName = data.child("fullname").getValue().toString();
                            partnerEmail = data.child("email").getValue().toString();
                            partnerPhoneNumber = data.child("phoneNumber").getValue().toString();

                            partnerNameView.setText(partnerName);
                            partnerEmailView.setText(partnerEmail);
                            partnerPhoneNumberView.setText(partnerPhoneNumber);
                        }
                    }
                    if (!userList.isEmpty()) {
                        PartnerListAdapter adapter = new PartnerListAdapter(getContext(), R.layout.partner_list_item, userList);
                        listView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(CaretakerActivity.TAG, "Failed to read database.", databaseError.toException());
                }
            });
        }

        if (partnerId.isEmpty()) {
            view.findViewById(R.id.select_partner_list_view).setVisibility(View.VISIBLE);
            view.findViewById(R.id.section1).setVisibility(View.GONE);
            view.findViewById(R.id.section2).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.select_partner_list_view).setVisibility(View.GONE);
            view.findViewById(R.id.section1).setVisibility(View.VISIBLE);
            view.findViewById(R.id.section2).setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onClick(View view) {

    }
}
