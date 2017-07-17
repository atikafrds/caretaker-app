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

import static com.atikafrds.caretaker.CaretakerActivity.partnerId;
import static com.atikafrds.caretaker.PartnerListAdapter.currentCaretakerReference;

/**
 * Created by t-atika.firdaus on 22/06/17.
 */

public class PartnerFragment extends Fragment implements View.OnClickListener {
    private DatabaseReference userDbReference;
    private String partnerName, partnerEmail, partnerPhoneNumber;
    public static TextView partnerNameView, partnerEmailView, partnerPhoneNumberView;
    private ArrayList<User> userList = new ArrayList<>();
    private ListView listView;
    private Button changePartner;

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
        changePartner = (Button) view.findViewById(R.id.changePartner);
        changePartner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                partnerId = "";
                view.findViewById(R.id.select_partner_list_view).setVisibility(View.VISIBLE);
                view.findViewById(R.id.section1).setVisibility(View.GONE);
                view.findViewById(R.id.section2).setVisibility(View.GONE);
            }
        });

        userDbReference = FirebaseDatabase.getInstance().getReference("users");
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            userDbReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
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
                    PartnerListAdapter adapter = new PartnerListAdapter(getContext(), R.layout.partner_list_item, userList);
                    listView.setAdapter(adapter);
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
