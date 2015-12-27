package com.r.raul.tools.Utils;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.r.raul.tools.Inspector.Machine;

import org.apache.commons.net.util.SubnetUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ObtenMaquinas extends AsyncTask<Void, Integer, Void> {

    final static int NUMERO_HILOS = 100;
    final static int TIME_UP = 1000;

    private Activity ac;
    private Connectivity con;
    private ArrayList<Machine> array;
    private ProgressBar progressBar;


    public ObtenMaquinas(Activity ac, ArrayList<Machine> array) {

        this.ac = ac;
        this.array = array;
    }

    private int calculoPercent(int valor, int tot) {
        return (valor * 100 / tot);
    }

    @Override
    protected Void doInBackground(Void... params) {

        WifiManager wifiManager = (WifiManager) ac.getSystemService(Context.WIFI_SERVICE);
        String gateway = con.parseIP(wifiManager.getDhcpInfo().gateway);
        String subMask = con.parseIP(wifiManager.getDhcpInfo().netmask);


        final ExecutorService es = Executors.newFixedThreadPool(NUMERO_HILOS);
        final List<Future<Machine>> futures = new ArrayList<Future<Machine>>();


        SubnetUtils utils = new SubnetUtils(gateway, subMask);
        SubnetUtils.SubnetInfo info = utils.getInfo();



        String[] addresses = utils.getInfo().getAllAddresses();

        for (String ipS : addresses) {

            InetAddress ip = null;
            try {
                ip = InetAddress.getByName(ipS);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if (!isCancelled()) {
                futures.add(Utilidades.machineExist(es, ip, TIME_UP));
            } else {
                es.shutdownNow();
            }
        }

        try {
            es.awaitTermination(TIME_UP, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int tot=0;
        for (final Future<Machine> f : futures) {
            tot++;
            if (!isCancelled()) {
                try {
                    if (f.get().isConectado()) {
                        LogUtils.LOGE(f.get().getIp());
                        if (f.get().getIp().equals(gateway)) {
                            f.get().setTipoImg(Constantes.TIPE_GATEWAY);
                        } else {
                            f.get().setTipoImg(Constantes.TIPE_OTHERS);
                        }
                        array.add(f.get());
                    }

                } catch (InterruptedException e) {
                    LogUtils.LOGI(e.getMessage());
                } catch (ExecutionException e) {
                    LogUtils.LOGE(e.getMessage());
                }
                publishProgress(calculoPercent(tot,addresses.length));

            } else {
                es.shutdownNow();
            }
        }


        return null;
    }

}
