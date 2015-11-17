package com.r.raul.tools;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Rulo on 15/11/2015.
 */
public class MainDeviceInfo extends Fragment {

    TextView txtInfo, txtInfo2, txtModelo, txtVersion;
    Connectivity con;
    FloatingActionButton fab;
    TelephonyManager tlfMan;


    public static MainDeviceInfo newInstance() {
        MainDeviceInfo fragment = new MainDeviceInfo();
        return fragment;
    }

    public MainDeviceInfo() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.info_device, container, false);

        TextView txtInfo = (TextView) rootView.findViewById(R.id.txtTipo);
        TextView txtInfo2 = (TextView) rootView.findViewById(R.id.txtRed);
        TextView txtModelo = (TextView) rootView.findViewById(R.id.txtModelo);
        TextView txtVersion = (TextView) rootView.findViewById(R.id.txtVersion);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (con.isConnectedWifi(getContext())) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
                if (con.isConnectedMobile(getContext())) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.android.settings.Settings$DataUsageSummaryActivity"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });

        NetworkInfo info = Connectivity.getNetworkInfo(getContext());
        tlfMan = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        if (con.isConnectedWifi(getContext())) {
            fab.setImageResource(R.drawable.ic_wifi_ac);
            txtInfo.setText(info.getExtraInfo());
        }
        if (con.isConnectedMobile(getContext())) {
            fab.setImageResource(R.drawable.ic_antenna_ac);
            txtInfo.setText(tlfMan.getNetworkOperatorName());
        }

        txtInfo2.setText(con.getType(info.getType(), info.getSubtype()));
        txtVersion.setText("Android " + Build.VERSION.RELEASE);
        txtModelo.setText(Build.MODEL);


       // callAsynchronousTask();
        return rootView;
    }


    public void carga() {


        try {


            NetworkInfo info = Connectivity.getNetworkInfo(getContext());
            tlfMan = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

            if (con.isConnectedWifi(getContext())) {
                fab.setImageResource(R.drawable.ic_wifi_ac);
                txtInfo.setText(info.getExtraInfo());
            }
            if (con.isConnectedMobile(getContext())) {
                fab.setImageResource(R.drawable.ic_antenna_ac);
                txtInfo.setText(tlfMan.getNetworkOperatorName());
            }

            txtInfo2.setText(con.getType(info.getType(), info.getSubtype()));
            txtVersion.setText("Android " + Build.VERSION.RELEASE);
            txtModelo.setText(Build.MODEL);

            txtInfo2.postInvalidate();
            txtInfo.postInvalidate();
            Toast.makeText(getActivity(), "Tarea finalizada!",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

        }


    }


    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {

                            carga();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }

        };
        timer.schedule(doAsynchronousTask, 0, 1000 * 10); //execute in every 50000 ms


    }


    class Señales extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {


            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progreso = values[0].intValue();


        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Boolean result) {


           carga();
        }

        @Override
        protected void onCancelled() {
            // Toast.makeText(getActivity(), "Tarea cancelada!", Toast.LENGTH_SHORT).show();
            carga();
        }
    }


}






