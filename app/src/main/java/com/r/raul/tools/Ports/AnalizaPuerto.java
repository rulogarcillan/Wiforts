package com.r.raul.tools.Ports;

import com.r.raul.tools.Utils.LogUtils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

public static Future<Puerto> portIsOpen(final ExecutorService es, final String ip, final int puertoTratar, final int timeOut) {


return es.submit(new Callable<Puerto>() {
	        @Override
			public Puerto call() {
				//tratamiento
				try {
					Socket socket = new Socket();
					if (timeOut==0){
						socket.connect(new InetSocketAddress(ip, puertoTratar));
					}else{
						socket.connect(new InetSocketAddress(ip, puertoTratar), timeOut);
					}
					socket.close();
					LogUtils.LOGE("Puerto: " +puertoTratar + " Abierto");
					return new Puerto(puertoTratar, 0);
				} catch (SocketTimeoutException exTime) {
					LogUtils.LOGE("Puerto: " + puertoTratar + " TimeOut");
					return new Puerto(puertoTratar, 2);
				} catch (Exception ex) {
					LogUtils.LOGE("Puerto: " +puertoTratar + " Cerrado");
					return new Puerto(puertoTratar, 1);
				}

	        }
	    });	
		
		
    }
}
