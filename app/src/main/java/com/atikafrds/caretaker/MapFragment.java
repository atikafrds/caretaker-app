package com.atikafrds.caretaker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
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

import static com.atikafrds.caretaker.CaretakerActivity.currentUserId;
import static com.atikafrds.caretaker.CaretakerActivity.partnerId;
import static com.atikafrds.caretaker.CaretakerActivity.userRole;

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

        ImageView imageView = (ImageView) view.findViewById(R.id.bluetoothButton);
        imageView.setVisibility(View.GONE);

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
//                                    Toast.makeText(getContext(), partnerLat + " " + partnerLng, Toast.LENGTH_SHORT).show();
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

//        if (userRole == UserRole.DEVICE_USER) {
//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                checkLocationPermission();
//            }
//        }

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (userRole == UserRole.DEVICE_USER) {
//            googleApiClient = new GoogleApiClient.Builder(getContext())
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//
//            if (checkGooglePlayServices()) {
//                buildGoogleApiClient();
//            }
//            createLocationRequest();
//        }
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

//    private synchronized void buildGoogleApiClient() {
//        googleApiClient = new GoogleApiClient.Builder(getContext())
//                .addConnectionCallbacks(this).addApi(LocationServices.API)
//                .build();
//    }

    private boolean checkGooglePlayServices() {
        int checkGooglePlayServices = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getContext());
        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices,
                    getActivity(), REQUEST_CODE_RECOVER_PLAY_SERVICES).show();

            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (userRole == UserRole.DEVICE_USER) {
//            if (requestCode == REQUEST_CODE_RECOVER_PLAY_SERVICES) {
//                if (resultCode == Activity.RESULT_OK) {
//                    if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
//                        googleApiClient.connect();
//                    }
//                } else if (resultCode == Activity.RESULT_CANCELED) {
////                    Toast.makeText(getContext(), "Google Play Services must be installed.",
////                            Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
    }

//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        if (userRole == UserRole.DEVICE_USER) {
//            if (googleApiClient.isConnected()) {
//                Log.d("map", "Google_Api_Client: It was connected on (onConnected) function, working as it should.");
//            } else {
//                Log.d("map failed", "Google_Api_Client: It was NOT connected on (onConnected) function, It is definetly bugged.");
//            }
//
//            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
//                startLocationUpdates();
//            }
//
//            if (lastLocation != null) {
////                Toast.makeText(getContext(), "Latitude: " + lastLocation.getLatitude() + "," +
////                        "Longitude: " + lastLocation.getLongitude(), Toast.LENGTH_LONG).show();
//                databaseReference.child("lat").setValue(lastLocation.getLatitude());
//                databaseReference.child("lng").setValue(lastLocation.getLongitude());
//            }
//        }
//    }

//    protected void createLocationRequest() {
//        locationRequest = new LocationRequest();
//        locationRequest.setInterval(20000);
//        locationRequest.setFastestInterval(5000);
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//    }

//    protected void startLocationUpdates() {
//        try {
//            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
//        } catch (SecurityException e) {
//            Log.e(TAG, "Couldn't request location update", e);
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        Log.d(TAG, "Connection to Google API suspended");
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        if (userRole == UserRole.DEVICE_USER) {
//            lastLocation = location;
////            Toast.makeText(getContext(), "Latitude: " + lastLocation.getLatitude() + "," +
////                    "Longitude:" + lastLocation.getLongitude(), Toast.LENGTH_LONG).show();
//        }
//    }

//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//    }

    protected void stopLocationUpdates() {
//        if (googleApiClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (googleApiClient != null) {
//            googleApiClient.disconnect();
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (userRole == UserRole.DEVICE_USER) {
//            googleApiClient.connect();
//        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (userRole == UserRole.DEVICE_USER) {
//            switch (requestCode) {
//                case MY_PERMISSIONS_REQUEST_LOCATION: {
//                    // If request is cancelled, the result arrays are empty.
//                    if (grantResults.length > 0
//                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        // permission was granted, yay! Do the
//                        // contacts-related task you need to do.
//                        if (ContextCompat.checkSelfPermission(getActivity(),
//                                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                                == PackageManager.PERMISSION_GRANTED) {
//
//                            if (googleApiClient == null) {
//                                buildGoogleApiClient();
//                            }
//                        }
//
//                    } else {
//                        // permission denied, boo! Disable the
//                        // functionality that depends on this permission.
////                        Toast.makeText(getContext(), "permission denied", Toast.LENGTH_LONG).show();
//                    }
//                    return;
//                }
//            }
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
//        if (userRole == UserRole.DEVICE_USER) {
//            startLocationUpdates();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
//        if (userRole == UserRole.DEVICE_USER) {
//            stopLocationUpdates();
//        }
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