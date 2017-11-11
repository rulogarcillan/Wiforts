package com.r.raul.tools.Inspector;


import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.text.Html;

import com.r.raul.tools.DB.Consultas;
import com.r.raul.tools.DB.MyDatabase;
import com.r.raul.tools.R;
import com.r.raul.tools.Utils.Connectivity;
import com.r.raul.tools.Utils.Constantes;

import org.apache.commons.net.util.SubnetUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jcifs.Config;
import jcifs.netbios.NbtAddress;

import static com.r.raul.tools.Utils.LogUtils.LOGE;
import static com.r.raul.tools.Utils.LogUtils.LOGI;

public class IpScan {
    private ScanResult scanResult;
    private static final int DEFAULT_TIME_OUT = 250;
    private static final int DEFAULT_FIXED_POOL = 36;
    private ExecutorService pool;

    private Connectivity con;
    private String macPadre;
    private String gateway;
    private String loacalIp;
    private String macMyDevice;
    private ArrayList<InspectorTable> arrayInspectorTable;
    private Consultas consultas;
    private String[] addresses;
    private Activity ac;


    public IpScan(ScanResult scanResult, Activity activity) {
        this.scanResult = scanResult;
        this.ac = activity;
        //se crea la base de datos si no existe.
        new MyDatabase(activity);
        if (this.ac != null) {
            WifiManager wifiManager = (WifiManager) this.ac.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            this.macPadre = wifiManager.getConnectionInfo().getBSSID();
            this.macMyDevice = wifiManager.getConnectionInfo().getMacAddress();
            this.gateway = con.parseIP(wifiManager.getDhcpInfo().gateway);
            this.loacalIp = con.getLocalAddress().getHostAddress();
            this.consultas = new Consultas(activity);

            //datos de BBDD sobre la mac del wifi
            arrayInspectorTable = this.consultas.getAllInspectorTableFromMacPadre(macPadre);

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


            } catch (IOException e) {

            }
            SubnetUtils utils = new SubnetUtils(gateway + "/" + prefix);
            LOGE("Subnet " + utils.getInfo().getNetmask());
            this.addresses = utils.getInfo().getAllAddresses();
            LOGE("TOTAL IPS: " + addresses.length);

        }
    }

    public int totalIpsAnalizar() {
        return addresses.length;
    }

    public void scanAll() {

        pool = Executors.newFixedThreadPool(DEFAULT_FIXED_POOL);
        for (String ip : addresses) {
            launch(ip);
        }
        pool.shutdown();
        try {
            if (!pool.awaitTermination(3600, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        if (pool != null) {
            pool.shutdownNow();
            pool.shutdown();
        }
    }


    public Boolean isFinish() {
        return pool.isTerminated();
    }

    private void launch(String i) {
        if (!pool.isShutdown()) {
            pool.execute(new SingleRunnable(i, scanResult));
        }
    }

    public void scanSingleIp(String ip, int timeout) {
        try {
            final String CMD = "/system/bin/ping -q -n -w 1 -c 3 %s";
            Process myProcess = Runtime.getRuntime().exec(String.format(CMD, ip));
            myProcess.waitFor();
            if (myProcess.exitValue() == 0) {
                LOGI("IP OK Ping " + ip);
                ipEstimulada(new Machine(ip, true));

            } else {
                try {
                    InetAddress h = InetAddress.getByName(ip);
                    if (h.isReachable(timeout)) {
                        LOGI("IP OK isReachable 1 " + ip);
                        ipEstimulada(new Machine(ip, true));
                    } else {
                        LOGI("IP KO isReachable 1 " + ip);
                        ipEstimulada(new Machine(ip, false));
                    }
                } catch (Exception e) {
                    LOGE(e.getMessage());
                }
            }

        } catch (Exception e) {
            LOGE("NO SE PUEDE HACER PING NATIVO");
            try {
                InetAddress h = InetAddress.getByName(ip);
                if (h.isReachable(timeout)) {
                    LOGI("IP OK isReachable 2 " + ip);
                    ipEstimulada(new Machine(ip, true));
                } else {
                    LOGI("IP KO isReachable 2 " + ip);
                    ipEstimulada(new Machine(ip, false));
                }
            } catch (UnknownHostException e2) {
                LOGE(e2.getMessage());
            } catch (IOException e2) {
                LOGE(e2.getMessage());
            }
        }
    }


    private void ipEstimulada(Machine item) {


        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/net/arp"));
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                if (!item.isConectado()) {
                    String[] arpLine = line.split("\\s+");
                    final String ip = arpLine[0];
                    String flag = arpLine[2];
                    final String macAddress = arpLine[3];
                    if (!"0x0".equals(flag) && !"00:00:00:00:00:00".equals(macAddress) && ip.equals(item.getIp())) {
                        item.setConectado(true);
                    }
                }

                if (item.isConectado()) {
                    String[] splitted = line.split(" +");
                    if (splitted != null && splitted.length >= 4 && item.getIp().equals(splitted[0])) {
                        String mac = splitted[3];
                        if (mac.matches("..:..:..:..:..:..")) {
                            item.setMac(mac);
                            break;
                        } else {
                            item.setMac(ac.getString(R.string.desconocido));
                        }
                    }
                }
            }
            reader.close();

        } catch (IOException ignored) {

        }

        //ip activa o inactiva
        if (item.isConectado()) {
            ipActiva(item);
        } else {
            ipInActiva();
        }
    }


    //ip inactiva
    private void ipInActiva() {
        //llamamos al interface inactiva
        scanResult.onInActiveIp();
    }

    private void ipActiva(Machine item) {

        Boolean isGateway = false;
        Boolean isInBBDD = false;
        Boolean isMyDevice = false;

        item.setMacPadre(macPadre); // padre

        if (item.getIp().equals(gateway)) {
            isGateway = true;
            item.setTipoImg(Constantes.TIPE_GATEWAY);
        } else if (item.getIp().equals(loacalIp)) {
            isMyDevice = true;
            item.setTipoImg(Constantes.TIPE_DEVICE);
            item.setMac(macMyDevice); // propia o hija
        } else {
            item.setTipoImg(Constantes.TIPE_OTHERS);
        }

        item.setNombre(consultas.insertaDeviceGetNombre(item.getMac()));

        for (InspectorTable itemTable : arrayInspectorTable) {
            if (itemTable.getMacdevice().equals(item.getMac())) {
                isInBBDD = true;
                item.setConocido(itemTable.getFavorito());
                break;
            }
        }
        if (!isInBBDD) {
            InspectorTable itemIns = new InspectorTable(item.getMac(), macPadre, item.getNombre(), (isGateway || isMyDevice) ? true : false);
            consultas.setItemInspectorTable(itemIns);
            arrayInspectorTable.add(itemIns);
            item.setConocido((isGateway || isMyDevice) ? true : false);
        }

        // agregamos el nombre del hardware

        NbtAddress[] nbts;
        try {
            Config.setProperty("jcifs.smb.client.soTimeout", "100");
            Config.setProperty("jcifs.smb.client.responseTimeout", "100");
            Config.setProperty("jcifs.netbios.soTimeout", "100");
            Config.setProperty("jcifs.netbios.retryTimeout", "100");

            nbts = NbtAddress.getAllByAddress(item.getIp());
            String netbiosname = nbts[0].getHostName();
            if (item.getNombre().equals("-")) {
                item.setNombre(netbiosname);
            } else {
                item.setNombre(item.getNombre() + Html.fromHtml("<br>") + netbiosname);
            }

        } catch (UnknownHostException e) {

        }


        item.setNombreSoft(consultas.getNameFromMac(item.getMac()));
        scanResult.onActiveIp(item);
    }

    private class SingleRunnable implements Runnable {
        private String ip;

        SingleRunnable(String ip, ScanResult scanResult) {
            this.ip = ip;
        }

        @Override
        public void run() {
            scanSingleIp(ip, DEFAULT_TIME_OUT);
        }
    }
}


