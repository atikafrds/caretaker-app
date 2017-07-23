package com.atikafrds.caretaker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class UserActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static double lat, lng;
    public static String user_partnerPhoneNumber = "";

    Handler h;

    final int RECEIVE_MESSAGE = 1;        // Status  for Handler
    public String message = "";
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "00:15:FF:F2:19:5F";

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Geocoder geocoder;
    private List<Address> addresses;

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

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECEIVE_MESSAGE:                                                   // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
                        if (endOfLineIndex > 0) {                                            // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);               // extract string
                            sb.delete(0, sb.length());                                      // and clear
                            message = sbprint;
//                            Toast.makeText(getApplicationContext(), sbprint, Toast.LENGTH_SHORT).show();
                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            };
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
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
        String str = location.getLatitude() + "\n" + location.getLongitude();
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();

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

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //   UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Fatal Error: In onResume() and socket create failed: "
                + e.getMessage() + ".", Toast.LENGTH_LONG).show();
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "Connection ok.");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                Toast.makeText(getApplicationContext(), "Fatal Error: In onResume() and unable to" +
                    "close socket during connection failure: " + e2.getMessage() + ".", Toast.LENGTH_LONG).show();
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "Creating Socket...");

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }

        try {
            btSocket.close();
        } catch (IOException e2) {
            Toast.makeText(getApplicationContext(), "Fatal Error: In onPause() and failed to close socket."
                + e2.getMessage() + ".", Toast.LENGTH_LONG).show();
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "Fatal Error: Bluetooth not support", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth is enabled.");
            } else {
                // Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                    if (message.equals("1") && (currentPartner != null)) {
                        sendSMS(currentUser, currentPartner);
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    private void sendSMS(User sender, User receiver) {
        if (currentUser != null) {
            String url = "http://rest.nexmo.com/sms/json";
            HashMap<String, String> params = new HashMap<String, String>();

            String loc = "";

            try {
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
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
            Notification notification = new Notification(sender.getId(), receiver.getId(),
                sender.getFullname(), sender.getPhoneNumber(), receiver.getPhoneNumber(),
                    sender.getLat(), sender.getLng(), loc, new Timestamp(System.currentTimeMillis()));
            databaseReference.child(key).setValue(notification);

            params.put("api_key", "4dd9933d");
            params.put("api_secret", "27c317f77c0787f2");
            params.put("from", sender.getPhoneNumber());
            params.put("to", receiver.getPhoneNumber());
            params.put("text", "You got an alert from " + sender.getFullname() + ". Their" +
                "location is near " + loc + ".\nLat: " + sender.getLat() + ", Lng: " +
                sender.getLng());

            final JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
            MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
        }
    }
}