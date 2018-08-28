package com.simplelifesolutions.palettestudio.Helpers;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;

import java.io.File;

public final class App extends Application {
    private static App sInstance;
    private byte[] mCapturedPhotoData;

    // Getters & Setters
    public byte[] getCapturedPhotoData() {
        return mCapturedPhotoData;
    }

    public void setCapturedPhotoData(byte[] capturedPhotoData) {
        mCapturedPhotoData = capturedPhotoData;
    }

    // Singleton code
    public static App getInstance() { return sInstance; }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
}
