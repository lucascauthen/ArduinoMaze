package com.lucascauthen.ArduinoMazo.presenters;

public class StatusMessage {
    public final int duration;
    public final String msg;
    private OnDoneShowingListener listener;

    public StatusMessage(String msg, int duration, OnDoneShowingListener listener) {
        this(msg, duration);
        this.listener = listener;
    }

    public StatusMessage(String msg, int duration) {
        this.duration = duration;
        this.msg = msg;
    }

    public interface OnDoneShowingListener {
        void done();
    }

    public void done() {
        if(listener != null) {
            listener.done();
        }
    }
}