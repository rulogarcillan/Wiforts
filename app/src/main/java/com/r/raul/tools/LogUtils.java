package com.r.raul.tools;


import android.util.Log;


public class LogUtils {

    static final String TAG = "myNetworks: ";

    public static void LOGD(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void LOGV(String message) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, message);
        }
    }

    public static void LOGI(String message) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, message);
        }
    }

    public static void LOGW(String message) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, message);
        }
    }

    public static void LOGE(String message) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, message);
        }
    }
    public static void LOG(String message) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG , message);
        }
    }
}
