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
	
        for (String ip : addresses) {
            InetAddress ipA = null;
            try {
                ipA = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if (!isCancelled()) {
                futures.add(Utilidades.machineExist(es, ipA, TIME_UP));
            } else {
                es.shutdownNow();
            }
        }

        try {
            es.awaitTermination(TIME_UP, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int tot = 0; //total ips analizadas para barra de progreso
        for (final Future<Machine> f : futures) {
            tot++;
            if (!isCancelled()) {
                try {
                    if (f.get().isConectado()) {
                        LogUtils.LOGI(f.get().getIp());
                        if (f.get().getIp().equals(gateway)) {
                            f.get().setTipoImg(Constantes.TIPE_GATEWAY);
                        } else {
                            f.get().setTipoImg(Constantes.TIPE_OTHERS);
                        }
                        f.get().setMac(getMacFromArpCache(f.get().getIp()));
                        array.add(f.get());
                    }

                } catch (InterruptedException e) {
                    LogUtils.LOGI(e.getMessage());
                } catch (ExecutionException e) {
                    LogUtils.LOGE(e.getMessage());
                }
                publishProgress(calculoPercent(tot, addresses.length));

            } else {
                es.shutdownNow();
            }
        }

        return null;
    }
    
    
    private String getMacFromArpCache(String ip) {
		if (ip == null)
			return null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(" +");
				if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
					String mac = splitted[3];
					if (mac.matches("..:..:..:..:..:..")) {
						return mac;
					} else {
						return null;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
