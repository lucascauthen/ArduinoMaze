package com.lucascauthen.ArduinoMazo.presenters;

import com.lucascauthen.ArduinoMazo.utility.NullObject;

public class ControlPresenter {

    private static ControlView NULL_VIEW = NullObject.create(ControlView.class);
    private ControlView view = NULL_VIEW;

    public void attachView(ControlView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = NULL_VIEW;
    }

    public interface ControlView {
        void showLoadingMsg(String msg);

    }
}
