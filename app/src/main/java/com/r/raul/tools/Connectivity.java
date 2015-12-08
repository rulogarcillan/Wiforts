package com.r.raul.tools;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;


public class Connectivity {


    public static NetworkInfo getNetworkInfo(Context context) {

        ConnectivityManager cm = null;
        if (context != null) {
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        } else {
            LogUtils.LOGE("Ativity nula");
            return null;
        }
        return cm.getActiveNetworkInfo();
    }


    public static boolean isConnected(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }


    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }


    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static String parseIP(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return String.valueOf(InetAddress.getByAddress(addressBytes)).replace("/", "");
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    public static String getType(int type, int subType, Activity a) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return "WIFI";
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return "2G | 1xRTT"; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return "2G | CDMA";  // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return "2G | EDGE";  // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return "3G | EVDO rev. 0";  // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return "3G | EVDO rev. A"; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return "2G | GPRS"; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return "3G | HSDPA"; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return "3G | HSPA";  // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return "3G | HSUPA";  // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return "3G | UMTS";   // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion
			 * to appropriate level to use these
			 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return "3G | EHRPD";  // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return "3G | EVDO_B"; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return "3G | HSPAP"; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return "2G | iDen"; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return "4G | LTE"; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return a.getString(R.string.desconocido);
            }
        } else {
            return a.getString(R.string.nodisponible);
        }
    }

    public static String getPublicIp() throws IOException {
        return getPublicIp(true);
    }

    public static String getPublicIp(boolean useHttps) throws IOException {
        URL ipify = useHttps ? new URL("https://api.ipify.org") : new URL("http://api.ipify.org");
        URLConnection conn = ipify.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String ip = null;
        ip = in.readLine();
        in.close();
        return ip;
    }

    public static InetAddress getLocalAddress() {
        try {
            Enumeration<NetworkInterface> b = NetworkInterface
                    .getNetworkInterfaces();
            while (b.hasMoreElements()) {
                for (InterfaceAddress f : b.nextElement()
                        .getInterfaceAddresses())
                    if (f.getAddress().isSiteLocalAddress())
                        return f.getAddress();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static ArrayList<String> readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        ArrayList<String> listdata = new ArrayList<String>();

        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);

            JSONObject json = new JSONObject(jsonText);

            listdata.add(0, json.getString("isp"));
            listdata.add(1, json.getString("country"));
            listdata.add(2, json.getString("countryCode"));
            listdata.add(3, json.getString("city"));
            listdata.add(4, json.getString("region"));
            listdata.add(5, json.getString("regionName"));
            listdata.add(6, json.getString("zip"));
            listdata.add(7, json.getString("lat"));
            listdata.add(8, json.getString("lon"));

            LogUtils.LOG(String.valueOf(listdata.size()));

            return listdata;
        } finally {
          /*  listdata.add(0,"");
            listdata.add(1,"");
            listdata.add(2,"");
            listdata.add(3,"");
            listdata.add(4,"");
            listdata.add(5,"");
            listdata.add(6,"");
            listdata.add(7,"");
            listdata.add(8,"");*/

            LogUtils.LOG(String.valueOf(listdata.size()));
            is.close();
        }
    }

    public static String obtenerHostName(String ip) {

        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return addr.getHostName();
    }
}
