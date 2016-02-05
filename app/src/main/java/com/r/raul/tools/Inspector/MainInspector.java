package com.r.raul.tools.Inspector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.r.raul.tools.R;
import com.r.raul.tools.Utils.Connectivity;
import com.r.raul.tools.Utils.SampleDivider;
import com.r.raul.tools.Utils.Utilidades;

import java.util.ArrayList;

import static com.r.raul.tools.Utils.LogUtils.LOGE;

/**
 * Created by Rulo on 22/12/2015.
 */
public class MainInspector extends Fragment {

    public MainInspector() {

    }

    private BroadcastReceiver reciver;
    private IntentFilter intentFilter = new IntentFilter();

    private Connectivity con;
    private SwipeRefreshLayout frameWifi;
    private ObtenMaquinas task;
    private ArrayList<Machine> array = new ArrayList<>();
    private RecyclerView recWifis;
    private MachineAdapter adaptador;
    private ProgressBar progressBar;
    private TextView Txtbssid, TxtCon, TxtTot;
    private ImageView imgDevice;
    private View rootView;

    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(reciver, this.intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();

        if (this.reciver != null) {
            getActivity().unregisterReceiver(this.reciver);
        }

        if (task != null) {
            task.cancel(true);
        }
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.inspector_main, container, false);

        setupReciver();


        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.appbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        TxtTot = (TextView) rootView.findViewById(R.id.TxtTotDev);
        TxtCon = (TextView) rootView.findViewById(R.id.TxtCon);
        Txtbssid = (TextView) rootView.findViewById(R.id.Txtbssid);

        TxtTot.setText(array.size() + "");

        frameWifi = (SwipeRefreshLayout) rootView.findViewById(R.id.frameWifi);
        recWifis = (RecyclerView) rootView.findViewById(R.id.recWifis);

        imgDevice = (ImageView) rootView.findViewById(R.id.imgDevice);

        adaptador = new MachineAdapter(getActivity(), array);

        recWifis.setHasFixedSize(true);
        recWifis.setAdapter(adaptador);
        recWifis.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        SampleDivider a = new SampleDivider(getActivity(), null);
        a.setmShowLastDivider(true);
        recWifis.addItemDecoration(a);

        imgDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));

            }
        });

        frameWifi.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorPrimary);

        frameWifi.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Utilidades.lanzaVibracion(getActivity(), 100);
                ejecutarTask();
            }
        });
        ejecutarTask();


        return rootView;
    }

    private void ejecutarTask() {

        if (con.isConnectedWifi(getActivity()) && ((task != null && task.getStatus() != AsyncTask.Status.RUNNING) || task == null)) {

            task = (ObtenMaquinas) new ObtenMaquinas(getActivity(), array) {


                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    array.clear();
                    adaptador.notifyDataSetChanged();
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(0);
                    progressBar.setIndeterminate(true);
                    TxtTot.setText(array.size() + "");
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    progressBar.setIndeterminate(false);
                    super.onProgressUpdate(values[0]);

                    if (progressBar.getProgress()<values[0]){
                        progressBar.setProgress(values[0]);
                    }
                    if (Integer.parseInt(TxtTot.getText().toString()) != array.size()) {
                        adaptador.notifyDataSetChanged();
                        TxtTot.setText(array.size() + "");
                    }

                    frameWifi.setRefreshing(false);
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    progressBar.setVisibility(View.INVISIBLE);
                    adaptador.notifyDataSetChanged();
                    TxtTot.setText(array.size() + "");
                    Utilidades.lanzaVibracion(getActivity(), 500);
                }
            }.execute();
        } else if (!con.isConnectedWifi(getActivity())) {
            Snackbar.make(rootView, getResources().getString(R.string.sinconexionamp), Snackbar.LENGTH_LONG).show();
            frameWifi.setRefreshing(false);
        } else {

            frameWifi.setRefreshing(false);
        }
    }


    private void ocultaMuestra() {

        if (con.isConnectedWifi(getActivity())) {
            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            final String macPadre = wifiManager.getConnectionInfo().getBSSID();
            final String wifi = wifiManager.getConnectionInfo().getSSID();
            Txtbssid.setText(macPadre);
            TxtCon.setText(wifi.replace("\"", ""));
            imgDevice.setImageResource(R.drawable.icon_wifisi);
        } else {
            Txtbssid.setText(getActivity().getString(R.string.sinconexionamp));
            TxtCon.setText(getActivity().getString(R.string.sinconexion));
            imgDevice.setImageResource(R.drawable.icon_wifino);
            if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                progressBar.setVisibility(View.INVISIBLE);
                adaptador.notifyDataSetChanged();
                task.cancel(true);
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adaptador = new MachineAdapter(getActivity(), array);
        recWifis.setAdapter(adaptador);
        adaptador.notifyDataSetChanged();

    }


    private void setupReciver() {
        this.reciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                if (info != null) {
                    if (info.isConnected()) {
                        LOGE(String.valueOf(Connectivity.isConnectedWifi(context)));
                        ocultaMuestra();
                    } else {
                        ocultaMuestra();
                    }
                }
            }
        };
        this.intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(reciver, this.intentFilter);
    }


}
