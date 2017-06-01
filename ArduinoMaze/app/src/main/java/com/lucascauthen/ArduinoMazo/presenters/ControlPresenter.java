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
        queueViewStatusForDuration("Go!", 200);
        final int duration;
        switch(this.difficulty) {
            case EASY:
                duration = 120000;
                break;
            case MEDIUM:
                duration = 90000;
                break;
            case HARD:
                duration = 45000;
                break;
            default:
                duration = 120000;
        }
        toggleViewDifficultySelect(false);
        queueViewStatusForDuration("Ready?!?!", 1000);
        queueViewStatusForDuration("Set", 500, new StatusMessage.OnDoneShowingListener() {
            @Override
            public void done() {
                view.startPlayCounter(duration, 500);
                backgroundExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        view.sendStartSignal();
                    }
                });
            }
        });
    }

    public void onTimerFinished() {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                view.sendStopSignal();
            }
        });
    }

    public void disconnect() {
        this.isBluetoothConnected = false;
    }

    public void newTiltData(final double azimuth, final double pitch, final double roll) {
        Direction d;
        int speed;
        //TODO calculate these values and send to arduino
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
        void sendStartSignal();
        void sendStopSignal();
        void sendInputData(byte[] input);
        void displayEndMessage();
    }
    public interface CompleteCallback {
        void complete(boolean status);
    }

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public enum MessageType {
        START((byte)0),
        STOP((byte)1),
        INPUT((byte)2),
        MESSAGE_SUFFIX((byte)255);

        public byte data;
        MessageType(byte data) {
            this.data = data;
        }
    }
}
