package com.lucascauthen.ArduinoMazo.view;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.lucascauthen.ArduinoMazo.R;
import com.lucascauthen.ArduinoMazo.presenters.ControlPresenter;

import java.io.IOException;
import java.util.UUID;


public class ControlActivity extends AppCompatActivity implements ControlPresenter.ControlView {

    private BluetoothAdapter adapter;
    private BluetoothSocket socket;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;

    private ControlPresenter presenter;

    private static final UUID DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @BindView(R.id.loading) ProgressBar loading;
    @BindView(R.id.status) TextView status;

    private String address;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        ButterKnife.bind(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Setup gyroscope sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        this.presenter = new ControlPresenter();
        presenter.attachView(this);

        Intent intent = getIntent();
        this.address = intent.getStringExtra(ControlPresenter.DEVICE_ADDRESS);
        presenter.present();
    }

    @Override
    public void connectToBluetooth(ControlPresenter.CompleteCallback callback) {
        try {
            if (socket == null || !presenter.isBluetoothConnected()) {
                adapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                BluetoothDevice dispositivo = adapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                socket = dispositivo.createInsecureRfcommSocketToServiceRecord(DEVICE_UUID);//create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                socket.connect();//start connection
            }
        } catch (IOException e) {
            e.printStackTrace();
            callback.complete(false);
        }
        callback.complete(true);
    }

    @Override
    public void toggleLoading(boolean enabled) {
        if (enabled) {
            loading.setVisibility(View.VISIBLE);
        } else {
            loading.setVisibility(View.GONE);
        }
    }

    @Override
    public void toggleStatus(boolean enabled) {
        if (enabled) {
            status.setVisibility(View.VISIBLE);
        } else {
            status.setVisibility(View.GONE);
        }
    }

    @Override
    public void setStatusMsg(String msg) {
        status.setText(msg);
    }
}
