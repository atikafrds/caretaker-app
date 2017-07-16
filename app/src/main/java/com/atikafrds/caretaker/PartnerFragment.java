package com.atikafrds.caretaker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import static com.atikafrds.caretaker.CaretakerActivity.TAG;
import static com.atikafrds.caretaker.CaretakerActivity.isHavePartner;

/**
 * Created by t-atika.firdaus on 22/06/17.
 */

public class PartnerFragment extends Fragment implements View.OnClickListener {
    private DatabaseReference userDbReference, caretakerDbReference;
    private String partnerUserId, partnerName, partnerEmail, partnerPhoneNumber;
    private TextView partnerNameView, partnerEmailView, partnerPhoneNumberView;
    private ArrayList<User> userList;
    private ListView listView;

    public static PartnerFragment newInstance() {
        PartnerFragment fragment = new PartnerFragment();
        return fragment;
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.partner_fragment, container, false);

        userDbReference = FirebaseDatabase.getInstance().getReference("users");
        caretakerDbReference = FirebaseDatabase.getInstance().getReference("caretakers");
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            caretakerDbReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (isHavePartner) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            if (data.child("id").getValue().toString().equals(user.getUid())) {
                                partnerUserId = data.child("partnerId").getValue().toString();
                            }
                            break;
//                            Toast.makeText(getContext(), partnerUserId, Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(CaretakerActivity.TAG, "Failed to read database.", databaseError.toException());
                }
            });

            userDbReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (partnerUserId != null) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            User newUser = new User();
                            newUser.setFullname(data.child("fullname").getValue().toString());
                            newUser.setId(data.child("id").getValue().toString());
                            newUser.setPhoneNumber(data.child("phoneNumber").getValue().toString());
                            userList.add(newUser);

                            if (newUser.getId().equals(partnerUserId)) {
                                partnerNameView = (TextView) view.findViewById(R.id.partnerFullname);
                                partnerEmailView = (TextView) view.findViewById(R.id.partnerEmail);
                                partnerPhoneNumberView = (TextView) view.findViewById(R.id.partnerPhoneNumber);

                                partnerName = data.child("fullname").getValue().toString();
                                partnerEmail = data.child("email").getValue().toString();
                                partnerPhoneNumber = data.child("phoneNumber").getValue().toString();

                                partnerNameView.setText(partnerName);
                                partnerEmailView.setText(partnerEmail);
                                partnerPhoneNumberView.setText(partnerPhoneNumber);

                                break;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(CaretakerActivity.TAG, "Failed to read database.", databaseError.toException());
                }
            });
        }

        Log.d(TAG, userList.toString());
        Log.d(TAG, partnerUserId);

//        PartnerListAdapter adapter = new PartnerListAdapter(getContext(), R.layout.partner_list_item, userList);
//        listView = (ListView) view.findViewById(R.id.select_partner_list_view);
//        listView.setAdapter(adapter);

//        if (!isHavePartner) {
////            view.findViewById(R.id.select_partner_list_view).setVisibility(View.VISIBLE);
//            view.findViewById(R.id.section1).setVisibility(View.GONE);
//            view.findViewById(R.id.section2).setVisibility(View.GONE);
//        }

        return view;
    }

    @Override
    public void onClick(View view) {

    }
}
