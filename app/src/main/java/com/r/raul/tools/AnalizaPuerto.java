package com.r.raul.tools;

import com.r.raul.tools.Puerto;

import java.util.concurrent.Callable;

public class AnalizaPuerto implements Callable<Puerto> {
	
	private String ip;
	private int puertoTratar;
	private int timeOut;
	
	public AnalizaPuerto(String ip, int puertoTratar, int timeOut){
		this.ip = ip;
		this.puertoTratar = puertoTratar;
		this.timeOut = timeOut;
	}


	@Override
	public Puerto call() {
	   //tratamiento
	   try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, puertoTratar), timeOut);
                socket.close();
                return new Puerto(puertoTratar, true);
            } catch (Exception ex) {
                return new Puerto(puertoTratar, false);
            }
	}
	
}
