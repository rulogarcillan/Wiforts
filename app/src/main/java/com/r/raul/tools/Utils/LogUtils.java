package com.r.raul.tools.Utils;


import android.os.Environment;
import android.util.Log;

import com.r.raul.tools.BuildConfig;
import com.r.raul.tools.DB.MyDatabase;
import com.r.raul.tools.DB.MyDatabaseMacs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;


public class LogUtils {

    static final String TAGBBDD = "BBDDmyNETWORKS";
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
            Log.i(TAG , message);
        }
    }


    public static final void copybd() {

        if (BuildConfig.DEBUG) {
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "/data/data/" + BuildConfig.APPLICATION_ID + "/databases/" + MyDatabase.DATABASE_NAME;
                    String backupDBPath = BuildConfig.APPLICATION_ID + ".db";
                    File currentDB = new File(currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    if (currentDB.exists()) {
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    }

                    String currentDBPath2 = "/data/data/" + BuildConfig.APPLICATION_ID + "/databases/" + MyDatabaseMacs.DATABASE_NAME;
                    String backupDBPath2 = BuildConfig.APPLICATION_ID + "_mac" + ".db";
                    File currentDB2 = new File(currentDBPath2);
                    File backupDB2 = new File(sd, backupDBPath2);

                    if (currentDB2.exists()) {
                        FileChannel src = new FileInputStream(currentDB2).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB2).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    }
                }
            } catch (Exception e) {
                LOGI("BBDD No copiada");
            }
        }
    }
}
