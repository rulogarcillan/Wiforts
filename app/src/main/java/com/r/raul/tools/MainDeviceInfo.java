package com.r.raul.tools;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v7.widget.Toolbar;

import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Rulo on 15/11/2015.
 */
public class MainDeviceInfo extends Fragment {

    //vistas
    private TextView txtNombreRed, txtTipoRed, txtModelo, txtVersion, txtIpPublic, txtIpLocal, txtSeñal, txtGateway, txtMasSubred, txtDns1, txtDns2;
    private FloatingActionButton fab;

    //servicios
    private Connectivity con; //esta es mi clase
    private TelephonyManager tlfMan;
    private NetworkInfo info;


    //
    private MyPhoneStateListener MyListener;
	private DataDeviceInfo dataDeviceInfo;


    public static MainDeviceInfo newInstance() {
	
        MainDeviceInfo fragment = new MainDeviceInfo();		
        return fragment;
    }

    public MainDeviceInfo() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        View rootView = inflater.inflate(R.layout.info_device, container, false);

        txtNombreRed = (TextView) rootView.findViewById(R.id.txtNombreRed);
        txtTipoRed = (TextView) rootView.findViewById(R.id.txtRed);
        txtModelo = (TextView) rootView.findViewById(R.id.txtModelo);
        txtVersion = (TextView) rootView.findViewById(R.id.txtVersion);
        txtIpPublic = (TextView) rootView.findViewById(R.id.txtIpPublic);
        txtIpLocal = (TextView) rootView.findViewById(R.id.txtIpLocal);
        txtSeñal = (TextView) rootView.findViewById(R.id.txtSeñal);

        txtDns1 = (TextView) rootView.findViewById(R.id.txtDns1);
        txtDns2 = (TextView) rootView.findViewById(R.id.txtDns2);
        txtMasSubred = (TextView) rootView.findViewById(R.id.txtMasSubred);
        txtGateway = (TextView) rootView.findViewById(R.id.txtGateway);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        //datos de telefonía.
        tlfMan = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        MyListener = new MyPhoneStateListener();
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_SERVICE_STATE);
		
		 fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (con.isConnectedWifi(getContext())) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                } else if (con.isConnectedMobile(getContext())) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.android.settings.Settings$DataUsageSummaryActivity"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName("com.android.settings",
                            "com.android.settings.Settings$DataUsageSummaryActivity"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });

        printData();

        return rootView;
    }  


    @Override
    public void onResume() {
        super.onResume();
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    public void onPause() {
        super.onPause();
        tlfMan.listen(MyListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void printData() {
	
		dataDeviceInfo = new dataDeviceInfo(getActivity()){		
		
			@Override
			protected void onPostExecute(Boolean result) {
			
				fab.setImageResource(dataDeviceInfo.getTipoIcono());
				txtNombreRed.setText(dataDeviceInfo.getTxtNombreRed());
				txtTipoRed.setText(dataDeviceInfo.getTxtTipoRed());
				txtSeñal.setText(dataDeviceInfo.getTxtSeñal());				
				txtIpLocal.setText(dataDeviceInfo.getTxtIpLocal());
				txtIpPublic.setText(dataDeviceInfo.getTxtIpPublic());
				txtDns1.setText(dataDeviceInfo.getTTxtDns1());
				txtDns2.setText(dataDeviceInfo.getTxtDns2());
				txtMasSubred.setText(dataDeviceInfo.getTxtMasSubred());
				txtGateway.setText(dataDeviceInfo.getTxtGateway());
				txtVersion.setText(dataDeviceInfo.getTxtVersion);
				txtModelo.setText(dataDeviceInfo.getTxtModelo());			
			}		
		};
		
		dataDeviceInfo.execute();       
    }
	
	 private class MyPhoneStateListener extends PhoneStateListener {     

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

				printData();
        }      
    }

}
