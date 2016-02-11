package com.r.raul.tools.Inspector;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.r.raul.tools.Utils.LogUtils.LOGE;
import static com.r.raul.tools.Utils.LogUtils.LOGI;

public class IpScan {
    private ScanResult scanResult;
    public static final int DEFAULT_TIME_OUT = 500;
    public static final int DEFAULT_FIXED_POOL = 100;
    public ExecutorService pool;


    public IpScan(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    public void scanAll(String[] scanRange) {

        pool = Executors.newFixedThreadPool(DEFAULT_FIXED_POOL);
        for (String ip : scanRange) {
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
        pool.shutdownNow();
    }

    private void launch(String i) {
        if (!pool.isShutdown()) {
            pool.execute(new SingleRunnable(i, scanResult));
        }
    }

    public void scanSingleIp(String ip, int timeout) {
        try {
            final String CMD = "/system/bin/ping -q -n -w 1 -c 1 %s";
            Process myProcess = Runtime.getRuntime().exec(String.format(CMD, ip));
            myProcess.waitFor();
            if (myProcess.exitValue() == 0) {
                LOGI("IP OK Ping " + ip);
                scanResult.onActiveIp(ip);
            } else {
                try {
                    InetAddress h = InetAddress.getByName(ip);
                    if (h.isReachable(timeout)) {
                        LOGI("IP OK isReachable 1 " + ip);
                        scanResult.onActiveIp(ip);
                    } else {
                        LOGI("IP KO isReachable 1 " + ip);
                        if (ipEestimulada(ip)) {
                            scanResult.onActiveIp(ip);
                        } else {
                            scanResult.onInActiveIp(ip);
                        }
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
                    scanResult.onActiveIp(ip);
                } else {
                    LOGI("IP KO isReachable 2 " + ip);
                    if (ipEestimulada(ip)) {
                        scanResult.onActiveIp(ip);
                    } else {
                        scanResult.onInActiveIp(ip);
                    }
                }
            } catch (UnknownHostException e2) {
                LOGE(e2.getMessage());
            } catch (IOException e2) {
                LOGE(e2.getMessage());
            }
        }
    }


    private Boolean ipEestimulada(String ipEntrada) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/net/arp"));
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] arpLine = line.split("\\s+");

                final String ip = arpLine[0];
                String flag = arpLine[2];
                final String macAddress = arpLine[3];

                if (!"0x0".equals(flag) && !"00:00:00:00:00:00".equals(macAddress) && ip.equals(ipEntrada)) {
                    return true;
                }
            }
            reader.close();

        } catch (IOException ignored) {

        }
        return false;
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


