package com.atikafrds.caretaker;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UserActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static double lat, lng;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private static final String SELECTED_ITEM = "arg_selected_item";
    public static final String TAG = UserActivity.class.getSimpleName();
    public static String user_partnerId, user_currentUserId;
    public static UserRole user_userRole;

    private BottomNavigationView bottomNavigation;
    private int mSelectedItem;
    private User currentUser, currentPartner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user_partnerId = getIntent().getStringExtra("partnerId");
        user_currentUserId = getIntent().getStringExtra("currentUserId");
        user_userRole = getIntent().getStringExtra("userRole").equals("DEVICE_USER") ? UserRole.DEVICE_USER : UserRole.CARETAKER;

        setContentView(R.layout.activity_user);

        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return true;
            }
        });

        MenuItem selectedItem;
        if (savedInstanceState != null) {
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM, 0);
            selectedItem = bottomNavigation.getMenu().findItem(mSelectedItem);
        } else {
            selectedItem = bottomNavigation.getMenu().getItem(0);
        }
        selectFragment(selectedItem);

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.
                        ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } else {
            refreshLocation(location);
        }
    }

    private void refreshLocation(final Location location) {
        Log.d(TAG, location.toString());

        currentUser = new User();
        currentPartner = new User();
        lat = location.getLatitude();
        lng = location.getLongitude();

        FirebaseDatabase.getInstance().getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.child("id").getValue().toString().equals(user_currentUserId)) {
                        currentUser.setId(data.child("id").getValue().toString());
                        currentUser.setFullname(data.child("fullname").getValue().toString());
                        currentUser.setEmail(data.child("email").getValue().toString());
                        currentUser.setPhoneNumber(data.child("phoneNumber").getValue().toString());
                        currentUser.setPartner(data.child("partnerId").getValue().toString());
                        currentUser.setLat(lat);
                        currentUser.setLng(lng);

                        FirebaseDatabase.getInstance().getReference("users").child(data.getKey()).setValue(currentUser);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("caretakers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.child("id").getValue().toString().equals(user_partnerId)) {
                        currentPartner.setId(data.child("id").getValue().toString());
                        currentPartner.setFullname(data.child("fullname").getValue().toString());
                        currentPartner.setEmail(data.child("email").getValue().toString());
                        currentPartner.setPhoneNumber(data.child("phoneNumber").getValue().toString());
                        currentPartner.setPartner(data.child("partnerId").getValue().toString());
                        currentPartner.setLat(Double.parseDouble(data.child("lat").getValue().toString()));
                        currentPartner.setLng(Double.parseDouble(data.child("lng").getValue().toString()));
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
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection to Google API suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        refreshLocation(location);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM, 0);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        MenuItem homeItem = bottomNavigation.getMenu().getItem(0);
        if (mSelectedItem != homeItem.getItemId()) {
            selectFragment(homeItem);
        } else {
            super.onBackPressed();
        }
    }

    private void selectFragment(MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.menu_home:
                fragment = UserMapFragment.newInstance();
                break;
            case R.id.menu_partner:
                fragment = UserPartnerFragment.newInstance();
                break;
            case R.id.menu_profile:
                fragment = UserProfileFragment.newInstance();
                break;
        }

        // uncheck the other items.
        for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
            MenuItem menuItem = bottomNavigation.getMenu().getItem(i);
            menuItem.setChecked(menuItem.getItemId() == mSelectedItem);
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, fragment);
            ft.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}