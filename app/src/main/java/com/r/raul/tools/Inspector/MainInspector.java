package com.r.raul.tools.Inspector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.r.raul.tools.DB.Consultas;
import com.r.raul.tools.R;
import com.r.raul.tools.Utils.Connectivity;
import com.r.raul.tools.Utils.ItemClickSupport;
import com.r.raul.tools.Utils.SampleDivider;
import com.r.raul.tools.Utils.Utilidades;

import org.apache.commons.net.util.SubnetUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
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
    private int totales = 0;

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

        ItemClickSupport.addTo(recWifis).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {

            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                setNombre(array.get(position));
            }
        });


        return rootView;
    }

    private void setNombre(final Machine item){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater(getArguments());
        final View dialogView = inflater.inflate(R.layout.dialog_edittext, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.editName);

        dialogBuilder.setTitle(R.string.custom_name);
        dialogBuilder.setMessage(R.string.enter_name);

        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                Consultas consultas = new Consultas(getActivity());
                consultas.upDeviceNombre(edt.getText().toString(), item.getMac());
                item.setNombre(!edt.getText().toString().equals("") ? edt.getText().toString():"-");
                adaptador.notifyDataSetChanged();
            }
        });
        dialogBuilder.setNeutralButton(R.string.no_name, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Consultas consultas = new Consultas(getActivity());
                consultas.upDeviceNombre("", item.getMac());
                item.setNombre("-");
                adaptador.notifyDataSetChanged();
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void ejecutarTask() {

        if (con.isConnectedWifi(getActivity()) && ((task != null && task.getStatus() != AsyncTask.Status.RUNNING) || task == null)) {

            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            String prefix = "";
            try {
                InetAddress inetAddress = InetAddress.getByName(con.parseIP(dhcpInfo.ipAddress));
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                    prefix = String.valueOf(address.getNetworkPrefixLength());
                    LOGE("Adress " + String.valueOf(address.getAddress()));
                    LOGE("Broadcast " + String.valueOf(address.getBroadcast()));
                    LOGE("Prefix " + String.valueOf(address.getNetworkPrefixLength()));
                }


            } catch (IOException e) {

            }

            SubnetUtils utils = new SubnetUtils(con.getLocalAddress().getHostAddress() + "/" + prefix);
            final String tot = utils.getInfo().getAllAddresses().length + "";


            task = (ObtenMaquinas) new ObtenMaquinas(getActivity(), array) {


                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    array.clear();
                    adaptador.notifyDataSetChanged();
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(0);
                    progressBar.setIndeterminate(true);
                    TxtTot.setText(array.size() + "/" + tot);
                    totales = array.size();
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    progressBar.setIndeterminate(false);
                    super.onProgressUpdate(values[0]);

                    if (progressBar.getProgress() < values[0]) {

                        progressBar.setProgress(values[0]);
                    }
                    if (totales != array.size()) {
                        totales = array.size();
                        TxtTot.setText(array.size() + "/" + tot);
                        adaptador.notifyDataSetChanged();
                    }

                    frameWifi.setRefreshing(false);
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    progressBar.setVisibility(View.INVISIBLE);
                    adaptador.notifyDataSetChanged();
                    TxtTot.setText(array.size() + "/" + tot);
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
