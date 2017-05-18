package com.lucascauthen.ArduinoMazo.view;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.lucascauthen.ArduinoMazo.R;

import java.util.Set;

public class ConnectActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private static final int  REQUEST_ENABLE_BT_CODE = 999; //Code used for requesting the user to turn on BT
    private Set<BluetoothDevice> pairedDevices;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        //Setup bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            //Kill app if no bluetooth support
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        } else if(!bluetoothAdapter.isEnabled()) {
            //Request user turn on bluetooth if it is not on
            Intent requestBluetoothOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(requestBluetoothOnIntent, REQUEST_ENABLE_BT_CODE);
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        registerReceiver(bluetoothReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT_CODE) {

        }
    }
}
