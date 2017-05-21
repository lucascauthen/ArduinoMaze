package com.lucascauthen.ArduinoMazo.view;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import com.lucascauthen.ArduinoMazo.R;
import com.lucascauthen.ArduinoMazo.presenters.ControlPresenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ConnectActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT_CODE = 999; //Code used for requesting the user to turn on BT
    private Set<BluetoothDevice> pairedDevices;

    @BindView(R.id.deviceList) ListViewCompat deviceList;
    List<BluetoothDevice> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ButterKnife.bind(this);
        //Setup bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //Kill app if no bluetooth support
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        } else if (!bluetoothAdapter.isEnabled()) {
            tryEnableBluetooth();
        } else {
            setupBlueToothListView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT_CODE) {
            if(bluetoothAdapter.isEnabled()) {
                setupBlueToothListView();
            } else {
                tryEnableBluetooth();
            }
        }
    }

    @OnItemClick(R.id.deviceList)
    public void onItemClick(int position) {
        String address = devices.get(position).getAddress();
        Intent intent = new Intent(this, ControlActivity.class);
        intent.putExtra(ControlPresenter.DEVICE_ADDRESS, address);
        startActivity(intent);
    }

    private void tryEnableBluetooth() {
        Intent requestBluetoothOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(requestBluetoothOnIntent, REQUEST_ENABLE_BT_CODE);
    }

    private void setupBlueToothListView() {
        List<String> deviceInfo = new ArrayList<>();
        devices = new ArrayList<>(bluetoothAdapter.getBondedDevices());
        for (BluetoothDevice aDevice : devices) {
            deviceInfo.add(aDevice.getName() + "\n" + aDevice.getAddress());
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceInfo);
        deviceList.setAdapter(adapter);
    }
}
