package com.atikafrds.caretaker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static com.atikafrds.caretaker.DeviceListActivity.EXTRA_DEVICE_ADDRESS;
import static com.atikafrds.caretaker.UserActivity.currentPartner;
import static com.atikafrds.caretaker.UserActivity.currentUser;
import static com.atikafrds.caretaker.UserActivity.lat;
import static com.atikafrds.caretaker.UserActivity.lng;


public class ArduinoActivity extends Activity {
    private Set<BluetoothDevice> pairedDevices;
    ListView devicelist;
    int handlerState = 1;
    StringBuilder recDataString = new StringBuilder();
    Handler bluetoothIn;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;

    private Geocoder geocoder;
    private List<Address> addresses;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino);
        setResult(Activity.RESULT_CANCELED);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth device not available", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (!myBluetooth.isEnabled()) {
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon, 1);
            }
        }

        Button btnPaired = (Button) findViewById(R.id.button_pair);
        devicelist = (ListView)findViewById(R.id.listView);

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });
    }

    private void pairedDevicesList() {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0) {
            for(BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No paired bluetooth devices found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3) {
            String info = ((TextView) v).getText().toString();
            address = info.substring(info.length() - 17);

            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            setResult(Activity.RESULT_OK, intent);
            startBluetooth();
        }
    };

    private void msg(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private void startBluetooth() {
        new ConnectBT().execute();

        //receive sensor
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (true) {
                    String readMessage = (String) msg.obj;
                    Log.d("msgArduino", readMessage);
                    Log.d("msgArduino", "FIRST:"+readMessage.charAt(0));
                    try {
                        Log.d("msgArduino", "Second:" + readMessage.charAt(1));
                        Log.d("msgArduino", "Third:" + readMessage.charAt(2));
                    }
                    catch(Exception e) {

                    }
                    recDataString.append(readMessage);
                    Toast.makeText(ArduinoActivity.this, recDataString, Toast.LENGTH_SHORT).show();
                    if (readMessage.equals("1")) {
                        sendSMS();
                    }
                    recDataString.delete(0, recDataString.length());
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ArduinoActivity.this, "Connecting...", "Please wait");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            }
            catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
                ConnectedThread mConnectedThread;
                mConnectedThread = new ConnectedThread(btSocket);
                mConnectedThread.start();
            }
            progress.dismiss();
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void write(String input) {
            byte[] msgBuffer = input.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    private void sendSMS() {
        final User user = currentUser;
        final User partner = currentPartner;

        if ((user != null) && (partner != null)) {
            String loc = "";

            try {
                geocoder = new Geocoder(this, Locale.getDefault());
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
            Notification notification = new Notification(user.getId(), partner.getId(),
                    user.getFullname(), user.getPhoneNumber(), partner.getPhoneNumber(),
                    user.getLat(), user.getLng(), loc, new Timestamp(System.currentTimeMillis()));
            databaseReference.child(key).setValue(notification);

            String text = "bSAFE Panic Button is Activated! Their location is near " + loc +
                    ". Open your bSAFE mobile apps to observe the detailed user location." +
                    "Stay safe with bSAFE";

            String url = "https://rest.nexmo.com/sms/json";
            HashMap<String, String> params = new HashMap<>();

            params.put("api_key", "4dd9933d");
            params.put("api_secret", "27c317f77c0787f2");
            params.put("from", user.getPhoneNumber());
            params.put("to", partner.getPhoneNumber());
            params.put("text", text);

            final JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(ArduinoActivity.this, "SMS sent successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(ArduinoActivity.this, "Failed to send SMS.", Toast.LENGTH_SHORT).show();
                        }
                    });
            MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
        }
    }
}