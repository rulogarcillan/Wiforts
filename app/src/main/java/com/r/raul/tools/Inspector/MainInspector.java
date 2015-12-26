package com.r.raul.tools.Inspector;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.pwittchen.networkevents.library.BusWrapper;
import com.github.pwittchen.networkevents.library.NetworkEvents;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.r.raul.tools.R;
import com.r.raul.tools.Utils.Connectivity;
import com.r.raul.tools.Utils.LogUtils;
import com.r.raul.tools.Utils.Utilidades;
import com.squareup.otto.Subscribe;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

/**
 * Created by Rulo on 22/12/2015.
 */
public class MainInspector extends Fragment {

    public MainInspector() {

    }

    private BusWrapper busWrapper;
    private NetworkEvents networkEvents;
    private Connectivity con;
    private FrameLayout frameWifi, frameNoWifi;


    public void onResume() {
        super.onResume();
        busWrapper.register(this);
        networkEvents.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        busWrapper.unregister(this);
        networkEvents.unregister();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        final View rootView = inflater.inflate(R.layout.inspector_main, container, false);


        EventBus bus = new EventBus();

        busWrapper = getGreenRobotBusWrapper(bus);
        networkEvents = new NetworkEvents(getActivity(), busWrapper).enableWifiScan();

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.appbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        frameWifi = (FrameLayout) rootView.findViewById(R.id.frameWifi);
        frameNoWifi = (FrameLayout) rootView.findViewById(R.id.frameNoWifi);


        new Pruebas().execute();


        return rootView;
    }


    @Subscribe
    @SuppressWarnings("unused")
    public void onEvent(ConnectivityChanged event) {
        ocultaMuestra();
    }


    @NonNull
    private BusWrapper getGreenRobotBusWrapper(final EventBus bus) {
        return new BusWrapper() {
            @Override
            public void register(Object object) {
                bus.register(object);
                ocultaMuestra();

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

    private void ocultaMuestra() {

        if (con.isConnectedWifi(getActivity())) {
            frameWifi.setVisibility(View.VISIBLE);
            frameNoWifi.setVisibility(View.INVISIBLE);
        } else {
            frameWifi.setVisibility(View.INVISIBLE);
            frameNoWifi.setVisibility(View.VISIBLE);
        }
    }


    private class Pruebas extends AsyncTask<Void, Void, Void> {

        final static int NUMERO_HILOS = 100;
        final static int TIME_UP = 1000;

        @Override
        protected Void doInBackground(Void... params) {


            final ExecutorService es = Executors.newFixedThreadPool(NUMERO_HILOS);
            final List<Future<Machine>> futures = new ArrayList<Future<Machine>>();


            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

            String[] re = con.parseIP(wifiManager.getDhcpInfo().gateway).split("\\.");
            String range;
            if (re.length >= 3) {
                range = re[0] + "." + re[1] + "." + re[2];
            } else {
                LogUtils.LOGE("Ip mal formada");
                return null;
            }

            int[] bounds = Utilidades.ScanNet.rangeFromCidr(range + ".255/24");

            for (int i = bounds[0]; i <= bounds[1]; i++) {
                String address = Utilidades.ScanNet.InetRange.intToIp(i);
                InetAddress ip = null;
                try {
                    ip = InetAddress.getByName(address);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                if (!isCancelled()) {
                    futures.add(Utilidades.machineExist(es, ip,TIME_UP));
                } else {
                    es.shutdownNow();
                }
            }

            try {
                es.awaitTermination(TIME_UP, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (final Future<Machine> f : futures) {

                if (!isCancelled()) {
                    try {
                        if (f.get().isConectado()) {
                            LogUtils.LOGE(f.get().getIp());
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                } else {
                    es.shutdownNow();

                }
            }

            /*  try {
                new ScanNet().escaneaIps();
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            //also, this fails for an invalid address, like "www.sjdosgoogle.com1234sd"
           /* InetAddress[] addresses = new InetAddress[0];
            try {
                addresses = InetAddress.getAllByName("192.168.0.11");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            for (InetAddress address : addresses) {
                try {
                    if (address.isReachable(200))
                    {
                        System.out.println("Connected "+ address);
                    }
                    else
                    {
                        System.out.println("Failed "+address);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
/*
            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);


            InetAddress localhost = null;
            try {
                localhost = InetAddress.getByName(con.parseIP(wifiManager.getDhcpInfo().gateway));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            // this code assumes IPv4 is used
            byte[] ip = localhost.getAddress();

            for (int i = 1; i <= 254; i++) {
                ip[3] = (byte) i;
                InetAddress address = null;
                try {
                    address = InetAddress.getByAddress(ip);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                try {
                    if (address.isReachable(1000)) {
                        System.out.println(address + " machine is turned on and can be pinged");

                    } else if (!address.getHostAddress().equals(address.getHostName())) {
                        System.out.println(address + " machine is known in a DNS lookup");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
*/

            return null;
        }
    }


}
