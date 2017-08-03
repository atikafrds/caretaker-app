package com.atikafrds.caretaker;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

import static com.atikafrds.caretaker.UserActivity.lat;
import static com.atikafrds.caretaker.UserActivity.lng;
import static com.atikafrds.caretaker.UserActivity.TAG;
import static com.atikafrds.caretaker.UserActivity.user_currentUserId;
import static com.atikafrds.caretaker.UserActivity.user_partnerId;

public class UserMapFragment extends Fragment {
    MapView mapView;
    private GoogleMap googleMap;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private ImageView bluetoothButton;

    private Geocoder geocoder;
    private List<Address> addresses;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothService mService = null;

    public static UserMapFragment newInstance() {
        UserMapFragment fragment = new UserMapFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.map_fragment, container, false);

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume(); // needed to get the map to display immediately

        bluetoothButton = (ImageView) view.findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        });

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                LatLng myLoc = new LatLng(lat, lng);

                try {
                    googleMap.setMyLocationEnabled(true);
                } catch (SecurityException e) {

                }

                googleMap.addMarker(new MarkerOptions().position(myLoc).title("Your Location"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(myLoc).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mService == null) {
            mService = new BluetoothService(getActivity(), mHandler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mService.start();
            }
        }
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
        if (mService != null) {
            mService.stop();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.equals("1")) {
                        Toast.makeText(getContext(), "'1' received. Sending SMS.", Toast.LENGTH_LONG).show();
//                        sendSMS();
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    mService = new BluetoothService(getActivity(), mHandler);
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), "Bluetooth was not enabled.",
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mService.connect(device, secure);
    }

    private void sendSMS() {

        final User currentUser = new User();
        final User currentPartner = new User();

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
//                        user_partnerPhoneNumber = data.child("phoneNumber").getValue().toString();
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

        if ((currentUser != null) && (currentPartner != null)) {
//            String url = "http://rest.nexmo.com/sms/json";
//            HashMap<String, String> params = new HashMap<String, String>();

            String loc = "";

            try {
                geocoder = new Geocoder(getContext(), Locale.getDefault());
                addresses = geocoder.getFromLocation(lat, lng, 1);
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

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notification");
            String key = databaseReference.push().getKey();
            Notification notification = new Notification(currentUser.getId(), currentUser.getId(),
                    currentUser.getFullname(), currentUser.getPhoneNumber(), currentUser.getPhoneNumber(),
                    currentUser.getLat(), currentUser.getLng(), loc, new Timestamp(System.currentTimeMillis()));
            databaseReference.child(key).setValue(notification);

//            params.put("api_key", "4dd9933d");
//            params.put("api_secret", "27c317f77c0787f2");
//            params.put("from", currentUser.getPhoneNumber());
//            params.put("to", currentPartner.getPhoneNumber());
//            params.put("text", "You got an alert from " + currentUser.getFullname() + ". Their" +
//                    "location is near " + loc + ".\nLat: " + currentUser.getLat() + ", Lng: " +
//                    currentUser.getLng());
//
//            final JsonObjectRequest jsObjRequest = new JsonObjectRequest
//                    (Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            Log.d(TAG, "Response: " + response.toString());
//                        }
//                    }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//
//                        }
//                    });
//            MySingleton.getInstance(getContext()).addToRequestQueue(jsObjRequest);
        }
    }
}