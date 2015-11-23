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

public class DataDeviceInfo extends AsyncTask<Void, Void, Boolean> {

	Private String txtNombreRed, txtTipoRed, txtModelo, txtVersion, txtIpPublic;
	Private String txtIpLocal, txtSeñal, txtGateway, txtMasSubred, txtDns1, txtDns2;
	Private int tipoIcono;
	Private Activity activity;
	Private Connectivity con; 
	Private TelephonyManager tlfMan;
	Private NetworkInfo info;
	
	/* Constructor */
	public DataDeviceInfo(Activity activity) {
		super();
		this.activity = activity;
		tlfMan = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
		info = Connectivity.getNetworkInfo(activity);
	}		
	
	/*Gets y sets*/
	public String getTxtNombreRed() {
		return txtNombreRed;
	}

	public void setTxtNombreRed(String txtNombreRed) {
		this.txtNombreRed = txtNombreRed;
	}

	public String getTxtTipoRed() {
		return txtTipoRed;
	}

	public void setTxtTipoRed(String txtTipoRed) {
		this.txtTipoRed = txtTipoRed;
	}

	public String getTxtModelo() {
		return txtModelo;
	}

	public void setTxtModelo(String txtModelo) {
		this.txtModelo = txtModelo;
	}

	public String getTxtVersion() {
		return txtVersion;
	}

	public void setTxtVersion(String txtVersion) {
		this.txtVersion = txtVersion;
	}

	public String getTxtIpPublic() {
		return txtIpPublic;
	}

	public void setTxtIpPublic(String txtIpPublic) {
		this.txtIpPublic = txtIpPublic;
	}

	public String getTxtIpLocal() {
		return txtIpLocal;
	}

	public void setTxtIpLocal(String txtIpLocal) {
		this.txtIpLocal = txtIpLocal;
	}

	public String getTxtSeñal() {
		return txtSeñal;
	}

	public void setTxtSeñal(String txtSeñal) {
		this.txtSeñal = txtSeñal;
	}

	public String getTxtGateway() {
		return txtGateway;
	}

	public void setTxtGateway(String txtGateway) {
		this.txtGateway = txtGateway;
	}

	public String getTxtMasSubred() {
		return txtMasSubred;
	}

	public void setTxtMasSubred(String txtMasSubred) {
		this.txtMasSubred = txtMasSubred;
	}

	public String getTxtDns1() {
		return txtDns1;
	}

	public void setTxtDns1(String txtDns1) {
		this.txtDns1 = txtDns1;
	}

	public String getTxtDns2() {
		return txtDns2;
	}

	public void setTxtDns2(String txtDns2) {
		this.txtDns2 = txtDns2;
	}

	public int getTipoIcono() {
		return tipoIcono;
	}

	public void setTipoIcono(int tipoIcono) {
		this.tipoIcono = tipoIcono;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}	
	
	@Override
	protected Boolean doInBackground(Void... params) {	
	
		if (con.isConnectedWifi(activity)) {
		
			ActualizaDatosMovil();

		} else if (con.isConnectedMobile(activity)) {
			
			ActualizaDatosWifi();

		} else {
			ActualizaDatosSincon();
	    }
		
	    return true;		
	}

	@Override
	protected void onPostExecute(Boolean result) {
		
	}
	
	public void ActualizaDatosMovil() {	
	
		txtVersion= "Android " + Build.VERSION.RELEASE;
		txtModelo = Build.MODEL;
		txtTipoRed = con.getType(info.getType(), info.getSubtype(), activity);
		txtNombreRed = tlfMan.getNetworkOperatorName();		
		
		//icono y señal
		try {                 
			for (CellInfo infoRed : tlfMan.getAllCellInfo()) {
				if (infoRed instanceof CellInfoGsm && con.getType(info.getType(), info.getSubtype(), activity) != "4G | LTE") {

					CellSignalStrengthGsm gsm = ((CellInfoGsm) infoRed).getCellSignalStrength();
					iconoDataMovil(gsm.getLevel());
					txtSeñal = String.valueOf(gsm.getDbm()) + " dBm";
					// LogUtils.LOG("GSM " + txtSeñal);
				} else if (infoRed instanceof CellInfoCdma) {
					
					CellSignalStrengthCdma cdma = ((CellInfoCdma) infoRed).getCellSignalStrength();
					iconoDataMovil(cdma.getLevel());
					txtSeñal = String.valueOf(cdma.getDbm()) + " dBm";
					// LogUtils.LOG("cdma " + txtSeñal);
					
				} else if (infoRed instanceof CellInfoLte) {
					
					CellSignalStrengthLte lte = ((CellInfoLte) infoRed).getCellSignalStrength();
					txtSeñal = String.valueOf(lte.getDbm()) + " dBm";
					iconoDataMovil(lte.getLevel());
					// LogUtils.LOG("lte " + txtSeñal);
					
				} else if (infoRed instanceof CellInfoWcdma) {
					if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
					
						final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) infoRed).getCellSignalStrength();
						txtSeñal = String.valueOf(wcdma.getDbm()) + " dBm";
						iconoDataMovil(wcdma.getLevel());
						//  LogUtils.LOG("cdma " + txtSeñal);
						
					} else {
						int cobertura = signalStrength.getGsmSignalStrength() * 2 - 113;
						txtSeñal = String.valueOf(signalStrength.getGsmSignalStrength() * 2 - 113) + " dBm";
							if (cobertura <= -113){
								iconoDataMovil(-1);
							}else if (cobertura <= -111) {
								iconoDataMovil(0);
							}else if (cobertura <= -97){
								iconoDataMovil(1);
							}else if (cobertura <= -87){
								iconoDataMovil(2);								
							}else if (cobertura <= -71){
								iconoDataMovil(3);
							}else if (cobertura > -71){
								iconoDataMovil(4);
							}
						}								
					}

				} else {
					if (con.getType(info.getType(), info.getSubtype(), activity) != "4G | LTE") {
						txtSeñal = String.valueOf("Uknow");
						iconoDataMovil(-1);
					}
				}
				
		} catch (Exception e) {
			LogUtils.LOG(e.getMessage());
		}			
						
						
		txtIpPublic = R.string.nodisponible;
		txtIpLocal = getLocalAddress();
		txtGateway = R.string.nodisponible;	
		txtMasSubred = R.string.nodisponible;
		txtDns1 = R.string.nodisponible;
		txtDns2 = R.string.nodisponible;           
	}
	
	public void ActualizaDatosWifi(){
	}
	
	public void ActualizaDatosSincon(){
		
		txtVersion= "Android " + Build.VERSION.RELEASE;
		txtModelo = Build.MODEL;
		tipoIcono = R.drawable.ic_sigmobile0; //modificar
		txtNombreRed = tlfMan.getNetworkOperatorName();		
		txtTipoRed=R.string.nodisponible;
        txtSeñal=R.string.nodisponible;		
		txtIpPublic = R.string.nodisponible;
		txtIpLocal = R.string.nodisponible;
		txtGateway = R.string.nodisponible;	
		txtMasSubred = R.string.nodisponible;
		txtDns1 = R.string.nodisponible;
		txtDns2 = R.string.nodisponible; 
	}
	
	private void iconoDataMovil(int percent) {
            
			switch (percent) {
                case -1:
                    tipoIcono = R.drawable.ic_sigmobile0;
                    break;
                case 1:
                    tipoIcono = R.drawable.ic_sigmobile2;
                    break;
                case 2:
                    tipoIcono = R.drawable.ic_sigmobile3;
                    break;
                case 3:
                    tipoIcono = R.drawable.ic_sigmobile4;
                    break;
                case 4:
                    tipoIcono = R.drawable.ic_sigmobile5;
                    break;
                case 0:
                    tipoIcono = R.drawable.ic_sigmobile1;
                    break;
            }
        }	
	
	
	//Obtiene ip local
	private String getLocalAddress() {
		try {
			Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
			while (b.hasMoreElements()) {
				for (InterfaceAddress f : b.nextElement().getInterfaceAddresses())
					if (f.getAddress().isSiteLocalAddress())
						return f.getAddress().getHostAddress();
			}
		} catch (SocketException e) {
			e.printStackTrace();			
		}
		
		return activity.getString(R.string.nodisponible);
	}

}
