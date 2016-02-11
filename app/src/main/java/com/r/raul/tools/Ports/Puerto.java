package com.r.raul.tools.Ports;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static com.r.raul.tools.Utils.LogUtils.LOGI;

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

           int result=-1;
           try {
                Socket socket = new Socket();
                socket.setPerformancePreferences(1, 0, 0);
                socket.setTcpNoDelay(true);
                socket.connect(new InetSocketAddress(ip, port), 250);
                socket.close();
            } catch (IOException ignored) {
            } finally {
               LOGI(result + " " + ip);
               return result != -1;
            }
	}
	
	public static int isOpenCloseTimeOut(final String ip, final int puertoTratar, final int timeOut) throws Exception {
		try {
                    Socket socket = new Socket();
                    socket.setPerformancePreferences(1, 0, 0);
                    socket.setTcpNoDelay(true);
                    if (timeOut == 0) {
                        socket.connect(new InetSocketAddress(ip, puertoTratar));
                    } else {
                        socket.connect(new InetSocketAddress(ip, puertoTratar), timeOut);
                    }
                    socket.close();
                    return  0;
                } catch (SocketTimeoutException exTime) {
                    return 2;
                } catch (Exception ex) {
                    return 1;
                }
	}
}



