package com.r.raul.tools.Utils;

import android.content.Context;
import android.os.Vibrator;

import com.r.raul.tools.Inspector.Machine;
import com.r.raul.tools.Ports.Puerto;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Utilidades {

    public static void lanzaVibracion(Context mContext, int time) {
        Vibrator v = (Vibrator) mContext
                .getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(time);
    }

    public static Future<Puerto> portIsOpen(final ExecutorService es, final String ip, final int puertoTratar, final int timeOut) {

        return es.submit(new Callable<Puerto>() {
            @Override
            public Puerto call() {
                // tratamiento
                try {
                    Socket socket = new Socket();
                    if (timeOut == 0) {
                        socket.connect(new InetSocketAddress(ip, puertoTratar));
                    } else {
                        socket.connect(new InetSocketAddress(ip, puertoTratar), timeOut);
                    }
                    socket.close();
                    LogUtils.LOGI("Puerto: " + puertoTratar + " Abierto");
                    return new Puerto(puertoTratar, 0);
                } catch (SocketTimeoutException exTime) {
                    LogUtils.LOGI("Puerto: " + puertoTratar + " TimeOut");
                    return new Puerto(puertoTratar, 2);
                } catch (Exception ex) {
                    LogUtils.LOGI("Puerto: " + puertoTratar + " Cerrado");
                    return new Puerto(puertoTratar, 1);
                }

            }
        });

    }

    public static Future<Machine> machineExist(final ExecutorService es, final InetAddress ip, final int time) {

        return es.submit(new Callable<Machine>() {
            @Override
            public Machine call() {

                Machine retorno = new Machine();

                try {
                    retorno = ScanNet.escaneaIps(ip, time);
                } catch (Exception e) {
                    retorno.setIp(ip.getHostName());
                    retorno.setConectado(false);
                    e.printStackTrace();
                }


                return retorno;
            }
        });

    }


    public static class ScanNet {

        public static Machine escaneaIps(InetAddress ip, int time) throws Exception {

            Machine retorno = new Machine();
            retorno.setIp(ip.getHostName());
            final ExecutorService es = Executors.newFixedThreadPool(5);
            final List<Future<Puerto>> futures = new ArrayList<Future<Puerto>>();

            if (ip.isReachable(time)) {
                retorno.setConectado(true);

            } else {

                int[] puertos = {135, 139, 22, 11, 80};


                for (int num : puertos) {

                    futures.add(Utilidades.portIsOpen(es, ip.getHostAddress(), num, 1000));
                    es.awaitTermination(1000L, TimeUnit.MILLISECONDS);

                    for (final Future<Puerto> f : futures) {
                        try {
                            if (f.get().getIsOpen() == 0) {
                                retorno.setConectado(true);
                                break;
                            } else if (f.get().getIsOpen() == 1) {
                                retorno.setConectado(false);
                            } else if (f.get().getIsOpen() == 2) {
                                retorno.setConectado(false);
                            }
                        } catch (InterruptedException e) {
                            LogUtils.LOGE(e.getMessage());

                        } catch (ExecutionException e) {
                            LogUtils.LOGE(e.getMessage());

                        }

                    }
                    //futures.clear();
                }
            }

            es.shutdownNow();
           // LogUtils.LOGE("FIIIIIIIIIIIIIIIIIIIN");
            return retorno;
        }
    }


}
