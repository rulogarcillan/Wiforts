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

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import de.greenrobot.event.EventBus;


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

    private BusWrapper busWrapper;
    private NetworkEvents networkEvents;

    //
    private MyPhoneStateListener MyListener;
    private String wifiStrength = "";
    private int tipoIcono;


    public static MainDeviceInfo newInstance() {
        MainDeviceInfo fragment = new MainDeviceInfo();
        return fragment;
    }

    public MainDeviceInfo() {
    }


    @Subscribe
    @SuppressWarnings("unused")
    public void onEvent(ConnectivityChanged event) {
        if (con.isConnectedWifi(getContext())) {
            getLevelWifi(getContext());
            printData();
        } else if (!con.isConnected(getActivity())) {
            printData();
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onEvent(WifiSignalStrengthChanged event) {

        if (con.isConnectedWifi(getContext())) {
            getLevelWifi(getContext());
            printData();
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        View rootView = inflater.inflate(R.layout.info_device, container, false);

        EventBus bus = new EventBus();

        busWrapper = getGreenRobotBusWrapper(bus);
        networkEvents = new NetworkEvents(getActivity(), busWrapper).enableWifiScan();


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
                } else if (con.isConnectedMobile(getContext())) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.android.settings.Settings$DataUsageSummaryActivity"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
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

        private int tipoIcono;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            if (con.isConnectedMobile(getContext())) {
                try {

                    tlfMan = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                    for (CellInfo info2 : tlfMan.getAllCellInfo()) {
                        if (info2 instanceof CellInfoGsm && con.getType(info.getType(), info.getSubtype(), getActivity()) != "4G | LTE") {
                            CellSignalStrengthGsm gsm = ((CellInfoGsm) info2).getCellSignalStrength();
                            iconoGsm(gsm.getLevel());

                            gsmStrength = String.valueOf(gsm.getDbm()) + " dBm";
                            // LogUtils.LOG("GSM " + gsmStrength);
                        } else if (info2 instanceof CellInfoCdma) {
                            CellSignalStrengthCdma cdma = ((CellInfoCdma) info2).getCellSignalStrength();
                            iconoGsm(cdma.getLevel());
                            gsmStrength = String.valueOf(cdma.getDbm()) + " dBm";
                            // LogUtils.LOG("cdma " + gsmStrength);
                        } else if (info2 instanceof CellInfoLte) {
                            CellSignalStrengthLte lte = ((CellInfoLte) info2).getCellSignalStrength();
                            gsmStrength = String.valueOf(lte.getDbm()) + " dBm";
                            iconoGsm(lte.getLevel());
                            // LogUtils.LOG("lte " + gsmStrength);
                        } else if (info2 instanceof CellInfoWcdma) {
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info2).getCellSignalStrength();
                                gsmStrength = String.valueOf(wcdma.getDbm()) + " dBm";
                                iconoGsm(wcdma.getLevel());
                                //  LogUtils.LOG("cdma " + gsmStrength);
                            } else {

                                gsmStrength = String.valueOf(signalStrength.getGsmSignalStrength() * 2 - 113) + " dBm";
                            }

                        } else {
                            if (con.getType(info.getType(), info.getSubtype(), getActivity()) == "4G | LTE") {

                            } else {
                                gsmStrength = String.valueOf("Uknow");
                                iconoGsm(-1);
                            }

                        }
                    }
                } catch (Exception e) {
                    LogUtils.LOG(e.getMessage());
                }

                printData();
            }
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
        busWrapper.register(this);
        networkEvents.register();
    }

    @Override
    public void onStart() {
        super.onStart();

    }


    @Override
    public void onStop() {
        super.onStop();

    }


    @Override
    public void onPause() {
        super.onPause();
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_NONE);
        busWrapper.unregister(this);
        networkEvents.unregister();
    }


    private void printData() {


        info = Connectivity.getNetworkInfo(getActivity());

        if (con.isConnectedWifi(getContext())) {

            fab.setImageResource(tipoIcono);
            txtNombreRed.setText(info.getExtraInfo());
            txtTipoRed.setText(con.getType(info.getType(), info.getSubtype(), getActivity()));
            txtSeñal.setText(wifiStrength);
            //ips y red

            new cargaIps().execute();


            txtIpPublic.setText(R.string.nodisponible);
            txtDns1.setText(R.string.nodisponible);
            txtDns2.setText(R.string.nodisponible);
            txtMasSubred.setText(R.string.nodisponible);
            txtGateway.setText(R.string.nodisponible);

        } else if (con.isConnectedMobile(getContext())) {
            fab.setImageResource(MyListener.getTipoIcono());
            txtNombreRed.setText(tlfMan.getNetworkOperatorName());
            txtTipoRed.setText(con.getType(info.getType(), info.getSubtype(), getActivity()));
            txtSeñal.setText(MyListener.getGsmStrength());
            //ips y red
            new cargaIps().execute();
            txtIpPublic.setText(R.string.nodisponible);
            txtDns1.setText(R.string.nodisponible);
            txtDns2.setText(R.string.nodisponible);
            txtMasSubred.setText(R.string.nodisponible);
            txtGateway.setText(R.string.nodisponible);

        } else {
            //red y señal
            fab.setImageResource(R.drawable.ic_sigmobile0); //cambiar
            txtNombreRed.setText(tlfMan.getNetworkOperatorName());
            txtTipoRed.setText(R.string.nodisponible);
            txtSeñal.setText(R.string.nodisponible);

            //ips y red
            txtIpLocal.setText(R.string.nodisponible);
            txtIpPublic.setText(R.string.nodisponible);
            txtDns1.setText(R.string.nodisponible);
            txtDns2.setText(R.string.nodisponible);
            txtMasSubred.setText(R.string.nodisponible);
            txtGateway.setText(R.string.nodisponible);


        }

        txtVersion.setText("Android " + Build.VERSION.RELEASE);
        txtModelo.setText(Build.MODEL);
    }

    public class cargaIps extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            String retorno;
            InetAddress IP = null;
            try {
                IP = InetAddress.getLocalHost();
                retorno = getLocalAddress().getHostAddress();


            } catch (UnknownHostException e) {
                e.printStackTrace();
                retorno = getActivity().getString(R.string.nodisponible);
            }


            return retorno;
        }

        @Override
        protected void onPostExecute(String result) {
            txtIpLocal.setText(result);
        }

        private InetAddress getLocalAddress() {
            try {
                Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
                while (b.hasMoreElements()) {
                    for (InterfaceAddress f : b.nextElement().getInterfaceAddresses())
                        if (f.getAddress().isSiteLocalAddress())
                            return f.getAddress();
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return null;
        }


    }

    public void getLevelWifi(Context context) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        /*List<ScanResult> wifiList = wifiManager.getScanResults();
        for (ScanResult scanResult : wifiList) {
            int level = WifiManager.calculateSignalLevel(scanResult.level, 5);
        }*/

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

}
