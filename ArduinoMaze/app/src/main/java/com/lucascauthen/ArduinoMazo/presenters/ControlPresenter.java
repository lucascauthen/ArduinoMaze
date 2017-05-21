package com.lucascauthen.ArduinoMazo.presenters;

import com.lucascauthen.ArduinoMazo.utility.BackgroundExecutor;
import com.lucascauthen.ArduinoMazo.utility.ForegroundExecutor;
import com.lucascauthen.ArduinoMazo.utility.NullObject;

public class ControlPresenter {

    private static ControlView NULL_VIEW = NullObject.create(ControlView.class);
    private ControlView view = NULL_VIEW;
    public static String DEVICE_ADDRESS = "device:address"; //Address used for bluetooth intent

    private final ForegroundExecutor foregroundExecutor;
    private final BackgroundExecutor backgroundExecutor;

    private boolean isBluetoothConnected = false;

    public ControlPresenter() {
        this.foregroundExecutor = new ForegroundExecutor();
        this.backgroundExecutor = new BackgroundExecutor();
    }

    public void attachView(ControlView view) {
        this.view = view;
    }

    public void present() {
        foregroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                view.toggleLoading(true);
            }
        });
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                view.connectToBluetooth(new CompleteCallback() {
                    @Override
                    public void complete(boolean status) {
                        ControlPresenter.this.isBluetoothConnected = status;
                        if(status) {
                            updateMsg("Connected!");
                        } else {
                            //TODO Handle error
                        }
                    }
                });
            }
        });
    }

    public void detachView() {
        this.view = NULL_VIEW;
    }

    public boolean isBluetoothConnected() {
        return isBluetoothConnected;
    }

    private void updateMsg(final String msg) {
        foregroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                view.toggleStatus(true);
                view.setStatusMsg(msg);
            }
        });
    }

    public interface ControlView {
        void connectToBluetooth(CompleteCallback callback);
        void toggleLoading(boolean enabled);
        void toggleStatus(boolean enabled);
        void setStatusMsg(String msg);
    }
    public interface CompleteCallback {
        void complete(boolean status);
    }
}
