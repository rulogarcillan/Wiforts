package com.r.raul.tools.Ports;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Puerto {
    
    private int puerto;
    private int isOpen;
    private int value = 0;
    private boolean isActive;

    public Puerto(int puerto, int isOpen) {
        super();
        setValue(puerto);		
        this.puerto = puerto;
        this.isOpen = isOpen;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public int getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(int isOpen) {
        this.isOpen = isOpen;
    }
    
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void setIsOpen(boolean isActive) {
		this.isActive = isActive;
	}

	public static boolean isReachable(String ip, int port) throws Exception {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), 1000);
			socket.setSoTimeout(1000);

			int result;
			try {
				InputStream inputStream = socket.getInputStream();
				result = inputStream.read();
				socket.close();

			} catch (Exception e) {

				return true;
			}

			return result != -1;
		} catch (IOException e) {

			return false;
		}
	}
}



