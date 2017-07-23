package com.atikafrds.caretaker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.atikafrds.caretaker.CaretakerActivity.currentUserId;
import static com.atikafrds.caretaker.CaretakerActivity.partnerId;
import static com.atikafrds.caretaker.CaretakerActivity.userRole;
import static com.atikafrds.caretaker.PartnerFragment.partnerEmailView;
import static com.atikafrds.caretaker.PartnerFragment.partnerNameView;
import static com.atikafrds.caretaker.PartnerFragment.partnerPhoneNumberView;

public class PartnerListAdapter extends ArrayAdapter<User> {
    private ArrayList<User> userData;
    private Context context;
    private int resource;
    private View view;
    private DatabaseReference userReference, partnerReference;
    private User currentUser;
    private String currentUserKey = "";

    public PartnerListAdapter(Context context, int resource, ArrayList<User> userData) {
        super(context, resource, userData);
        this.context = context;
        this.resource = resource;
        this.userData = userData;

        userReference = FirebaseDatabase.getInstance().getReference(userRole == UserRole.
                DEVICE_USER ? "users" : "caretakers");
        partnerReference = FirebaseDatabase.getInstance().getReference(userRole == UserRole.
                DEVICE_USER ? "caretakers" : "users");

        currentUser = new User();

        userReference.addValueEventListener(new ValueEventListener() {
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
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(resource, parent, false);

        final User currentPartner = userData.get(position);

        TextView partnerName = (TextView) view.findViewById(R.id.partnerNameinList);
        TextView partnerPhoneNumber = (TextView) view.findViewById(R.id.partnerPhoneNumberinList);
        partnerName.setText(currentPartner.getFullname());
        partnerPhoneNumber.setText(currentPartner.getPhoneNumber());

        Button selectPartner = (Button) view.findViewById(R.id.selectPartner);
        selectPartner.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final User currPartner = userData.get(position);

                partnerId = currPartner.getId();

                currPartner.setPartner(currentUser.getId());
                partnerReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            if (data.child("id").getValue().toString().equals(partnerId)) {
//                                currentPartnerKey = data.getKey();
                                partnerNameView.setText(data.child("fullname").getValue().toString());
                                partnerEmailView.setText(data.child("email").getValue().toString());
                                partnerPhoneNumberView.setText(data.child("phoneNumber").getValue().toString());

                                FirebaseDatabase.getInstance().getReference(userRole == UserRole.
                                        DEVICE_USER ? "caretakers" : "users").child(data.getKey()).
                                        setValue(currPartner);

                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("TAG", databaseError.toString());
                    }
                });

                currentUser.setPartner(currPartner.getId());
                if (!currentUserKey.isEmpty()) {
                    FirebaseDatabase.getInstance().getReference(userRole == UserRole.
                            DEVICE_USER ? "users" : "caretakers").child(currentUserKey).
                            setValue(currentUser);
                }

                ((View) parent.getParent()).findViewById(R.id.section1).setVisibility(View.VISIBLE);
                ((View) parent.getParent()).findViewById(R.id.section2).setVisibility(View.VISIBLE);
                parent.setVisibility(View.GONE);
            }
        });

        return view;
    }
}