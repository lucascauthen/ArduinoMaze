package com.lucascauthen.ArduinoMazo.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.lucascauthen.ArduinoMazo.R;
import com.lucascauthen.ArduinoMazo.presenters.ControlPresenter;
import com.lucascauthen.ArduinoMazo.presenters.StatusMessage;
import com.lucascauthen.ArduinoMazo.utility.AnimationUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import static android.view.View.GONE;


public class ControlActivity extends AppCompatActivity implements ControlPresenter.ControlView, SensorEventListener {

    private BluetoothAdapter adapter;
    private BluetoothSocket socket;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor magneticSensor;


    private ControlPresenter presenter;

    private static final UUID DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @BindView(R.id.loading) ProgressBar loading;
    @BindView(R.id.status) TextView status;
    @BindView(R.id.difficultyContainer) LinearLayout difficultyContainer;
    @BindView(R.id.timer) TextView timer;

    private String address;

    private float inclinationMatrix[];
    private float rotationMatrix[];
    private float gravity[];
    private float geomagnetic[];
    private float orientationMatrix[];

    private Handler updateHandler;
    private boolean stopUpdate = false;

    boolean lightOn = false; //TODO Remove after testing

    Queue<StatusMessage> msgQueue = new LinkedList<>();
    boolean showingMsg = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        ButterKnife.bind(this);
        hideSystemUI();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Setup gyroscope sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        this.presenter = new ControlPresenter();
        presenter.attachView(this);

        updateHandler = new Handler();

        inclinationMatrix = new float[9];
        rotationMatrix = new float[9];
        gravity = new float[3];
        geomagnetic = new float[3];
        orientationMatrix = new float[3];

        Intent intent = getIntent();
        this.address = intent.getStringExtra(ControlPresenter.DEVICE_ADDRESS);
        //presenter.present();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometerSensor, 100000);
        sensorManager.registerListener(this, magneticSensor, 100000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            loading.setVisibility(GONE);
        }
    }

    @Override
    public void toggleMsgStatus(boolean enabled) {
        if (enabled) {
            status.setVisibility(View.VISIBLE);
        } else {
            status.setVisibility(GONE);
        }
    }

    @Override
    public void queueMessageForDuration(final StatusMessage msg) {
        this.msgQueue.add(msg);
        if (!showingMsg) {
            showingMsg = true;
            updateHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!msgQueue.isEmpty()) {
                        showMessageForDuration(msgQueue.poll(), this);
                    } else {
                        showingMsg = false;
                    }
                }
            });
        }
    }

    private void showMessageForDuration(final StatusMessage msg, final Runnable after) {
        status.setText(msg.msg);
        AnimationUtils.fadeInFromGone(status, 500, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                updateHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AnimationUtils.fadeOutToGone(status, 500, new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                msg.done();
                                updateHandler.post(after);
                            }
                        });
                    }
                }, msg.duration);
            }
        });
    }

    @Override
    public void setStatusMsg(String msg) {
        status.setText(msg);
    }

    @Override
    public void toggleDifficultySelect(boolean enabled) {
        if (enabled) {
            AnimationUtils.fadeInFromGone(difficultyContainer, 500);
        } else {
            AnimationUtils.fadeOutToGone(difficultyContainer, 500);
        }
    }

    @Override
    public void toggleTimer(boolean enabled) {
        if (enabled) {
            timer.setVisibility(View.VISIBLE);
        } else {
            timer.setVisibility(GONE);
        }
    }

    @Override
    public void startPlayCounter(final int time, final int updateInterval) {
        timer.setVisibility(View.VISIBLE);
        new CountDownTimer(time, updateInterval) {

            @SuppressLint("DefaultLocale")
            public void onTick(long millisUntilFinished) {
                timer.setText(String.format("%.2f", (float) millisUntilFinished / 1000.0f));
            }

            public void onFinish() {
                presenter.onTimerFinished();
                timer.setText("0.00");
            }
        }.start();
    }

    @Override
    public void startUpdateLoop(final int updatesPerSecond) {
        stopUpdate = false;
        Runnable updateLoop = new Runnable() {
            @Override
            public void run() {
                double azimuth = Math.toDegrees(orientationMatrix[0]);
                double pitch = Math.toDegrees(orientationMatrix[1]);
                double roll = Math.toDegrees(orientationMatrix[2]);


                if (!stopUpdate) {
                    updateHandler.postDelayed(this, 1000 / updatesPerSecond);
                }
            }
        };

    }

    @Override
    public void stopUpdateLoop() {
        stopUpdate = true;
    }

    @Override
    public void sendStartSignal(ControlPresenter.Difficulty difficulty) {
        try {
            socket.getOutputStream().write(ControlPresenter.MessageType.START.data);
            socket.getOutputStream().write(difficulty.data);
            socket.getOutputStream().write(ControlPresenter.MessageType.MESSAGE_SUFFIX.data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendStopSignal() {
        try {
            byte msg = ControlPresenter.MessageType.STOP.data;
            socket.getOutputStream().write(msg);
            socket.getOutputStream().write(ControlPresenter.MessageType.MESSAGE_SUFFIX.data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendInputData(byte direction) {
        try {
            socket.getOutputStream().write(direction);
            socket.getOutputStream().write(ControlPresenter.MessageType.MESSAGE_SUFFIX.data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void displayEndMessage() {

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }
        if (gravity != null && geomagnetic != null) {
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic);
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationMatrix);
                StringBuilder builder = new StringBuilder();
                builder.append("Azimuth:").append(String.format("%.2f", Math.toDegrees(orientationMatrix[0])))
                .append("\nPitch:").append(String.format("%.2f", Math.toDegrees(orientationMatrix[1])))
                .append("\nRoll:").append(String.format("%.2f", Math.toDegrees(orientationMatrix[2])));
                status.setText(builder.toString());
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Do nothing
    }

    @OnClick(R.id.status)
    void onClickStatus() {
        if (lightOn) {
            try {
                socket.getOutputStream().write("0".toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                socket.getOutputStream().write("1".toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        lightOn = !lightOn;
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @OnClick(R.id.easyDifficultyButton)
    void onClickEasy() {
        presenter.setDifficulty(ControlPresenter.Difficulty.EASY);
    }

    @OnClick(R.id.mediumDifficultyButton)
    void onClickMedium() {
        presenter.setDifficulty(ControlPresenter.Difficulty.MEDIUM);
    }

    @OnClick(R.id.hardDifficultyButton)
    void onClickHard() {
        presenter.setDifficulty(ControlPresenter.Difficulty.HARD);
    }
}


