package com.r.raul.tools;

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
	private int dBm;

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

	public int getdBm() {
		return dBm;
	}

	public void setdBm(int dBm) {
		this.txtSeñal =  dBm + " dBm";
		this.dBm = dBm;
	}
	
	
	public void iconoDataMovil(int level) {

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

	public void iconoDataWifi(int level) {

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
