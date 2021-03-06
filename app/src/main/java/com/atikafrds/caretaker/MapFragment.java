package com.atikafrds.caretaker;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

import static com.atikafrds.caretaker.CaretakerActivity.partnerId;

public class MapFragment extends Fragment {
    private DatabaseReference userDbReference;
    private String partnerName;
    private double partnerLat, partnerLng;

    public static final String TAG = CaretakerActivity.class.getSimpleName();
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 200;

    MapView mapView;
    private GoogleMap googleMap;
    Geocoder geocoder;
    List<Address> addresses;

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.map_fragment, container, false);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userDbReference = FirebaseDatabase.getInstance().getReference("users");
        if (user != null) {
            userDbReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!partnerId.isEmpty()) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            if (data.child("id").getValue().toString().equals(partnerId)) {
                                partnerLat = Double.parseDouble(data.child("lat").getValue().toString());
                                partnerLng = Double.parseDouble(data.child("lng").getValue().toString());
                                partnerName = data.child("fullname").getValue().toString();
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

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                try {
                    googleMap.setMyLocationEnabled(true);
                } catch (SecurityException e) {

                }

                String loc = "";
                // For dropping a marker at a point on the Map
                LatLng partnerLoc = new LatLng(partnerLat, partnerLng);
                try {
                    geocoder = new Geocoder(getContext(), Locale.getDefault());
                    addresses = geocoder.getFromLocation(partnerLat, partnerLng, 1);
                    String address, city;

                    if (addresses.get(0).getAddressLine(0) != null) {
                        address = addresses.get(0).getAddressLine(0);
                    } else {
                        address = "";
                    }
                    if (addresses.get(0).getLocality() != null) {
                        city = addresses.get(0).getLocality();
                    } else {
                        city = "";
                    }
                    loc = address + ", " + city;
                } catch (Exception e) {
                    Log.e("Geocoder failed: ", e.toString());
                }

                googleMap.addMarker(new MarkerOptions().position(partnerLoc).title(partnerName).snippet(loc));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(partnerLoc).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}