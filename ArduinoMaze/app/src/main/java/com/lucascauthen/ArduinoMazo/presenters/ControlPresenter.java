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

    private Difficulty difficulty;

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
                //view.toggleLoading(true);
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
                            queueViewStatusForDuration("Connected!!", 1500);
                            queueViewStatusForDuration("Select a game mode!", 5000);
                            toggleViewLoading(false);
                            toggleViewDifficultySelect(true);
                        } else {
                            updateViewMsg("Error!!!");
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

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        toggleViewDifficultySelect(false);
        queueViewStatusForDuration("Ready?!?!", 1000);
        queueViewStatusForDuration("Set", 500, new StatusMessage.OnDoneShowingListener() {
            @Override
            public void done() {
                //Start playing
            }
        });
        queueViewStatusForDuration("Go!", 200);
    }

    public void onTimerFinished() {

    }

    public void disconnect() {
        this.isBluetoothConnected = false;
    }

    private void updateViewMsg(final String msg) {
        foregroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                view.toggleMsgStatus(true);
                view.setStatusMsg(msg);
            }
        });
    }
    private void toggleViewLoading(final boolean enabled) {
        foregroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                view.toggleLoading(enabled);
            }
        });
    }

    private void toggleViewDifficultySelect(final boolean enabled) {
        foregroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                view.toggleDifficultySelect(enabled);
            }
        });
    }

    private void queueViewStatusForDuration(final String msg, final int durMillis) {
        foregroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                view.queueMessageForDuration(new StatusMessage(msg, durMillis));
            }
        });
    }
    private void queueViewStatusForDuration(final String msg, final int durMillis, final StatusMessage.OnDoneShowingListener listener) {
        foregroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                view.queueMessageForDuration(new StatusMessage(msg, durMillis, listener));
            }
        });
    }

    public interface ControlView {
        void connectToBluetooth(CompleteCallback callback);
        void toggleLoading(boolean enabled);
        void toggleMsgStatus(boolean enabled);
        void queueMessageForDuration(StatusMessage msg);
        void setStatusMsg(String msg);
        void toggleDifficultySelect(boolean enabled);
        void toggleTimer(boolean enabled);
        void startPlayCounter(int time, int updateInterval);
        void startUpdateLoop(int updatesPerSecond);
        void stopUpdateLoop();
    }
    public interface CompleteCallback {
        void complete(boolean status);
    }

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }
}
