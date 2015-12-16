

package com.r.raul.tools.Device;

import java.util.ArrayList;

public class DetalleTarjeta {

	private ArrayList<DetalleFilaTarjeta> infoIp;
	private ArrayList<DetalleFilaTarjeta> infoRed;

	public DetalleTarjeta() {
		super();
		infoIp = new ArrayList<DetalleFilaTarjeta>;
		infoRed = new ArrayList<DetalleFilaTarjeta>;
	}
	
	public void resetArrays(){
		  infoIp.clear();
		  infoRed.clear();
	}
	
	public ArrayList<DetalleFilaTarjeta> getInfoIp() {
		return infoIp;
	}
	
	public void setInfoIp(ArrayList<DetalleFilaTarjeta> infoIp) {
		this.infoIp = infoIp;
	}
	
	public ArrayList<DetalleFilaTarjeta> getInfoRed() {
		return infoRed;
	}
	
	public void setInfoRed(ArrayList<DetalleFilaTarjeta> infoRed) {
		this.infoRed = infoRed;
	}
	
	
	

	

}
