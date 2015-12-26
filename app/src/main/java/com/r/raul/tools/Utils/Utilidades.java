package com.r.raul.tools.Utils;

import android.content.Context;
import android.os.Vibrator;

import com.r.raul.tools.Inspector.Machine;
import com.r.raul.tools.Ports.Puerto;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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
                    LogUtils.LOGE("Puerto: " + puertoTratar + " Abierto");
                    return new Puerto(puertoTratar, 0);
                } catch (SocketTimeoutException exTime) {
                    LogUtils.LOGE("Puerto: " + puertoTratar + " TimeOut");
                    return new Puerto(puertoTratar, 2);
                } catch (Exception ex) {
                    LogUtils.LOGE("Puerto: " + puertoTratar + " Cerrado");
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

            if (ip.isReachable(time)) {
                retorno.setConectado(true);

            } else {
                retorno.setIp(ip.getHostName());

                int[] puertos = {135, 139, 22, 11, 80};

                for (int num : puertos) {
                    try {

                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(ip.getHostAddress(), num), 200);
                        socket.close();
                        retorno.setConectado(true);

                        break;
                    } catch (IOException e1) {

                        retorno.setConectado(false);
                    }

                }

            }

            return retorno;
        }

        public static int[] rangeFromCidr(String cidrIp) {
            int maskStub = 1 << 31;
            String[] atoms = cidrIp.split("/");
            int mask = Integer.parseInt(atoms[1]);
            System.out.println(mask);

            int[] result = new int[2];
            result[0] = InetRange.ipToInt(atoms[0]) & (maskStub >> (mask - 1)); // lower bound
            result[1] = InetRange.ipToInt(atoms[0]); // upper bound
            System.out.println(InetRange.intToIp(result[0]));
            System.out.println(InetRange.intToIp(result[1]));

            return result;
        }

        public static class InetRange {
            public static int ipToInt(String ipAddress) {
                try {
                    byte[] bytes = InetAddress.getByName(ipAddress).getAddress();
                    int octet1 = (bytes[0] & 0xFF) << 24;
                    int octet2 = (bytes[1] & 0xFF) << 16;
                    int octet3 = (bytes[2] & 0xFF) << 8;
                    int octet4 = bytes[3] & 0xFF;
                    int address = octet1 | octet2 | octet3 | octet4;

                    return address;
                } catch (Exception e) {
                    e.printStackTrace();

                    return 0;
                }
            }

            public static String intToIp(int ipAddress) {
                int octet1 = (ipAddress & 0xFF000000) >>> 24;
                int octet2 = (ipAddress & 0xFF0000) >>> 16;
                int octet3 = (ipAddress & 0xFF00) >>> 8;
                int octet4 = ipAddress & 0xFF;

                return new StringBuffer().append(octet1).append('.').append(octet2)
                        .append('.').append(octet3).append('.')
                        .append(octet4).toString();
            }
        }
    }
}
