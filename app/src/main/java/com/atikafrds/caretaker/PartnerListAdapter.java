package com.atikafrds.caretaker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.atikafrds.caretaker.CaretakerActivity.currentUserId;
import static com.atikafrds.caretaker.CaretakerActivity.partnerId;
import static com.atikafrds.caretaker.PartnerFragment.partnerEmailView;
import static com.atikafrds.caretaker.PartnerFragment.partnerNameView;
import static com.atikafrds.caretaker.PartnerFragment.partnerPhoneNumberView;

/**
 * Created by t-atika.firdaus on 06/07/17.
 */

public class PartnerListAdapter extends ArrayAdapter<User> {
    private ArrayList<User> userData;
    private Context context;
    private int resource;
    private View view;
    private DatabaseReference userReference, currentUserReference, caretakerReference;
    public static DatabaseReference currentCaretakerReference;
    private User currentCaretaker;

    public PartnerListAdapter(Context context, int resource, ArrayList<User> userData) {
        super(context, resource, userData);
        this.context = context;
        this.resource = resource;
        this.userData = userData;

        currentCaretaker = new User();

        userReference = FirebaseDatabase.getInstance().getReference("users");
        caretakerReference = FirebaseDatabase.getInstance().getReference("caretakers");

        caretakerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.child("id").getValue().toString().equals(currentUserId)) {
                        currentCaretakerReference = data.getRef();
                        currentCaretaker.setId(data.child("id").getValue().toString());
                        currentCaretaker.setFullname(data.child("fullname").getValue().toString());
                        currentCaretaker.setEmail(data.child("email").getValue().toString());
                        currentCaretaker.setPhoneNumber(data.child("phoneNumber").getValue().toString());
                        currentCaretaker.setLat(Double.parseDouble(data.child("lat").getValue().toString()));
                        currentCaretaker.setLng(Double.parseDouble(data.child("lng").getValue().toString()));
                        currentCaretaker.setPartner(data.child("partnerId").getValue().toString());
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

        final User currentUser = userData.get(position);

        TextView partnerName = (TextView) view.findViewById(R.id.partnerNameinList);
        TextView partnerPhoneNumber = (TextView) view.findViewById(R.id.partnerPhoneNumberinList);
        partnerName.setText(currentUser.getFullname());
        partnerPhoneNumber.setText(currentUser.getPhoneNumber());
        Button selectPartner = (Button) view.findViewById(R.id.selectPartner);
        selectPartner.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                User currUser = userData.get(position);

                currentCaretaker.setPartner(currUser.getId());
                currUser.setPartner(currentCaretaker.getId());
                FirebaseDatabase.getInstance().getReference("caretakers").child(currentCaretakerReference.getKey()).setValue(currentCaretaker);

                partnerId = currUser.getId();
                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            if (data.child("id").getValue().toString().equals(partnerId)) {
                                currentUserReference = data.getRef();
                                partnerNameView.setText(data.child("fullname").getValue().toString());
                                partnerEmailView.setText(data.child("email").getValue().toString());
                                partnerPhoneNumberView.setText(data.child("phoneNumber").getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

//                FirebaseDatabase.getInstance().getReference("users").child(currentUserReference.getKey()).setValue(currUser);

                ((View) parent.getParent()).findViewById(R.id.section1).setVisibility(View.VISIBLE);
                ((View) parent.getParent()).findViewById(R.id.section2).setVisibility(View.VISIBLE);
                parent.setVisibility(View.GONE);
            }
        });

        return view;
    }
}