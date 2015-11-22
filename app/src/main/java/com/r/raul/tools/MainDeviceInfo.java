package com.r.raul.tools;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
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
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
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
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_SERVICE_STATE);


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
        private String wifiStrength = "";
        private int tipoIcono;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            if (con.isConnectedWifi(getContext())) {
                getLevelWifi(getContext());

            } else if (con.isConnectedMobile(getContext())) {
                try {

                    tlfMan = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                    for (CellInfo info2 : tlfMan.getAllCellInfo()) {
                        if (info2 instanceof CellInfoGsm && con.getType(info.getType(), info.getSubtype()) != "4G | LTE") {
                            CellSignalStrengthGsm gsm = ((CellInfoGsm) info2).getCellSignalStrength();
                            iconoGsm(gsm.getLevel());

                            gsmStrength = String.valueOf(gsm.getDbm()) + " dBm";
                            LogUtils.LOG("GSM " + gsmStrength);
                        } else if (info2 instanceof CellInfoCdma) {
                            CellSignalStrengthCdma cdma = ((CellInfoCdma) info2).getCellSignalStrength();
                            iconoGsm(cdma.getLevel());
                            gsmStrength = String.valueOf(cdma.getDbm()) + " dBm";
                            LogUtils.LOG("cdma " + gsmStrength);
                        } else if (info2 instanceof CellInfoLte) {
                            CellSignalStrengthLte lte = ((CellInfoLte) info2).getCellSignalStrength();
                            gsmStrength = String.valueOf(lte.getDbm()) + " dBm";
                            iconoGsm(lte.getLevel());
                            LogUtils.LOG("lte " + gsmStrength);
                        } else if (info2 instanceof CellInfoWcdma) {
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info2).getCellSignalStrength();
                                gsmStrength = String.valueOf(wcdma.getDbm()) + " dBm";
                                iconoGsm(wcdma.getLevel());
                                LogUtils.LOG("cdma " + gsmStrength);
                            } else {

                                gsmStrength = String.valueOf(signalStrength.getGsmSignalStrength() * 2 - 113) + " dBm";
                            }

                        } else {
                            if (con.getType(info.getType(), info.getSubtype()) == "4G | LTE") {

                            } else {
                                gsmStrength = String.valueOf("Uknow");
                                iconoGsm(-1);
                            }

                        }
                    }
                } catch (Exception e) {
                    LogUtils.LOG(e.getMessage());
                }

            } else {

                //nada
            }


            printData();
        }


        public void getLevelWifi(Context context) {

            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> wifiList = wifiManager.getScanResults();
            for (ScanResult scanResult : wifiList) {
                int level = WifiManager.calculateSignalLevel(scanResult.level, 5);
            }

            int rssi = wifiManager.getConnectionInfo().getRssi();
            int level = WifiManager.calculateSignalLevel(rssi, 5);

            switch (level) {
                case 0:
                    tipoIcono = R.drawable.ic_wifi1;
                    break;
                case 1:
                    tipoIcono = R.drawable.ic_wifi2;
                    break;
                case 2:
                    tipoIcono = R.drawable.ic_wifi3;
                    break;
                case 3:
                    tipoIcono = R.drawable.ic_wifi4;
                    break;
                case 4:
                    tipoIcono = R.drawable.ic_wifi5;
                    break;

            }

             wifiStrength = rssi + " dBm";
        }

        public String getGsmStrength() {
            return gsmStrength;
        }

        public String getWifiStrength() {
            return wifiStrength;
        }

        public void iconoGsm(int percent) {
            switch (percent) {
                case -1:
                    tipoIcono = R.drawable.ic_sigmobile0;
                    break;
                case 1:
                    tipoIcono = R.drawable.ic_sigmobile2;
                    break;
                case 2:
                    tipoIcono = R.drawable.ic_sigmobile3;
                    break;
                case 3:
                    tipoIcono = R.drawable.ic_sigmobile4;
                    break;
                case 4:
                    tipoIcono = R.drawable.ic_sigmobile5;
                    break;
                case 0:
                    tipoIcono = R.drawable.ic_sigmobile1;
                    break;
            }
        }

        public int getTipoIcono() {
            return tipoIcono;
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

        txtTipoRed.setText(con.getType(info.getType(), info.getSubtype()));
        fab.setImageResource(MyListener.getTipoIcono());
        txtVersion.setText("Android " + Build.VERSION.RELEASE);
        txtModelo.setText(Build.MODEL);

        if (con.isConnectedWifi(getContext())) {


            txtNombreRed.setText(info.getExtraInfo());

            txtSeñal.setText(MyListener.getWifiStrength());

        } else if (con.isConnectedMobile(getContext())) {

            txtNombreRed.setText(tlfMan.getNetworkOperatorName());

            txtSeñal.setText(MyListener.getGsmStrength());

        } else {
            fab.setImageResource(R.drawable.ic_sigmobile0); //cambiar
            txtNombreRed.setText(tlfMan.getNetworkOperatorName());
            txtTipoRed.setText("-");
            txtSeñal.setText("-");
            txtSeñal.setText("-");
        }


    }

}
