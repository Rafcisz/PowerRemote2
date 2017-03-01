package com.example.asus.powerremote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends Activity {

    protected static final int DISCOVERY_REQUEST = 1;
    private BluetoothAdapter btAdapter;


    public TextView statusUpdate;
    public Button connect;
    public Button disconnect;
    public ImageView logo;
    public String toastText="";
    public BluetoothDevice remoteDevice;

    BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String prevStateExtra = BluetoothAdapter.EXTRA_PREVIOUS_STATE;
            String stateExtra = BluetoothAdapter.EXTRA_STATE;
            int state = intent.getIntExtra(stateExtra, -1);
            int previousState = intent.getIntExtra(prevStateExtra, -1);
            switch (state){
                case (BluetoothAdapter.STATE_TURNING_ON):
                {
                    toastText="Bluetooth is turning on";
                    Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    break;
                }
                case (BluetoothAdapter.STATE_ON):
                {
                    toastText="Bluetooth is on";
                    Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    setupIU();
                    break;
                }
                case (BluetoothAdapter.STATE_TURNING_OFF):
                {
                    toastText="Bluetooth is turning off";
                    Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    break;
                }
                case (BluetoothAdapter.STATE_OFF):
                {
                    toastText="Bluetooth off";
                    Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    setupIU();
                    break;
                }

            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupIU();
    }

    private void setupIU() {
        final TextView statusUpdate = (TextView) findViewById(R.id.result);
        final Button connect = (Button)findViewById(R.id.connectBtn);
        final Button disconnect = (Button)findViewById(R.id.disconnectBtn);


        disconnect.setVisibility(View.GONE);
        logo.setVisibility(View.GONE);

        btAdapter= BluetoothAdapter.getDefaultAdapter();
            if (btAdapter.isEnabled()){
                String address = btAdapter.getAddress();
                String name = btAdapter.getName();
                String statusText = name + " : " + address;
                statusUpdate.setText(statusText);
                disconnect.setVisibility(View.VISIBLE);
                logo.setVisibility(View.VISIBLE);
                connect.setVisibility(View.GONE);
        }
        else {
            connect.setVisibility(View.VISIBLE);
            statusUpdate.setText("Bluetooth nie włączone");
        }

        connect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
                //String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;
                //IntentFilter filter = new IntentFilter(actionStateChanged);
                //registerReceiver(bluetoothState, filter);
                //startActivityForResult(new Intent(actionRequestEnable),0);

                String scanModeChanged = BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
                String beDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
                IntentFilter filter = new IntentFilter(scanModeChanged);
                registerReceiver(bluetoothState, filter);
                startActivityForResult(new Intent(beDiscoverable), DISCOVERY_REQUEST);

            }
        });

        disconnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btAdapter.disable();
                disconnect.setVisibility(View.GONE);
                logo.setVisibility(View.GONE);
                connect.setVisibility(View.VISIBLE);
                statusUpdate.setText("Bluetooth off");

            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == DISCOVERY_REQUEST){
            Toast.makeText(MainActivity.this, "Discovery in progress", Toast.LENGTH_SHORT).show();
            setupIU();
            findDevices();
        }
    }

    private void findDevices() {
        String lastUsedRemoteDevice = getLastUsedRemoteBTDEvice();
        if (lastUsedRemoteDevice != null) {
            toastText="Checking for known paired devices, namenly: "+ lastUsedRemoteDevice;
            Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();

            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            for (BluetoothDevice pairedDevice : pairedDevices) {
                if (pairedDevice.getAddress().equals(lastUsedRemoteDevice)) {
                    toastText= "Found device: " + pairedDevice.getName() + "@" + lastUsedRemoteDevice;
                    Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    remoteDevice = pairedDevice;
                }
            }
        }
        if (remoteDevice == null) {
            toastText="Starting discovery for remote devices...";
            Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
            registerReceiver(discoveruResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    BroadcastReceiver discoveruResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            BluetoothDevice remoteDevice;
            remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            toastText= "Discovered: "+ remoteDeviceName;
            Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
        }
    };
    private String getLastUsedRemoteBTDEvice() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String result = prefs.getString("LAST_REMOTE_ADDRESS", null);
        return result;
    }
}
