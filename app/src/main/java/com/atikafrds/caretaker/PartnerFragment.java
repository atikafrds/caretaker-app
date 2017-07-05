package com.atikafrds.caretaker;

import android.app.ListFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by t-atika.firdaus on 22/06/17.
 */

public class PartnerFragment extends Fragment implements View.OnClickListener {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDbReference, caretakerDbReference;
    private String partnerUserId, partnerName, partnerEmail, partnerPhoneNumber;
    private TextView partnerNameView, partnerEmailView, partnerPhoneNumberView;

    public static PartnerFragment newInstance() {
        PartnerFragment fragment = new PartnerFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
//                R.array.Planets, R.layout.user_list_item);
//        setListAdapter(adapter);
//        getListView().setOnItemClickListener(this);
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
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.child("id").getValue().toString().equals(user.getUid())) {
                            partnerUserId = data.child("partnerId").getValue().toString();
//                            Toast.makeText(getContext(), partnerUserId, Toast.LENGTH_LONG).show();
                            break;
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
                            if (data.child("id").getValue().toString().equals(partnerUserId)) {
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

        return view;
    }

    @Override
    public void onClick(View view) {

    }
}
