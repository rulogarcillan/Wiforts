package com.r.raul.tools.Utils;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
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
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jcifs.Config;
import jcifs.netbios.NbtAddress;

import static com.r.raul.tools.Utils.LogUtils.LOGE;

public class ObtenMaquinas extends AsyncTask<Void, Integer, Void> {

    final static int NUMERO_HILOS = 100;
    final static int TIME_UP = 1000;

    private Activity ac;
    private Connectivity con;
    private ArrayList<Machine> array;
    private ProgressBar progressBar;
    private Consultas consultas;
  //  private final ExecutorService es = Executors.newFixedThreadPool(NUMERO_HILOS);
    private final List<Future<Machine>> futures = new ArrayList<Future<Machine>>();

    private static final int CORE_POOL_SIZE = 100;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 1;
    public static final Executor THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());


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

        WifiManager wifiManager = (WifiManager) ac.getSystemService(Context.WIFI_SERVICE);
        final String macPadre = wifiManager.getConnectionInfo().getBSSID();
        final String macMyDevice = wifiManager.getConnectionInfo().getMacAddress();
        final String gateway = con.parseIP(wifiManager.getDhcpInfo().gateway);
        final String subMask = con.parseIP(wifiManager.getDhcpInfo().netmask);
        final String loacalIp = con.getLocalAddress().getHostAddress();
        String prefix = "";
        //consultamos las conexiones guardadas para la mac padre
        ArrayList<InspectorTable> arrayInspectorTable = consultas.getAllInspectorTableFromMacPadre(macPadre);


        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        try {
            InetAddress inetAddress = InetAddress.getByName(con.parseIP(dhcpInfo.ipAddress));
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                prefix = String.valueOf(address.getNetworkPrefixLength());
            }
        } catch (IOException e) {

        }

        SubnetUtils utils = new SubnetUtils(gateway, subMask);
        SubnetUtils.SubnetInfo info = utils.getInfo();
        String[] addresses;
        try {
            addresses = utils.getInfo().getAllAddresses();
            LOGE("BIEN");
        } catch (Exception e) {
            LOGE("MAL");
            utils = new SubnetUtils(gateway + "/" + prefix);
            addresses = utils.getInfo().getAllAddresses();
        }


        for (String ip : addresses) {
            InetAddress ipA = null;
            try {
                ipA = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if (!isCancelled()) {
                futures.add(Utilidades.machineExist((ExecutorService) THREAD_POOL_EXECUTOR, ipA, TIME_UP));
            } else {

            }
        }



        int tot = 0; //total ips analizadas para barra de progreso
        for (final Future<Machine> f : futures) {
            tot++;
            if (!isCancelled()) {
                try {
                    if (f.get().isConectado()) {

                        Boolean isGateway = false;
                        Boolean isInBBDD = false;
                        Boolean isMyDevice = false;

                        LogUtils.LOGI(f.get().getIp());

                        f.get().setMacPadre(macPadre); //padre
                        f.get().setMac(getMacFromArpCache(f.get().getIp())); //propia o hija

                        if (f.get().getIp().equals(gateway)) {
                            isGateway = true;
                            f.get().setTipoImg(Constantes.TIPE_GATEWAY);
                        } else if (f.get().getIp().equals(loacalIp)) {
                            isMyDevice = true;
                            f.get().setTipoImg(Constantes.TIPE_DEVICE);
                            f.get().setMac(macMyDevice); //propia o hija
                        } else {
                            f.get().setTipoImg(Constantes.TIPE_OTHERS);
                        }

                        publishProgress(calculoPercent(tot, addresses.length));
                        for (InspectorTable item : arrayInspectorTable) {
                            if (f.get().getMac().equals(item.getMacdevice())) {
                                isInBBDD = true;
                                f.get().setNombre(item.getNombre());
                                f.get().setConocido(item.getFavorito());
                                break;
                            }
                        }
                        if (!isInBBDD) {
                            InspectorTable itemIns = new InspectorTable(f.get().getMac(), macPadre, "", (isGateway || isMyDevice) ? true : false);
                            consultas.setItemInspectorTable(itemIns);
                            arrayInspectorTable.add(itemIns);
                            f.get().setNombre("");
                            f.get().setConocido((isGateway || isMyDevice) ? true : false);
                        }

                        //agregamos el nombre del hardware
                        NbtAddress[] nbts = new NbtAddress[0];
                        try {
                            publishProgress(calculoPercent(tot, addresses.length));
                            Config.setProperty("jcifs.smb.client.soTimeout", "100");
                            Config.setProperty("jcifs.smb.client.responseTimeout", "100");
                            Config.setProperty("jcifs.netbios.soTimeout", "100");
                            Config.setProperty("jcifs.netbios.retryTimeout", "100");

                            nbts = NbtAddress.getAllByAddress(f.get().getIp());
                            String netbiosname = nbts[0].getHostName();

                            f.get().setNombre(netbiosname);
                        } catch (UnknownHostException e) {
                            f.get().setNombre("-");
                            if (isMyDevice) {
                                f.get().setNombre(ac.getString(R.string.midevice));
                            }
                            e.printStackTrace();
                        }

                        f.get().setNombreSoft(consultas.getNameFromMac(f.get().getMac()));

                        array.add(f.get());
                        publishProgress(calculoPercent(tot, addresses.length));
                    }

                } catch (InterruptedException e) {
                    LogUtils.LOGI(e.getMessage());
                } catch (ExecutionException e) {
                    LOGE(e.getMessage());
                }
                publishProgress(calculoPercent(tot, addresses.length));
            } else {
              //  es.shutdownNow();
            }
        }

        return null;
    }

    @Override
    protected void onCancelled() {
       // es.shutdownNow();
        super.onCancelled();
    }

    @Override
    protected void onCancelled(Void aVoid) {
       // es.shutdownNow();
        super.onCancelled(aVoid);
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
