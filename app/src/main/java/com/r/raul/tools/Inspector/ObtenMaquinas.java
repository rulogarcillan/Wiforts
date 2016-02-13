package com.r.raul.tools.Inspector;

import android.app.Activity;
import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ObtenMaquinas extends AsyncTask<Void, Integer, Void> {

    private Activity ac;
    private ArrayList<Machine> array;

    private IpScan ipScan = null;
    private int tot = 0; // total ips analizadas para barra de progreso
    private int totalMachine;

    public ObtenMaquinas(Activity ac, ArrayList<Machine> array) {
        this.ac = ac;
        this.array = array;
    }

    private int calculoPercent(int valor, float tot) {
        return (int) (valor * 100 / tot);
    }


    @Override
    protected Void doInBackground(Void... params) {

        ipScan = new IpScan(new ScanResult() {

            @Override
            public void onActiveIp(Machine item) {
                // TODO Auto-generated method stub
                synchronized (array){
                    array.add(item);
                    ordenarIps(array);
                    publishProgress(calculoPercent(tot++, totalMachine));
                }
            }

            @Override
            public void onInActiveIp() {
                publishProgress(calculoPercent(tot++, totalMachine));
            }

        }, ac);

        totalMachine = ipScan.totalIpsAnalizar();
        ipScan.scanAll();

        return null;
    }


    private void ordenarIps(ArrayList ips) {
        Collections.sort(ips, new Comparator<Machine>() {

            @Override
            public int compare(Machine o1, Machine o2) {

                InetAddress adr1 = null;
                try {
                    adr1 = InetAddress.getByName(o1.getIp());
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                InetAddress adr2 = null;
                try {
                    adr2 = InetAddress.getByName(o2.getIp());
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                byte[] ba1 = adr1.getAddress();
                byte[] ba2 = adr2.getAddress();

                if (ba1.length < ba2.length)
                    return -1;
                if (ba1.length > ba2.length)
                    return 1;

                for (int i = 0; i < ba1.length; i++) {
                    int b1 = unsignedByteToInt(ba1[i]);
                    int b2 = unsignedByteToInt(ba2[i]);
                    if (b1 == b2)
                        continue;
                    if (b1 < b2)
                        return -1;
                    else
                        return 1;
                }
                return 0;
            }

            private int unsignedByteToInt(byte b) {
                return (int) b & 0xFF;
            }
        });
    }

    @Override
    protected void onCancelled() {
        ipScan.stop();
        super.onCancelled();
    }

    @Override
    protected void onCancelled(Void aVoid) {
        ipScan.stop();
        super.onCancelled(aVoid);
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
