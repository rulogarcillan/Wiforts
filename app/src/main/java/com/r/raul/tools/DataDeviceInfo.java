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

public class DataDeviceInfo {

	private String txtNombreRed;
	private String txtTipoRed;
	private String txtModelo;
	private String txtVersion;
	private String txtIpPublic;
	private String txtIpLocal;
	private String txtSeñal;
	private String txtGateway;
	private String txtMasSubred;
	private String txtDns1;
	private String txtDns2;
	private int tipoIcono;

	/* Constructor */
	public DataDeviceInfo() {

	}

	/* Gets y sets */
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

	private void iconoDataMovil(int level) {

		switch (level) {
		case -1:
			tipoIcono = R.drawable.ic_sigmobile0;
			break;
		case 0:
			tipoIcono = R.drawable.ic_sigmobile1;
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

		}
	}

	private void iconoDataWifi(int level) {

		switch (level) {
		case 0:
			tipoIcono = R.drawable.ic_wifi1;
			break;
		case 1:
			tipoIcono = R.drawable.ic_wifi2;
			break;
		case 2:
			tipoIcono = R.drawable.ic_wifi3;
			break;
		case 3:
			tipoIcono = R.drawable.ic_wifi4;
			break;
		case 4:
			tipoIcono = R.drawable.ic_wifi5;
			break;
		}
	}

}
