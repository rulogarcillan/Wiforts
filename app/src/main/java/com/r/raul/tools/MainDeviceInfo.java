package com.r.raul.tools;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.pwittchen.networkevents.library.BusWrapper;
import com.github.pwittchen.networkevents.library.NetworkEvents;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.github.pwittchen.networkevents.library.event.WifiSignalStrengthChanged;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Rulo on 15/11/2015.
 */
public class MainDeviceInfo extends Fragment {

    public static final int SIGNAL_STRENGTH_OUT = -150;
    private GoogleMap mMap;
    SupportMapFragment fragment;

    // vistas
    private TextView txtNombreRed, txtTipoRed, txtModelo, txtVersion, txtHost,
            txtIpPublic, txtIpLocal, txtSeñal, txtGateway, txtMasSubred,
            txtDns1, txtDns2, txtIsp, txtCountry, txtCountryCode, txtCity, txtRegion, txtRegionName, txtZip, txtLat, txtLon;

    private FloatingActionButton fab;
    private LineChart chart;

    private ArrayList<Float> chardevuelta = new ArrayList<>();

    // servicios
    private Connectivity con; // clase de conexion
    private NetworkInfo info; // esta llama a con

    private TelephonyManager tlfMan;
    private BusWrapper busWrapper;
    private NetworkEvents networkEvents;
    //
    private MyPhoneStateListener MyListener;

    private DataDeviceInfo dataDeviceInfo = new DataDeviceInfo();


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
        if (savedInstanceState != null) {
            for (int i = 50; i != 0; i--) {
                String tag = "CHART" + i;
                chardevuelta.add(savedInstanceState.getFloat("tag"));
            }
        }


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


        txtHost = (TextView) rootView.findViewById(R.id.txtHost);
        txtDns1 = (TextView) rootView.findViewById(R.id.txtDns1);
        txtDns2 = (TextView) rootView.findViewById(R.id.txtDns2);
        txtMasSubred = (TextView) rootView.findViewById(R.id.txtMasSubred);
        txtGateway = (TextView) rootView.findViewById(R.id.txtGateway);

        txtIsp = (TextView) rootView.findViewById(R.id.txtIsp);
        txtCountry = (TextView) rootView.findViewById(R.id.txtCountry);
        txtCountryCode = (TextView) rootView.findViewById(R.id.txtCountryCode);
        txtCity = (TextView) rootView.findViewById(R.id.txtCity);
        txtRegion = (TextView) rootView.findViewById(R.id.txtRegion);
        txtRegionName = (TextView) rootView.findViewById(R.id.txtRegionName);
        txtZip = (TextView) rootView.findViewById(R.id.txtZip);
        txtLat = (TextView) rootView.findViewById(R.id.txtLat);
        txtLon = (TextView) rootView.findViewById(R.id.txtLon);


        chart = (LineChart) rootView.findViewById(R.id.chart);

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

        CardView cardIp = (CardView) rootView.findViewById(R.id.cardIp);
        cardIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzaMaps();
            }
        });

        android.support.v4.app.FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);


        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }

        this.mMap = fragment.getMap();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lanzaMaps();
            }
        });
        configureMaps();
        configureChar();

        return rootView;
    }


    private void configureMaps() {
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

    }

    private void configureChar() {


        //especificaciones del chart
        chart.setDescription("");
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setWillNotDraw(false);
        chart.setPinchZoom(false);

        //Limites del eje Y
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setAxisMaxValue(0f);
        leftAxis.setAxisMinValue(-150f);
        leftAxis.setStartAtZero(false);
        chart.getAxisRight().setEnabled(false);

        //Leyenda
        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.CIRCLE);


        //Añado data
        LineData data = chart.getData();


        if (data == null) {
            LogUtils.LOG("Es nuevo");
            data = new LineData();
        }
        chart.setData(data);


    }


    private void addEntry() {

        LineData data = chart.getData();

        if (data != null) {

            LineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
                if (chardevuelta.isEmpty()) {

                    for (int i = 0; i < 50; i++) {
                        data.addXValue("");
                        data.addEntry(new Entry(-200, set.getEntryCount()), 0);
                    }
                } else {
                    for (Float dato : chardevuelta ) {
                        data.addXValue("");
                        data.addEntry(new Entry(dato, set.getEntryCount()), 0);


                    }
                }

            }

            data.addXValue("");

            data.addEntry(new Entry(dataDeviceInfo.getdBm(), set.getEntryCount()), 0);

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(50);
            // move to the latest entry
            chart.moveViewToX(data.getXValCount() - 51);


        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, (getActivity().getString(R.string.intensidad_red).replace(":", "")));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor((int) (Long.decode("#FF4081") + 4278190080L));

        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
        //set.setDrawFilled(true);
        set.setFillAlpha(65);

        //set.setDrawHorizontalHighlightIndicator(false);

        set.setFillColor((int) (Long.decode("#FF4081") + 4278190080L));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
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
            } else if (con.isConnectedWifi(getContext())) {
                ActualizaDatosWifi();
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

    public void showHideFragment(final Fragment fragment, Boolean esconder) {

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (esconder) {
            ft.hide(fragment);

        } else {
            ft.show(fragment);
        }
        try {
            ft.commit();
        } catch (Exception e) {

        }

    }

    private void printData() {


        addEntry();
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
                //if (aVoid) {
                txtIpPublic.setText(dataDeviceInfo.getTxtIpPublic());
                txtHost.setText(dataDeviceInfo.getTxtHost());
                txtIsp.setText(dataDeviceInfo.getTxtIsp());
                txtCountry.setText(dataDeviceInfo.getTxtCountry());
                txtCountryCode.setText(dataDeviceInfo.getTxtCountryCode());
                txtCity.setText(dataDeviceInfo.getTxtCity());
                txtRegion.setText(dataDeviceInfo.getTxtRegion());
                txtRegionName.setText(dataDeviceInfo.getTxtRegionName());
                txtZip.setText(dataDeviceInfo.getTxtZip());
                txtLat.setText(dataDeviceInfo.getTxtLat());
                txtLon.setText(dataDeviceInfo.getTxtLon());


                if (dataDeviceInfo.getTxtLat().equals(getActivity().getString(R.string.nodisponible)) || dataDeviceInfo.getTxtLat().equals(getActivity().getString(R.string.desconocido))) {
                    showHideFragment(fragment, true);
                } else {
                    showHideFragment(fragment, false);
                    if (con.isConnected(getActivity())) {

                        LatLng position = new LatLng(Double.parseDouble(dataDeviceInfo.getTxtLat()), Double.parseDouble(dataDeviceInfo.getTxtLon()));

                        mMap.clear();

                        mMap.addMarker(new MarkerOptions().position(position).title(dataDeviceInfo.getTxtIpPublic())).showInfoWindow();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16));
                        mMap.addCircle(new CircleOptions()
                                .center(position)
                                .radius(100)
                                .strokeWidth((float) 0.5)
                                .strokeColor(getResources().getColor(R.color.colorAccentTra))
                                .fillColor(getResources().getColor(R.color.colorAccentTra)));

                    }
                    // }
                }
                txtIpLocal.setText(dataDeviceInfo.getTxtIpLocal());
            }

        }.execute();

        txtGateway.setText(dataDeviceInfo.getTxtGateway());
        txtMasSubred.setText(dataDeviceInfo.getTxtMasSubred());
        txtDns1.setText(dataDeviceInfo.getTxtDns1());
        txtDns2.setText(dataDeviceInfo.getTxtDns2());

    }

    public void getLevelWifi() {

        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(
                Context.WIFI_SERVICE);
        DhcpInfo info = wifiManager.getDhcpInfo();

        int rssi = wifiManager.getConnectionInfo().getRssi();
        int level = WifiManager.calculateSignalLevel(rssi, 5);

        dataDeviceInfo.iconoDataWifi(level);
        dataDeviceInfo.setdBm(rssi);

        dataDeviceInfo.setTxtGateway(con.parseIP(info.gateway));
        dataDeviceInfo.setTxtMasSubred(con.parseIP(info.netmask));
        dataDeviceInfo.setTxtDns1(con.parseIP(info.dns1));
        dataDeviceInfo.setTxtDns2(con.parseIP(info.dns2));

		/*
         * List<ScanResult> wifiList = wifiManager.getScanResults(); for
		 * (ScanResult scanResult : wifiList) { int level =
		 * WifiManager.calculateSignalLevel(scanResult.level, 5); }
		 */

    }


    public void getLevelMobile(SignalStrength signalStrength) {
        int level = 0;
        int dBm = SIGNAL_STRENGTH_OUT;

        try {
            tlfMan = (TelephonyManager) getActivity().getSystemService(
                    Context.TELEPHONY_SERVICE);

            for (CellInfo info2 : tlfMan.getAllCellInfo()) {
                if (info2 instanceof CellInfoGsm) {

                    CellSignalStrengthGsm gsm = ((CellInfoGsm) info2).getCellSignalStrength();
                    level = gsm.getLevel();
                    dBm = gsm.getDbm();
                    LogUtils.LOG("GSM " + dBm);

                } else if (info2 instanceof CellInfoCdma) {
                    CellSignalStrengthCdma cdma = ((CellInfoCdma) info2).getCellSignalStrength();

                    level = cdma.getLevel();
                    dBm = cdma.getDbm();
                    LogUtils.LOG("cdma1 " + dBm);

                } else if (info2 instanceof CellInfoLte) {

                    CellSignalStrengthLte lte = ((CellInfoLte) info2).getCellSignalStrength();
                    dBm = lte.getDbm();
                    level = lte.getLevel();
                    LogUtils.LOG("lte " + dBm);

                } else if (info2 instanceof CellInfoWcdma) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info2)
                                .getCellSignalStrength();

                        dBm = wcdma.getDbm();
                        level = wcdma.getLevel();
                        LogUtils.LOG("cdma2 " + dBm);

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
                        LogUtils.LOG("cdma2 api < " + dBm);
                    }

                } else {

                    dBm = SIGNAL_STRENGTH_OUT;
                    dataDeviceInfo.setTxtSeñal(getActivity().getString(
                            R.string.nodisponible));
                    level = -1;
                    LogUtils.LOG("Red desconocida " + dBm);
                }
            }
            if (dBm != -113) {
                dataDeviceInfo.iconoDataMovil(level);
                dataDeviceInfo.setdBm(dBm);
            }

        } catch (Exception e) {

            LogUtils.LOG(e.getMessage());
            dataDeviceInfo.setTipoIcono(R.drawable.ic_sigmobile0); // modificar
            dataDeviceInfo.setdBm(SIGNAL_STRENGTH_OUT);
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
        dataDeviceInfo.setdBm(SIGNAL_STRENGTH_OUT);
        dataDeviceInfo.setTxtSeñal(getActivity().getString(R.string.nodisponible));

        dataDeviceInfo.setTxtIpPublic(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtIpLocal(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtHost(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtGateway(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtMasSubred(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtDns1(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtDns2(getActivity().getString(R.string.nodisponible));


        dataDeviceInfo.setTxtIsp(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtCountry(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtCountryCode(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtCity(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtRegion(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtRegionName(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtZip(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtLat(getActivity().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtLon(getActivity().getString(R.string.nodisponible));

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
        dataDeviceInfo.setTxtGateway(getActivity().getString(R.string.desconocido));
        dataDeviceInfo.setTxtMasSubred(getActivity().getString(R.string.desconocido));
        dataDeviceInfo.setTxtDns1(getActivity().getString(R.string.desconocido));
        dataDeviceInfo.setTxtDns2(getActivity().getString(R.string.desconocido));

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
        //dataDeviceInfo.setTxtGateway(getActivity().getString(R.string.nodisponible));
        //dataDeviceInfo.setTxtMasSubred(getActivity().getString(R.string.nodisponible));
        //dataDeviceInfo.setTxtDns1(getActivity().getString(R.string.nodisponible));
        //dataDeviceInfo.setTxtDns2(getActivity().getString(R.string.nodisponible));

    }

    public class cargaIps extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            if (con.isConnected(getActivity())) {

                dataDeviceInfo.setTxtIpLocal(con.getLocalAddress().getHostAddress());

                try {
                    String ip = con.getPublicIp();

                    if (!ip.equals(dataDeviceInfo.getTxtIpPublic())) {
                        dataDeviceInfo.setTxtIpPublic(ip);
                        String url = "http://ip-api.com/json/" + ip;
                        try {
                            ArrayList<String> listdata = new ArrayList<String>();
                            listdata = con.readJsonFromUrl(url);
                            if (!listdata.isEmpty()) {

                                try {
                                    dataDeviceInfo.setTxtIsp(listdata.get(0).isEmpty() ? getActivity().getString(R.string.desconocido) : listdata.get(0));
                                    dataDeviceInfo.setTxtCountry(listdata.get(1).isEmpty() ? getActivity().getString(R.string.desconocido) : listdata.get(1));
                                    dataDeviceInfo.setTxtCountryCode(listdata.get(2).isEmpty() ? getActivity().getString(R.string.desconocido) : listdata.get(2));
                                    dataDeviceInfo.setTxtCity(listdata.get(3).isEmpty() ? getActivity().getString(R.string.desconocido) : listdata.get(3));
                                    dataDeviceInfo.setTxtRegion(listdata.get(4).isEmpty() ? getActivity().getString(R.string.desconocido) : listdata.get(4));
                                    dataDeviceInfo.setTxtRegionName(listdata.get(5).isEmpty() ? getActivity().getString(R.string.desconocido) : listdata.get(5));
                                    dataDeviceInfo.setTxtZip(listdata.get(6).isEmpty() ? getActivity().getString(R.string.desconocido) : listdata.get(6));
                                    dataDeviceInfo.setTxtLat(listdata.get(7).isEmpty() ? getActivity().getString(R.string.desconocido) : listdata.get(7));
                                    dataDeviceInfo.setTxtLon(listdata.get(8).isEmpty() ? getActivity().getString(R.string.desconocido) : listdata.get(8));
                                } catch (Exception e) {
                                    LogUtils.LOG(e.getMessage());
                                }

                            } else {

                                dataDeviceInfo.setTxtIsp(getActivity().getString(R.string.desconocido));
                                dataDeviceInfo.setTxtCountry(getActivity().getString(R.string.desconocido));
                                dataDeviceInfo.setTxtCountryCode(getActivity().getString(R.string.desconocido));
                                dataDeviceInfo.setTxtCity(getActivity().getString(R.string.desconocido));
                                dataDeviceInfo.setTxtRegion(getActivity().getString(R.string.desconocido));
                                dataDeviceInfo.setTxtRegionName(getActivity().getString(R.string.desconocido));
                                dataDeviceInfo.setTxtZip(getActivity().getString(R.string.desconocido));
                                dataDeviceInfo.setTxtLat(getActivity().getString(R.string.desconocido));
                                dataDeviceInfo.setTxtLon(getActivity().getString(R.string.desconocido));
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return true;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                return true;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean actualiza) {
            super.onPostExecute(actualiza);
        }
    }


    private void lanzaMaps() {


        Uri gmmIntentUri = Uri.parse("geo:" + dataDeviceInfo.getTxtLat() + "," + dataDeviceInfo.getTxtLon() + "?q=" + dataDeviceInfo.getTxtLat() + "," + dataDeviceInfo.getTxtLon());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        LineData data = chart.getData();
        for (int i = data.getXValCount(); i > data.getXValCount() - 50; i--) {
            String tag = "CHART" + i;
            outState.putFloat(tag, data.getDataSets().get(0).getEntryForXIndex(i).getVal());

            LogUtils.LOG(data.getDataSets().get(0).getEntryForXIndex(i).getVal()+"bb");
        }
    }
}
