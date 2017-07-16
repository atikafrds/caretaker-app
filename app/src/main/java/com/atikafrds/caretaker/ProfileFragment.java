package com.atikafrds.caretaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.atikafrds.caretaker.LoginActivity.role;

/**
 * Created by t-atika.firdaus on 22/06/17.
 */

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private TextView profileFullname, profileEmail, profilePhoneNumber;
    private Button logoutButton;
    private String userRole;

    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference databaseReference;

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        profileFullname = (TextView) view.findViewById(R.id.profileFullname);
        profileEmail = (TextView) view.findViewById(R.id.profileEmail);
        profilePhoneNumber = (TextView) view.findViewById(R.id.profilePhoneNumber);
        logoutButton = (Button) view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(this);

//        authListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            userRole = sharedPref.getString(role, "");

            if (userRole.equals(UserRole.DEVICE_USER.toString())) {
                databaseReference = FirebaseDatabase.getInstance().getReference("users");
            } else {
                databaseReference = FirebaseDatabase.getInstance().getReference("caretakers");
            }

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.child("id").getValue().toString().equals(user.getUid())) {
                            profileFullname.setText(data.child("fullname").getValue().toString());
                            profileEmail.setText(data.child("email").getValue().toString());
                            profilePhoneNumber.setText(data.child("phoneNumber").getValue().toString());
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(CaretakerActivity.TAG, "Failed to read database.", databaseError.toException());
                }
            });
        } else {
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view == logoutButton) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        }
    }
}
