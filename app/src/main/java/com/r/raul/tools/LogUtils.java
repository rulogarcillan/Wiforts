package com.r.raul.tools;


import android.util.Log;


public class LogUtils {

    static final String TAG = "DEBUG_WIFORTS: ";

    public static void LOGD(final String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG + tag, message);
        }
    }

    public static void LOGV(final String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG + tag, message);
        }
    }

    public static void LOGI(final String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG + tag, message);
        }
    }

    public static void LOGW(final String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG + tag, message);
        }
    }

    public static void LOGE(final String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG + tag, message);
        }
    }

    public static void LOG(String message) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG , message);
        }
    }
}
