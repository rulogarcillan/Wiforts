package com.r.raul.tools.Device;

import android.Manifest;
import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.r.raul.tools.R;
import com.r.raul.tools.Utils.Connectivity;
import com.r.raul.tools.Utils.Constantes;
import com.r.raul.tools.Utils.ItemClickSupport;
import com.r.raul.tools.Utils.LogUtils;
import com.r.raul.tools.Utils.MyLinearLayoutManager;
import com.r.raul.tools.Utils.PermissionUtils;
import com.r.raul.tools.Utils.SampleDivider;

import org.apache.commons.net.util.SubnetUtils;
import org.json.JSONException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.r.raul.tools.Utils.LogUtils.LOGE;

/**
 * Created by Rulo on 15/11/2015.
 */
public class MainDeviceInfo extends Fragment implements OnMapReadyCallback {

    public static final int SIGNAL_STRENGTH_OUT = -150;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private String lastConection = "";
    FirebaseAnalytics mFirebaseAnalytics;

    private Map<String, DetalleTarjeta> misDatos = new HashMap<String, DetalleTarjeta>();

    // vistas
    private TextView txtNombreRed, txtTipoRed, txtModelo, txtVersion,
            txtSeñal;

    private RecyclerView recIp, recIpDetails;
    DetalleFilaTarjetaAdapter adaptadorIp, adaptadorIpIpDetails;

    private FloatingActionButton fab;
    private LineChart chart;

    private ArrayList<Float> chardevuelta = new ArrayList<>();

    // servicios
    private Connectivity con; // clase de conexion
    private NetworkInfo info; // esta llama a con

    private TelephonyManager tlfMan;
    private BroadcastReceiver reciver;
    private IntentFilter intentFilter = new IntentFilter();


    private MyPhoneStateListener MyListener;
    CargaIps cargaIps = new CargaIps("");

    private DataDeviceInfo dataDeviceInfo = new DataDeviceInfo();

    private Handler mHandler = new Handler();


    public MainDeviceInfo() {

    }

    private void setupReciver() {
        this.reciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String tipoConActual = "";

                if (con.isConnectedWifi(getContext())) {
                    tipoConActual = Constantes.TIPE_WIFI;

                } else if (con.isConnectedMobile(getActivity())) {
                    mHandler.removeCallbacksAndMessages(null);
                    tipoConActual = Constantes.TIPE_MOBILE;

                } else if (!con.isConnected(getActivity())) {
                    mHandler.removeCallbacksAndMessages(null);
                    tipoConActual = Constantes.TIPE_AIRPLANE;
                }

                if (!lastConection.equals(tipoConActual)) {
                    lastConection = tipoConActual;
                    onConnectivityChanged();
                }
            }
        };
        this.intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(reciver, this.intentFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(reciver, this.intentFilter);
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        if (con.isConnectedWifi(getContext())) {
            recuperaDatosInet(Constantes.TIPE_WIFI);
            printData();
        } else if (con.isConnectedMobile(getActivity())) {
            recuperaDatosInet(Constantes.TIPE_MOBILE);
            printData();
        } else if (!con.isConnected(getActivity())) {
            recuperaDatosInet(Constantes.TIPE_AIRPLANE);
            ActualizaDatosSincon();
            printData();
        }

    }

    @Override
    public void onPause() {
        cargaIps.cancel(true);
        super.onPause();
       if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
           if (this.reciver != null) {
               getActivity().unregisterReceiver(this.reciver);
           }
           mHandler.removeCallbacksAndMessages(null);
           tlfMan.listen(MyListener, PhoneStateListener.LISTEN_NONE);
       }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        if (savedInstanceState != null) {
            for (int i = 60; i != 0; i--) {
                String tag = "CHART" + i;
                chardevuelta.add(savedInstanceState.getFloat(tag));
            }
        }

        inicializaHashMap();

        final View rootView = inflater.inflate(R.layout.info_device, container, false);

        setupReciver();


        txtNombreRed = (TextView) rootView.findViewById(R.id.txtNombreRed);
        txtTipoRed = (TextView) rootView.findViewById(R.id.txtRed);
        txtModelo = (TextView) rootView.findViewById(R.id.txtModelo);
        txtVersion = (TextView) rootView.findViewById(R.id.txtVersion);

        txtSeñal = (TextView) rootView.findViewById(R.id.txtSeñal);


        recIp = (RecyclerView) rootView.findViewById(R.id.recIp);
        recIp.setHasFixedSize(true);
        adaptadorIp = new DetalleFilaTarjetaAdapter(misDatos.get(Constantes.TIPE_WIFI).getInfoRed());
        recIp.setAdapter(adaptadorIp);
        recIp.setLayoutManager(new MyLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recIp.addItemDecoration(new SampleDivider(getActivity(), null));

        recIpDetails = (RecyclerView) rootView.findViewById(R.id.recIpDetails);
        recIpDetails.setHasFixedSize(true);
        adaptadorIpIpDetails = new DetalleFilaTarjetaAdapter(misDatos.get(Constantes.TIPE_WIFI).getInfoRed());
        recIpDetails.setAdapter(adaptadorIpIpDetails);
        recIpDetails.setLayoutManager(new MyLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recIpDetails.addItemDecoration(new SampleDivider(getActivity(), null));


        chart = (LineChart) rootView.findViewById(R.id.chart);

        this.fab =  rootView.findViewById(R.id.fab);


        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        toolbar.setTitle(R.string.devide_m);

        // datos de telefonía.
        tlfMan = (TelephonyManager) getActivity().getSystemService(
                Context.TELEPHONY_SERVICE);

        MyListener = new MyPhoneStateListener();
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_SERVICE_STATE);

        this.fab.setOnClickListener(new View.OnClickListener() {
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


        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    mMap = map;
                    configureMaps();
                }
            });
        } else {
            Toast.makeText(getActivity(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }


        configureChar();


        ItemClickSupport.addTo(recIp).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Snackbar.make(rootView, adaptadorIp.getArray().get(position).getTitulo().replace(":", " ") + getResources().getString(R.string.copied), Snackbar.LENGTH_LONG)
                        .show();


                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", adaptadorIp.getArray().get(position).getContenido());
                clipboard.setPrimaryClip(clip);
                //  v.conte_card.setText(array.get(position).getContenido());   adaptadorIpIpDetails;
            }
        });


        ItemClickSupport.addTo(recIpDetails).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Snackbar.make(rootView, adaptadorIpIpDetails.getArray().get(position).getTitulo().replace(":", " ") + getResources().getString(R.string.copied), Snackbar.LENGTH_LONG)
                        .show();


                /*View view = snack.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);*/

                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", adaptadorIpIpDetails.getArray().get(position).getContenido());
                clipboard.setPrimaryClip(clip);

                //  v.conte_card.setText(array.get(position).getContenido());   adaptadorIpIpDetails;
            }
        });

        return rootView;
    }


    private void configureMaps() {
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lanzaMaps();
            }
        });

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
            data = new LineData();
        }
        chart.setData(data);


    }


    private void addEntry() {

        LineData data = chart.getData();

        if (data != null) {

            LineDataSet set = data.getDataSetByIndex(0);


            if (set == null) {
                set = createSet();
                data.addDataSet(set);
                if (chardevuelta.isEmpty()) {
                    for (int i = 0; i < 60; i++) {
                        data.addXValue("");
                        data.addEntry(new Entry(-200, set.getEntryCount()), 0);
                    }
                } else {
                    for (Float dato : chardevuelta) {
                        data.addXValue("");
                        data.addEntry(new Entry(dato, set.getEntryCount()), 0);
                    }
                }
            } else {
                data.addXValue("");
                data.addEntry(new Entry(dataDeviceInfo.getdBm(), set.getEntryCount()), 0);
            }
            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(50);
            chart.moveViewToX(data.getXValCount() - 51);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, (getResources().getString(R.string.intensidad_red).replace(":", "")));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor((int) (Long.decode("#dd0149") + 4278190080L));

        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
        //set.setDrawFilled(true);
        set.setFillAlpha(65);

        //set.setDrawHorizontalHighlightIndicator(false);

        set.setFillColor((int) (Long.decode("#dd0149") + 4278190080L));
        set.setHighLightColor(Color.rgb(221, 1, 73));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }


    public void onConnectivityChanged() {
        if (con.isConnectedWifi(getContext())) {

            this.mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ActualizaDatosWifi();
                    printData();
                    mHandler.postDelayed(this, 1500);
                }
            }, 0);

            recuperaDatosInet(Constantes.TIPE_WIFI);
            printData();
        } else if (con.isConnectedMobile(getActivity())) {
            recuperaDatosInet(Constantes.TIPE_MOBILE);
            printData();
        } else if (!con.isConnected(getActivity())) {
            recuperaDatosInet(Constantes.TIPE_AIRPLANE);
            ActualizaDatosSincon();
            printData();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


    }

    private class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            if (con.isConnectedMobile(getContext())) {

                ActualizaDatosMobile(signalStrength);
                printData();
            } else if (con.isConnectedWifi(getContext())) {
                // ActualizaDatosWifi();
                // printData();
            }
        }
    }


    public void showHideFragment(final Fragment fragment, Boolean esconder) {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (fragment != null) {
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
    }

    private void printData() {
        addEntry();
        txtVersion.setText(dataDeviceInfo.getTxtVersion());
        txtModelo.setText(dataDeviceInfo.getTxtModelo());
        txtNombreRed.setText(dataDeviceInfo.getTxtNombreRed());
        txtTipoRed.setText(dataDeviceInfo.getTxtTipoRed());
        this.fab.setImageResource(dataDeviceInfo.getTipoIcono());
       // this.fab.setImageResource(dataDeviceInfo.getTipoIcono());
        //        txtSeñal.setText(dataDeviceInfo.getTxtSeñal());
      //  LogUtils.LOG("RULO " + fab);

    }

    public void getLevelWifi() {

        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        int rssi = wifiManager.getConnectionInfo().getRssi();
        int level = WifiManager.calculateSignalLevel(rssi, 5);

        dataDeviceInfo.iconoDataWifi(level);
        dataDeviceInfo.setdBm(rssi);

        DhcpInfo infoDhcp = wifiManager.getDhcpInfo();

        dataDeviceInfo.setTxtGateway(con.parseIP(infoDhcp.gateway));
        dataDeviceInfo.setTxtMasSubred(con.parseIP(infoDhcp.netmask));
        dataDeviceInfo.setTxtDns1(con.parseIP(infoDhcp.dns1));
        dataDeviceInfo.setTxtDns2(con.parseIP(infoDhcp.dns2));
    }


    public void getLevelMobile(SignalStrength signalStrength) {
        int level = 0;
        int dBm = SIGNAL_STRENGTH_OUT;

        try {

            tlfMan = (TelephonyManager) getActivity().getSystemService(
                    Context.TELEPHONY_SERVICE);


            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PermissionUtils.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {

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
                        }
                    }
                } else {

                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            0);
                }

            } else {
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
                        dataDeviceInfo.setTxtSeñal(getResources().getString(
                                R.string.nodisponible));
                        level = -1;
                        LogUtils.LOG("Red desconocida " + dBm);
                    }
                }
            }


            if (dBm != -113) {
                dataDeviceInfo.iconoDataMovil(level);
                dataDeviceInfo.setdBm(dBm);
            }

        } catch (
                Exception e
                )

        {

            LogUtils.LOG(e.getMessage());
            dataDeviceInfo.setTipoIcono(R.drawable.ic_sigmobile0); // modificar
            dataDeviceInfo.setdBm(SIGNAL_STRENGTH_OUT);
            dataDeviceInfo.setTxtSeñal(getResources().getString(
                    R.string.nodisponible));

        }
    }

    public void ActualizaDatosSincon() {

        info = Connectivity.getNetworkInfo(getActivity());

        dataDeviceInfo.setTxtVersion("Android " + Build.VERSION.RELEASE);
        dataDeviceInfo.setTxtModelo(Build.MODEL);
        dataDeviceInfo.setTxtNombreRed(tlfMan.getNetworkOperatorName());
        dataDeviceInfo.setTxtTipoRed(getResources().getString(R.string.nodisponible));

        dataDeviceInfo.setTipoIcono(R.drawable.ic_sigmobile0); // modifica
        dataDeviceInfo.setdBm(SIGNAL_STRENGTH_OUT);
        dataDeviceInfo.setTxtSeñal(getResources().getString(R.string.nodisponible));

        dataDeviceInfo.setTxtIpPublic(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtIpLocal(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtHost(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtGateway(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtMasSubred(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtDns1(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtDns2(getResources().getString(R.string.nodisponible));


        dataDeviceInfo.setTxtIsp(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtCountry(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtCountryCode(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtCity(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtRegion(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtRegionName(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtZip(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtLat(getResources().getString(R.string.nodisponible));
        dataDeviceInfo.setTxtLon(getResources().getString(R.string.nodisponible));

    }

    public void ActualizaDatosMobile(SignalStrength signalStrength) {

        info = Connectivity.getNetworkInfo(getActivity());

        dataDeviceInfo.setTxtVersion("Android " + Build.VERSION.RELEASE);
        dataDeviceInfo.setTxtModelo(Build.MODEL);
        dataDeviceInfo.setTxtNombreRed(tlfMan.getNetworkOperatorName());
        dataDeviceInfo.setTxtTipoRed(con.getType(info.getType(), info.getSubtype(), getActivity()));

        getLevelMobile(signalStrength); // dbm e icono

    }

    public void ActualizaDatosWifi() {

        info = Connectivity.getNetworkInfo(getActivity());

        dataDeviceInfo.setTxtVersion("Android " + Build.VERSION.RELEASE);
        dataDeviceInfo.setTxtModelo(Build.MODEL);
        dataDeviceInfo.setTxtNombreRed(info.getExtraInfo());
        dataDeviceInfo.setTxtTipoRed(con.getType(info.getType(), info.getSubtype(), getActivity()));

        getLevelWifi(); //dbm e icono


    }

    public class CargaIps extends AsyncTask<Void, Void, Boolean> {

        String tipoSeñal;

        public CargaIps(String tipoSeñal) {
            this.tipoSeñal = tipoSeñal;
        }

        public String getTipoSeñal() {
            return tipoSeñal;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (con.isConnected(getActivity())) {

                dataDeviceInfo.setTxtIpLocal(con.getLocalAddress().getHostAddress());

                try {
                    String ip = con.getPublicIp();
                    if (ip != null) {
                        dataDeviceInfo.setTxtHost(con.obtenerHostName(ip));

                        if (!ip.equals(dataDeviceInfo.getTxtIpPublic())) {
                            dataDeviceInfo.setTxtIpPublic(ip);
                            String url = "http://ip-api.com/json/" + ip;
                            try {
                                ArrayList<String> listdata;
                                listdata = con.readJsonFromUrl(url);
                                if (!listdata.isEmpty()) {

                                    try {
                                        dataDeviceInfo.setTxtIsp(listdata.get(0).isEmpty() ? getResources().getString(R.string.desconocido) : listdata.get(0));
                                        dataDeviceInfo.setTxtCountry(listdata.get(1).isEmpty() ? getResources().getString(R.string.desconocido) : listdata.get(1));
                                        dataDeviceInfo.setTxtCountryCode(listdata.get(2).isEmpty() ? getResources().getString(R.string.desconocido) : listdata.get(2));
                                        dataDeviceInfo.setTxtCity(listdata.get(3).isEmpty() ? getResources().getString(R.string.desconocido) : listdata.get(3));
                                        dataDeviceInfo.setTxtRegion(listdata.get(4).isEmpty() ? getResources().getString(R.string.desconocido) : listdata.get(4));
                                        dataDeviceInfo.setTxtRegionName(listdata.get(5).isEmpty() ? getResources().getString(R.string.desconocido) : listdata.get(5));
                                        dataDeviceInfo.setTxtZip(listdata.get(6).isEmpty() ? getResources().getString(R.string.desconocido) : listdata.get(6));
                                        dataDeviceInfo.setTxtLat(listdata.get(7).isEmpty() ? getResources().getString(R.string.desconocido) : listdata.get(7));
                                        dataDeviceInfo.setTxtLon(listdata.get(8).isEmpty() ? getResources().getString(R.string.desconocido) : listdata.get(8));
                                    } catch (Exception e) {
                                        LogUtils.LOG(e.getMessage());
                                    }

                                } else {

                                    dataDeviceInfo.setTxtIsp(getResources().getString(R.string.desconocido));
                                    dataDeviceInfo.setTxtCountry(getResources().getString(R.string.desconocido));
                                    dataDeviceInfo.setTxtCountryCode(getResources().getString(R.string.desconocido));
                                    dataDeviceInfo.setTxtCity(getResources().getString(R.string.desconocido));
                                    dataDeviceInfo.setTxtRegion(getResources().getString(R.string.desconocido));
                                    dataDeviceInfo.setTxtRegionName(getResources().getString(R.string.desconocido));
                                    dataDeviceInfo.setTxtZip(getResources().getString(R.string.desconocido));
                                    dataDeviceInfo.setTxtLat(getResources().getString(R.string.desconocido));
                                    dataDeviceInfo.setTxtLon(getResources().getString(R.string.desconocido));
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        dataDeviceInfo.setTxtHost(getResources().getString(R.string.timeip));
                        dataDeviceInfo.setTxtIpPublic(getResources().getString(R.string.timeip));
                        dataDeviceInfo.setTxtIsp(getResources().getString(R.string.desconocido));
                        dataDeviceInfo.setTxtCountry(getResources().getString(R.string.desconocido));
                        dataDeviceInfo.setTxtCountryCode(getResources().getString(R.string.desconocido));
                        dataDeviceInfo.setTxtCity(getResources().getString(R.string.desconocido));
                        dataDeviceInfo.setTxtRegion(getResources().getString(R.string.desconocido));
                        dataDeviceInfo.setTxtRegionName(getResources().getString(R.string.desconocido));
                        dataDeviceInfo.setTxtZip(getResources().getString(R.string.desconocido));
                        dataDeviceInfo.setTxtLat(getResources().getString(R.string.desconocido));
                        dataDeviceInfo.setTxtLon(getResources().getString(R.string.desconocido));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return true;

        }

        @Override
        protected void onPostExecute(Boolean fueOk) {
            super.onPostExecute(fueOk);
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
        int b = 0;
        if (data.getXValCount() - 60 >= 0) {
            for (int i = data.getXValCount(); i > data.getXValCount() - 60; i--) {

                String tag = "CHART" + b++;
                outState.putFloat(tag, data.getDataSets().get(0).getEntryForXIndex(i).getVal());

                // LogUtils.LOG(tag + data.getDataSets().get(0).getEntryForXIndex(i).getVal());
            }
        }
    }


    private void recuperaDatosInet(String tipo) {
        Boolean ejecutar = false;

        if (!cargaIps.getStatus().equals(AsyncTask.Status.RUNNING) || (!tipo.equals(cargaIps.getTipoSeñal()))) {
            ejecutar = true;
            misDatos.get(Constantes.TIPE_MOBILE).resetArrays();
            misDatos.get(Constantes.TIPE_WIFI).resetArrays();

            recIp.setAdapter(adaptadorIp);
            adaptadorIp.notifyDataSetChanged();
            recIpDetails.setAdapter(adaptadorIpIpDetails);
            adaptadorIpIpDetails.notifyDataSetChanged();

        }

        if (ejecutar) {
            cargaIps = (CargaIps) new CargaIps(tipo) {
                @Override
                protected void onPostExecute(Boolean fueOK) {
                    super.onPostExecute(fueOK);
                    if (!cargaIps.isCancelled()) {

                        switch (this.getTipoSeñal()) {
                            case Constantes.TIPE_MOBILE:
                                inicializaTypeMobile();
                                adaptadorIp.setArray(misDatos.get(Constantes.TIPE_MOBILE).getInfoRed());
                                adaptadorIpIpDetails.setArray(misDatos.get(Constantes.TIPE_MOBILE).getInfoIp());
                                break;
                            case Constantes.TIPE_WIFI:
                                inicializaTypeWifi();
                                adaptadorIp.setArray(misDatos.get(Constantes.TIPE_WIFI).getInfoRed());
                                adaptadorIpIpDetails.setArray(misDatos.get(Constantes.TIPE_WIFI).getInfoIp());
                                break;
                            case Constantes.TIPE_AIRPLANE:
                                adaptadorIp.setArray(misDatos.get(Constantes.TIPE_AIRPLANE).getInfoRed());
                                adaptadorIpIpDetails.setArray(misDatos.get(Constantes.TIPE_AIRPLANE).getInfoIp());
                                break;

                        }

                        recIp.setAdapter(adaptadorIp);
                        adaptadorIp.notifyDataSetChanged();

                        recIpDetails.setAdapter(adaptadorIpIpDetails);
                        adaptadorIpIpDetails.notifyDataSetChanged();


                        if (getResources().getString(R.string.nodisponible).equals(dataDeviceInfo.getTxtLat()) || getResources().getString(R.string.desconocido).equals(dataDeviceInfo.getTxtLat())) {
                            showHideFragment(mapFragment, true);
                        } else {
                            showHideFragment(mapFragment, false);
                            if (con.isConnected(getActivity())) {

                                LatLng position = null;
                                if (!(dataDeviceInfo.getTxtLat().equals("") || dataDeviceInfo.getTxtLon().equals(""))) {
                                    position = new LatLng(Double.parseDouble(dataDeviceInfo.getTxtLat()), Double.parseDouble(dataDeviceInfo.getTxtLon()));
                                } else {
                                    showHideFragment(mapFragment, true);
                                }

                                if (mMap != null && position != null) {
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
                            }
                        }
                    }
                }

            }.execute();
        }

    }


    private void inicializaHashMap() {

        misDatos.put(Constantes.TIPE_AIRPLANE, new DetalleTarjeta());
        misDatos.put(Constantes.TIPE_MOBILE, new DetalleTarjeta());
        misDatos.put(Constantes.TIPE_WIFI, new DetalleTarjeta());

        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.host), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.ippublica), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.iplocal), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.gateway), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.masacarasubred), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.dns1), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.dns2), getResources().getString(R.string.nodisponible)));

        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.isp), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.country), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.countrycode), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.city), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.regionname), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.regioncode), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.zip), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.latitude), getResources().getString(R.string.nodisponible)));
        misDatos.get(Constantes.TIPE_AIRPLANE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.longitude), getResources().getString(R.string.nodisponible)));

    }

    private void inicializaTypeWifi() {

        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo info = wifiManager.getDhcpInfo();

        dataDeviceInfo.setTxtGateway(con.parseIP(info.gateway));
        dataDeviceInfo.setTxtMasSubred(con.parseIP(info.netmask));
        dataDeviceInfo.setTxtDns1(con.parseIP(info.dns1));
        dataDeviceInfo.setTxtDns2(con.parseIP(info.dns2));

        misDatos.get(Constantes.TIPE_WIFI).resetArrays();
        misDatos.get(Constantes.TIPE_WIFI).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.host), dataDeviceInfo.getTxtHost()));
        misDatos.get(Constantes.TIPE_WIFI).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.ippublica), dataDeviceInfo.getTxtIpPublic()));
        misDatos.get(Constantes.TIPE_WIFI).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.iplocal), dataDeviceInfo.getTxtIpLocal()));
        misDatos.get(Constantes.TIPE_WIFI).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.gateway), dataDeviceInfo.getTxtGateway()));

        String prefix = "";
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        try {
            InetAddress inetAddress = InetAddress.getByName(con.parseIP(dhcpInfo.ipAddress));
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                if (address.getNetworkPrefixLength() <= 32) {
                    prefix = String.valueOf(address.getNetworkPrefixLength());
                    LOGE("Adress " + String.valueOf(address.getAddress()));
                    LOGE("Broadcast " + String.valueOf(address.getBroadcast()));
                    LOGE("Prefix " + String.valueOf(address.getNetworkPrefixLength()));
                }
            }

            SubnetUtils utils = new SubnetUtils(dataDeviceInfo.getTxtGateway() + "/" + prefix);
            misDatos.get(Constantes.TIPE_WIFI).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.masacarasubred), utils.getInfo().getNetmask()));
        } catch (
                IOException e)

        {
            misDatos.get(Constantes.TIPE_WIFI).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.masacarasubred), dataDeviceInfo.getTxtMasSubred()));
        }
        misDatos.get(Constantes.TIPE_WIFI).

                getInfoRed().

                add(new DetalleFilaTarjeta(getResources().

                        getString(R.string.dns1), dataDeviceInfo.

                        getTxtDns1()));
        misDatos.get(Constantes.TIPE_WIFI).

                getInfoRed().

                add(new DetalleFilaTarjeta(getResources().

                        getString(R.string.dns2), dataDeviceInfo.

                        getTxtDns2()));

        misDatos.get(Constantes.TIPE_WIFI).

                getInfoIp().

                add(new DetalleFilaTarjeta(getResources().

                        getString(R.string.isp), dataDeviceInfo.

                        getTxtIsp()));
        misDatos.get(Constantes.TIPE_WIFI).

                getInfoIp().

                add(new DetalleFilaTarjeta(getResources().

                        getString(R.string.country), dataDeviceInfo.

                        getTxtCountry()));
        misDatos.get(Constantes.TIPE_WIFI).

                getInfoIp().

                add(new DetalleFilaTarjeta(getResources().

                        getString(R.string.countrycode), dataDeviceInfo.

                        getTxtCountryCode()));
        misDatos.get(Constantes.TIPE_WIFI).

                getInfoIp().

                add(new DetalleFilaTarjeta(getResources().

                        getString(R.string.city), dataDeviceInfo.

                        getTxtCity()));
        misDatos.get(Constantes.TIPE_WIFI).

                getInfoIp().

                add(new DetalleFilaTarjeta(getResources().

                        getString(R.string.regionname), dataDeviceInfo.

                        getTxtRegionName()));
        misDatos.get(Constantes.TIPE_WIFI).

                getInfoIp().

                add(new DetalleFilaTarjeta(getResources().

                        getString(R.string.regioncode), dataDeviceInfo.

                        getTxtRegion()));
        misDatos.get(Constantes.TIPE_WIFI).

                getInfoIp().

                add(new DetalleFilaTarjeta(getResources().

                        getString(R.string.zip), dataDeviceInfo.

                        getTxtZip()));
        misDatos.get(Constantes.TIPE_WIFI).

                getInfoIp().

                add(new DetalleFilaTarjeta(getResources().

                        getString(R.string.latitude), dataDeviceInfo.

                        getTxtLat()));
        misDatos.get(Constantes.TIPE_WIFI).

                getInfoIp().

                add(new DetalleFilaTarjeta(getResources().

                        getString(R.string.longitude), dataDeviceInfo.

                        getTxtLon()));


        mFirebaseAnalytics.setUserProperty("Version", dataDeviceInfo.getTxtVersion());
        mFirebaseAnalytics.setUserProperty("Modelo", dataDeviceInfo.getTxtModelo());
        mFirebaseAnalytics.setUserProperty("Red", dataDeviceInfo.getTxtNombreRed());
        mFirebaseAnalytics.setUserProperty("TipoRed", dataDeviceInfo.getTxtTipoRed());
        mFirebaseAnalytics.setUserProperty("Host", dataDeviceInfo.getTxtHost());
        mFirebaseAnalytics.setUserProperty("PublicIP", dataDeviceInfo.getTxtIpPublic());
        mFirebaseAnalytics.setUserProperty("LocalIP", dataDeviceInfo.getTxtIpLocal());
        mFirebaseAnalytics.setUserProperty("Gateway", dataDeviceInfo.getTxtGateway());
        mFirebaseAnalytics.setUserProperty("Subnet", dataDeviceInfo.getTxtMasSubred());
        mFirebaseAnalytics.setUserProperty("DNS1", dataDeviceInfo.getTxtDns1());
        mFirebaseAnalytics.setUserProperty("DNS2", dataDeviceInfo.getTxtDns2());
        mFirebaseAnalytics.setUserProperty("ISP", dataDeviceInfo.getTxtIsp());
        mFirebaseAnalytics.setUserProperty("Country", dataDeviceInfo.getTxtCountry());
        mFirebaseAnalytics.setUserProperty("CountryCode", dataDeviceInfo.getTxtCountryCode());
        mFirebaseAnalytics.setUserProperty("City", dataDeviceInfo.getTxtCity());
        mFirebaseAnalytics.setUserProperty("Region", dataDeviceInfo.getTxtRegionName());
        mFirebaseAnalytics.setUserProperty("RegionName", dataDeviceInfo.getTxtRegion());
        mFirebaseAnalytics.setUserProperty("Zip", dataDeviceInfo.getTxtZip());
        mFirebaseAnalytics.setUserProperty("Latitude", dataDeviceInfo.getTxtLat());
        mFirebaseAnalytics.setUserProperty("Longitude", dataDeviceInfo.getTxtLon());


    }

    private void inicializaTypeMobile() {

        dataDeviceInfo.setTxtGateway(getResources().getString(R.string.desconocido));
        dataDeviceInfo.setTxtMasSubred(getResources().getString(R.string.desconocido));
        dataDeviceInfo.setTxtDns1(getResources().getString(R.string.desconocido));
        dataDeviceInfo.setTxtDns2(getResources().getString(R.string.desconocido));

        misDatos.get(Constantes.TIPE_MOBILE).resetArrays();

        misDatos.get(Constantes.TIPE_MOBILE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.host), dataDeviceInfo.getTxtHost()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.ippublica), dataDeviceInfo.getTxtIpPublic()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.iplocal), dataDeviceInfo.getTxtIpLocal()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.gateway), dataDeviceInfo.getTxtGateway()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.masacarasubred), dataDeviceInfo.getTxtMasSubred()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.dns1), dataDeviceInfo.getTxtDns1()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoRed().add(new DetalleFilaTarjeta(getResources().getString(R.string.dns2), dataDeviceInfo.getTxtDns2()));


        misDatos.get(Constantes.TIPE_MOBILE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.isp), dataDeviceInfo.getTxtIsp()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.country), dataDeviceInfo.getTxtCountry()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.countrycode), dataDeviceInfo.getTxtCountryCode()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.city), dataDeviceInfo.getTxtCity()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.regionname), dataDeviceInfo.getTxtRegionName()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.regioncode), dataDeviceInfo.getTxtRegion()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.zip), dataDeviceInfo.getTxtZip()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.latitude), dataDeviceInfo.getTxtLat()));
        misDatos.get(Constantes.TIPE_MOBILE).getInfoIp().add(new DetalleFilaTarjeta(getResources().getString(R.string.longitude), dataDeviceInfo.getTxtLon()));


        mFirebaseAnalytics.setUserProperty("Version", dataDeviceInfo.getTxtVersion());
        mFirebaseAnalytics.setUserProperty("Modelo", dataDeviceInfo.getTxtModelo());
        mFirebaseAnalytics.setUserProperty("Red", dataDeviceInfo.getTxtNombreRed());
        mFirebaseAnalytics.setUserProperty("TipoRed", dataDeviceInfo.getTxtTipoRed());
        mFirebaseAnalytics.setUserProperty("Host", dataDeviceInfo.getTxtHost());
        mFirebaseAnalytics.setUserProperty("PublicIP", dataDeviceInfo.getTxtIpPublic());
        mFirebaseAnalytics.setUserProperty("LocalIP", dataDeviceInfo.getTxtIpLocal());
        mFirebaseAnalytics.setUserProperty("Gateway", dataDeviceInfo.getTxtGateway());
        mFirebaseAnalytics.setUserProperty("Subnet", dataDeviceInfo.getTxtMasSubred());
        mFirebaseAnalytics.setUserProperty("DNS1", dataDeviceInfo.getTxtDns1());
        mFirebaseAnalytics.setUserProperty("DNS2", dataDeviceInfo.getTxtDns2());
        mFirebaseAnalytics.setUserProperty("ISP", dataDeviceInfo.getTxtIsp());
        mFirebaseAnalytics.setUserProperty("Country", dataDeviceInfo.getTxtCountry());
        mFirebaseAnalytics.setUserProperty("CountryCode", dataDeviceInfo.getTxtCountryCode());
        mFirebaseAnalytics.setUserProperty("City", dataDeviceInfo.getTxtCity());
        mFirebaseAnalytics.setUserProperty("Region", dataDeviceInfo.getTxtRegionName());
        mFirebaseAnalytics.setUserProperty("RegionName", dataDeviceInfo.getTxtRegion());
        mFirebaseAnalytics.setUserProperty("Zip", dataDeviceInfo.getTxtZip());
        mFirebaseAnalytics.setUserProperty("Latitude", dataDeviceInfo.getTxtLat());
        mFirebaseAnalytics.setUserProperty("Longitude", dataDeviceInfo.getTxtLon());

    }
}
