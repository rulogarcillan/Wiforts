package com.r.raul.tools;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.pwittchen.networkevents.library.BusWrapper;
import com.github.pwittchen.networkevents.library.NetworkEvents;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.github.pwittchen.networkevents.library.event.WifiSignalStrengthChanged;
import com.squareup.otto.Subscribe;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * Created by Rulo on 15/11/2015.
 */
public class MainDeviceInfo extends Fragment {

    // vistas
    private TextView txtNombreRed, txtTipoRed, txtModelo, txtVersion,
            txtIpPublic, txtIpLocal, txtSeñal, txtGateway, txtMasSubred,
            txtDns1, txtDns2;
    private FloatingActionButton fab;

    // servicios
    private Connectivity con; // clase de conexion
    private NetworkInfo info; // esta llama a con

    private TelephonyManager tlfMan;
    private BusWrapper busWrapper;
    private NetworkEvents networkEvents;
    //
    private MyPhoneStateListener MyListener;

    private DataDeviceInfo dataDeviceInfo = new DataDeviceInfo();

    public static MainDeviceInfo newInstance() {
        MainDeviceInfo fragment = new MainDeviceInfo();
        return fragment;
    }

    public MainDeviceInfo() {
    }

    @Override
    public void onResume() {
        super.onResume();
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        busWrapper.register(this);
        networkEvents.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_NONE);
        busWrapper.unregister(this);
        networkEvents.unregister();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View rootView = inflater
                .inflate(R.layout.info_device, container, false);

        EventBus bus = new EventBus();

        busWrapper = getGreenRobotBusWrapper(bus);
        networkEvents = new NetworkEvents(getActivity(), busWrapper)
                .enableWifiScan();

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
        // datos de telefonía.
        tlfMan = (TelephonyManager) getActivity().getSystemService(
                Context.TELEPHONY_SERVICE);

        MyListener = new MyPhoneStateListener();
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_SERVICE_STATE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (con.isConnectedWifi(getContext())) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                } else if (con.isConnectedMobile(getContext())) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent
                            .setComponent(new ComponentName(
                                    "com.android.settings",
                                    "com.android.settings.Settings$DataUsageSummaryActivity"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent
                            .setComponent(new ComponentName(
                                    "com.android.settings",
                                    "com.android.settings.Settings$DataUsageSummaryActivity"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });

        // printData();

        return rootView;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onEvent(ConnectivityChanged event) {
        if (con.isConnectedWifi(getContext())) {
            ActualizaDatosWifi();
            printData();
        } else if (!con.isConnected(getActivity())) {
            ActualizaDatosSincon();
            printData();
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onEvent(WifiSignalStrengthChanged event) {

        if (con.isConnectedWifi(getContext())) {
            ActualizaDatosWifi();
            printData();
        }
    }

    private class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            if (con.isConnectedMobile(getContext())) {

                ActualizaDatosMobile(signalStrength);
                printData();
            }
        }
    }

    @NonNull
    private BusWrapper getGreenRobotBusWrapper(final EventBus bus) {
        return new BusWrapper() {
            @Override
            public void register(Object object) {
                bus.register(object);
            }

            @Override
            public void unregister(Object object) {
                bus.unregister(object);
            }

            @Override
            public void post(Object event) {
                bus.post(event);
            }
        };
    }

    private void printData() {

        txtVersion.setText(dataDeviceInfo.getTxtVersion());
        txtModelo.setText(dataDeviceInfo.getTxtModelo());
        txtNombreRed.setText(dataDeviceInfo.getTxtNombreRed());
        txtTipoRed.setText(dataDeviceInfo.getTxtTipoRed());

        fab.setImageResource(dataDeviceInfo.getTipoIcono());
        txtSeñal.setText(dataDeviceInfo.getTxtSeñal());

        new cargaIps() {


            @Override
            protected void onPostExecute(Boolean aVoid) {
                super.onPostExecute(aVoid);
                if (aVoid){
                    txtIpPublic.setText(dataDeviceInfo.getTxtIpPublic());
                }
                txtIpLocal.setText(dataDeviceInfo.getTxtIpLocal());
            }

        }
                .execute();

        txtGateway.setText(dataDeviceInfo.getTxtGateway());
        txtMasSubred.setText(dataDeviceInfo.getTxtMasSubred());
        txtDns1.setText(dataDeviceInfo.getTxtDns1());
        txtDns2.setText(dataDeviceInfo.getTxtDns2());

    }

    public void getLevelWifi() {

        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(
                Context.WIFI_SERVICE);

        int rssi = wifiManager.getConnectionInfo().getRssi();
        int level = WifiManager.calculateSignalLevel(rssi, 5);

        dataDeviceInfo.iconoDataWifi(level);
        dataDeviceInfo.setdBm(rssi);

		/*
         * List<ScanResult> wifiList = wifiManager.getScanResults(); for
		 * (ScanResult scanResult : wifiList) { int level =
		 * WifiManager.calculateSignalLevel(scanResult.level, 5); }
		 */

    }

    public void getLevelMobile(SignalStrength signalStrength) {
        int level = 0;
        int dBm = 0;

        try {
            tlfMan = (TelephonyManager) getActivity().getSystemService(
                    Context.TELEPHONY_SERVICE);
            for (CellInfo info2 : tlfMan.getAllCellInfo()) {
                if (info2 instanceof CellInfoGsm
                        && con.getType(info.getType(), info.getSubtype(),
                        getActivity()) != "4G | LTE") {

                    CellSignalStrengthGsm gsm = ((CellInfoGsm) info2).getCellSignalStrength();
                    level = gsm.getLevel();

                    dBm = gsm.getDbm();
                    // LogUtils.LOG("GSM " + gsmStrength);
                } else if (info2 instanceof CellInfoCdma) {
                    CellSignalStrengthCdma cdma = ((CellInfoCdma) info2).getCellSignalStrength();

                    level = cdma.getLevel();
                    dBm = cdma.getDbm();
                    // LogUtils.LOG("cdma " + gsmStrength);
                } else if (info2 instanceof CellInfoLte) {
                    CellSignalStrengthLte lte = ((CellInfoLte) info2).getCellSignalStrength();
                    dBm = lte.getDbm();
                    level = lte.getLevel();
                    // LogUtils.LOG("lte " + gsmStrength);
                } else if (info2 instanceof CellInfoWcdma) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info2)
                                .getCellSignalStrength();
                        dBm = wcdma.getDbm();
                        level = wcdma.getLevel();
                        // LogUtils.LOG("cdma " + gsmStrength);
                    } else {
                        dBm = signalStrength.getGsmSignalStrength() * 2 - 113;

                        if (dBm <= -113) {
                            level = -1;
                        } else if (dBm <= -111) {
                            level = 0;
                        } else if (dBm <= -97) {
                            level = 1;
                        } else if (dBm <= -87) {
                            level = 2;
                        } else if (dBm <= -71) {
                            level = 3;
                        } else if (dBm > -71) {
                            level = 4;
                        }
                    }

                } else {
                    if (con.getType(info.getType(), info.getSubtype(),
                            getActivity()) != "4G | LTE") {
                        dBm = 0;
                        dataDeviceInfo.setTxtSeñal(getActivity().getString(
                                R.string.nodisponible));
                        level = -1;
                    }

                }
            }

            dataDeviceInfo.iconoDataMovil(level);
            dataDeviceInfo.setdBm(dBm);

        } catch (Exception e) {

            LogUtils.LOG(e.getMessage());
            dataDeviceInfo.setTipoIcono(R.drawable.ic_sigmobile0); // modificar
            dataDeviceInfo.setdBm(0);
            dataDeviceInfo.setTxtSeñal(getActivity().getString(
                    R.string.nodisponible));

        }
    }

    public void ActualizaDatosSincon() {

        info = Connectivity.getNetworkInfo(getActivity());

        dataDeviceInfo.setTxtVersion("Android " + Build.VERSION.RELEASE);
        dataDeviceInfo.setTxtModelo(Build.MODEL);
        dataDeviceInfo.setTxtNombreRed(tlfMan.getNetworkOperatorName());
        dataDeviceInfo.setTxtTipoRed(getActivity().getString(R.string.nodisponible));

        dataDeviceInfo.setTipoIcono(R.drawable.ic_sigmobile0); // modifica
        dataDeviceInfo.setdBm(0);
        dataDeviceInfo.setTxtSeñal(getActivity().getString(R.string.nodisponible));

        dataDeviceInfo.setTxtIpPublic(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtIpLocal(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtGateway(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtMasSubred(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtDns1(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtDns2(getActivity().getString(R.string.nodisponible));

    }

    public void ActualizaDatosMobile(SignalStrength signalStrength) {

        info = Connectivity.getNetworkInfo(getActivity());

        dataDeviceInfo.setTxtVersion("Android " + Build.VERSION.RELEASE);
        dataDeviceInfo.setTxtModelo(Build.MODEL);
        dataDeviceInfo.setTxtNombreRed(tlfMan.getNetworkOperatorName());
        dataDeviceInfo.setTxtTipoRed(con.getType(info.getType(), info.getSubtype(), getActivity()));

        getLevelMobile(signalStrength); // dbm e icono

        //dataDeviceInfo.setTxtIpPublic(getActivity().getString(R.string.nodisponible));
        //dataDeviceInfo.setTxtIpLocal(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtGateway(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtMasSubred(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtDns1(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtDns2(getActivity().getString(R.string.nodisponible));

    }

    public void ActualizaDatosWifi() {

        info = Connectivity.getNetworkInfo(getActivity());

        dataDeviceInfo.setTxtVersion("Android " + Build.VERSION.RELEASE);
        dataDeviceInfo.setTxtModelo(Build.MODEL);
        dataDeviceInfo.setTxtNombreRed(info.getExtraInfo());
        dataDeviceInfo.setTxtTipoRed(con.getType(info.getType(), info.getSubtype(), getActivity()));

        getLevelWifi(); //dbm e icono

        //dataDeviceInfo.setTxtIpPublic(getActivity().getString(R.string.nodisponible));
        //dataDeviceInfo.setTxtIpLocal(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtGateway(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtMasSubred(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtDns1(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtDns2(getActivity().getString(R.string.nodisponible));

    }

    public class cargaIps extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {


            dataDeviceInfo.setTxtIpLocal(con.getLocalAddress().getHostAddress());


            try {
                String ip = con.getPublicIp();
                if (ip != dataDeviceInfo.getTxtIpPublic())
                    dataDeviceInfo.setTxtIpPublic(ip);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean actualiza) {
            super.onPostExecute(actualiza);
        }
    }

}
