package com.example.julianparker.volngo;

import android.app.Application;

import timber.log.Timber;

/**
 * Created on 7/27/2017.
 */

public class PlacePicker extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
