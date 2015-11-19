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

import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
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

    //vistas
    private TextView txtNombreRed, txtTipoRed, txtModelo, txtVersion, txtIpPublic, txtIpLocal, txtSeñal, txtGateway, txtMasSubred, txtDns1, txtDns2;
    private FloatingActionButton fab;

    //servicios
    private Connectivity con; //esta es mi clase
    private TelephonyManager tlfMan;
    private NetworkInfo info;


    //
    private MyPhoneStateListener MyListener;


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

        txtNombreRed = (TextView) rootView.findViewById(R.id.txtNombreRed);
        txtTipoRed = (TextView) rootView.findViewById(R.id.txtRed);
        txtModelo = (TextView) rootView.findViewById(R.id.txtModelo);
        txtVersion = (TextView) rootView.findViewById(R.id.txtVersion);
        txtIpPublic = (TextView) rootView.findViewById(R.id.txtIpPublic);
        txtIpLocal = (TextView) rootView.findViewById(R.id.txtIpLocal);
        txtSeñal = (TextView) rootView.findViewById(R.id.txtSeñal);

        txtDns1 = (TextView) rootView.findViewById(R.id.txtDns1);
        txtDns2 = (TextView) rootView.findViewById(R.id.txtDns2);
        txtMasSubred = (TextView) rootView.findViewById(R.id.txtMasSubred);
        txtGateway = (TextView) rootView.findViewById(R.id.txtGateway);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        //datos de telefonía.
        tlfMan = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        MyListener = new MyPhoneStateListener();
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);



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


        printData();

        return rootView;
    }


    private class MyPhoneStateListener extends PhoneStateListener {


        private String gsmStrength = "";
        private int cobertura = 0;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

           /* gsmStrength = String.valueOf(signalStrength.getGsmSignalStrength() * 2 - 113);


            CellSignalStrengthLte a1 = new CellSignalStrengthLte(signalStrength);
            a1.initialize(signalStrength, 1);
            cobertura = a1.getDbm();

            if (cobertura > 0) {
                CellSignalStrengthGsm a3 = new CellSignalStrengthGsm();
                a3.initialize(signalStrength.getGsmSignalStrength(), 1);
                cobertura = a3.getDbm();
            }

            if (cobertura > 0) {
                CellSignalStrengthCdma a2 = new CellSignalStrengthCdma();
                a2.initialize(signalStrength.getCdmaDbm(), signalStrength.getCdmaEcio(), signalStrength.getEvdoDbm(), signalStrength.getEvdoEcio(), signalStrength.getEvdoSnr());
                cobertura = a2.getDbm();
            }*/

            try {
                final TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                for (final CellInfo info : tm.getAllCellInfo()) {
                    if (info instanceof CellInfoGsm) {
                        final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                        // do what you need
                        gsmStrength = String.valueOf(gsm.getDbm());
                    } else if (info instanceof CellInfoCdma) {
                        final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();
                        gsmStrength = String.valueOf(cdma.getDbm());
                    } else if (info instanceof CellInfoLte) {
                        final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                        gsmStrength = String.valueOf(lte.getDbm());
                    } else {
                        gsmStrength = String.valueOf("Uknow");
                    }
                }
            } catch (Exception e) {

            }

            printData();
        }

        public String getStrength() {
            return gsmStrength;
        }

        public String getCobertura() {
            return String.valueOf(cobertura);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    }

    @Override
    public void onPause() {
        super.onPause();
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_NONE);


    }

    @Override
    public void onStop() {
        super.onStop();


    }

    private void printData() {


        info = Connectivity.getNetworkInfo(getActivity());

        if (con.isConnectedWifi(getContext())) {
            fab.setImageResource(R.drawable.ic_wifi_ac);

            txtNombreRed.setText(info.getExtraInfo());
            txtTipoRed.setText(con.getType(info.getType(), info.getSubtype()));

            txtSeñal.setText("wifi dBm");

        } else if (con.isConnectedMobile(getContext())) {
            fab.setImageResource(R.drawable.ic_antenna_ac);

            txtNombreRed.setText(tlfMan.getNetworkOperatorName());
            txtTipoRed.setText(con.getType(info.getType(), info.getSubtype()));

            txtSeñal.setText(MyListener.getStrength() + "dBm");
            txtIpPublic.setText(MyListener.getStrength());
        } else {
            fab.setImageResource(R.drawable.ic_antenna_ac);

            txtNombreRed.setText(tlfMan.getNetworkOperatorName());
            txtTipoRed.setText("-");
            txtSeñal.setText("-");
            txtSeñal.setText(MyListener.getStrength() + "dBm");
            txtIpPublic.setText(MyListener.getStrength());
        }

        txtVersion.setText("Android " + Build.VERSION.RELEASE);
        txtModelo.setText(Build.MODEL);

    }

 


  /*  public void callAsynchronousTask() {
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
    }*/


}
