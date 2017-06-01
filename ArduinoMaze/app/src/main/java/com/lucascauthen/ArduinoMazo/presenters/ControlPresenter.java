package com.lucascauthen.ArduinoMazo.presenters;

import android.util.Log;
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

    private Direction lastTilt = Direction.NONE;

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
                duration = 2000000;
                break;
            case MEDIUM:
                duration = 15000;
                break;
            case HARD:
                duration = 10000;
                break;
            default:
                duration = 10000;
        }
        toggleViewDifficultySelect(false);
        /*queueViewStatusForDuration("Ready?!?!", 1000);
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
        });*/
        view.startPlayCounter(duration, 10);
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                view.startUpdateLoop(30);
                view.sendStartSignal(ControlPresenter.this.difficulty);
            }
        });
    }

    public void onTimerFinished() {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                view.stopUpdateLoop();
                view.sendStopSignal();
            }
        });
    }

    public void disconnect() {
        this.isBluetoothConnected = false;
    }

    public void newTiltData(final double pitch, final double roll) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Direction newDirection = Direction.NONE;
                double pitch_abs = Math.abs(pitch);
                double roll_abs = Math.abs(roll);
                if(pitch_abs > 30.0 || roll_abs > 30.0) {
                    if(pitch_abs > roll_abs) {
                        if(pitch > 30.0) {
                            newDirection = Direction.LEFT;
                        } else {
                            newDirection = Direction.RIGHT;
                        }
                    } else {
                        if(roll > 30.0) {
                            newDirection = Direction.UP;
                        } else {
                            newDirection = Direction.DOWN;
                        }
                    }
                }
                if(newDirection != lastTilt) {
                    lastTilt = newDirection;
                    view.sendInputData(newDirection.data);
                }
            }
        });
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
        void sendStartSignal(Difficulty difficulty);
        void sendStopSignal();
        void sendInputData(byte direction);
        void displayEndMessage();
    }
    public interface CompleteCallback {
        void complete(boolean status);
    }

    public enum Difficulty {
        EASY((byte)0),
        MEDIUM((byte)1),
        HARD((byte)2);

        public byte data;
        Difficulty(byte data) {
            this.data = data;
        }
    }

    public enum Direction {
        NONE((byte)0),
        UP((byte)1),
        DOWN((byte)2),
        LEFT((byte)3),
        RIGHT((byte)4);
        public byte data;
        Direction(byte data) {
            this.data = data;
        }
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
