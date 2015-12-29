package com.r.raul.tools.Utils;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.r.raul.tools.DB.Consultas;
import com.r.raul.tools.Inspector.InspectorTable;
import com.r.raul.tools.Inspector.Machine;
import com.r.raul.tools.R;

import org.apache.commons.net.util.SubnetUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
    private Consultas consultas;


    public ObtenMaquinas(Activity ac, ArrayList<Machine> array) {

        this.ac = ac;
        this.array = array;
        this.consultas = new Consultas(ac);
    }

    private int calculoPercent(int valor, int tot) {
        return (valor * 100 / tot);
    }

    @Override
    protected Void doInBackground(Void... params) {
    	
    	final ExecutorService es = Executors.newFixedThreadPool(NUMERO_HILOS);
        final List<Future<Machine>> futures = new ArrayList<Future<Machine>>();
        
        WifiManager wifiManager = (WifiManager) ac.getSystemService(Context.WIFI_SERVICE);
        final String macPadre = wifiManager.getConnectionInfo().getBSSID();
        final String macMyDevice = wifiManager.getConnectionInfo().getMacAddress();
        final String gateway = con.parseIP(wifiManager.getDhcpInfo().gateway);
        final String subMask = con.parseIP(wifiManager.getDhcpInfo().netmask);
        final String loacalIp = con.getLocalAddress().getHostAddress();

	//consultamos las conexiones guardadas para la mac padre
        ArrayList<InspectorTable> arrayInspectorTable = consultas.getAllInspectorTableFromMacPadre(macPadre);
        
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
                    	
                    	Boolean isGateway=false;
                    	Boolean isInBBDD=false;
                    	Boolean isMyDevice=false;
                    	
                    	LogUtils.LOGI(f.get().getIp());

                        f.get().setMacPadre(macPadre); //padre
                        f.get().setMac(getMacFromArpCache(f.get().getIp())); //propia o hija

                    	if (f.get().getIp().equals(gateway)) {
                            isGateway=true;
                            f.get().setTipoImg(Constantes.TIPE_GATEWAY);
                    	}else if (f.get().getIp().equals(loacalIp)){
                    	    isMyDevice=true;			
                    	    f.get().setTipoImg(Constantes.TIPE_DEVICE);
                            f.get().setMac(macMyDevice); //propia o hija
                        } else {
                            f.get().setTipoImg(Constantes.TIPE_OTHERS);
                        }

                    	
                    	for(InspectorTable item : arrayInspectorTable){
                    		if(f.get().getMac().equals(item.getMacdevice())){
                    			isInBBDD=true;
                    			f.get().setNombre(item.getNombre());
                    			f.get().setConocido(item.getFavorito());
                    			break;
                    		}
                    	}
                    	if (!isInBBDD){
                    		InspectorTable itemIns = new InspectorTable(f.get().getMac(), macPadre, "", (isGateway || isMyDevice) ? true : false);
                    		consultas.setItemInspectorTable(itemIns);
                    		arrayInspectorTable.add(itemIns);
                    		f.get().setNombre("");
                    		f.get().setConocido((isGateway||isMyDevice) ? true:false);
                    	}
                    	
                    	//agregamos el nombre del hardware
                    	f.get().setNombreSoft(consultas.getNameFromMac(f.get().getMac()));
                        
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
                        return ac.getString(R.string.desconocido);
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
        return ac.getString(R.string.desconocido);
	}

}
