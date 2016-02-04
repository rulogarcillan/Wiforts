
package com.r.raul.tools.Utils2;

import com.r.raul.tools.Ports.Puerto;

import java.util.ArrayList;

public class PortScan {
  
    private ScanResult scanResult;

    public PortScan(ScanResult scanResult) {
        this.scanResult = scanResult;
    }
    
    public PortScan() {
    }
    
    public void start(String ip) throws Exception {
        
        ArrayList<Puerto> standardPorts = getStandardPortList(); //puertos a tratar
        boolean activo = false;
        for (Puerto port : standardPorts) {
            if (scanPort(ip, port)){
                activo = true;
                if (scanResult != null) {
                    scanResult.onActiveIp(ip);
                }
                break;
            }
        }
        if (!activo){
            if (scanResult != null) {
                scanResult.onInActiveIp(ip);
            }
        }
    }

    private ArrayList<Puerto> getStandardPortList() {
        ArrayList<Puerto> portList = new ArrayList<Puerto>();
        portList.add(new Puerto(80,0));
        portList.add(new Puerto(22,0));
        portList.add(new Puerto(443,0));
        portList.add(new Puerto(25,0));
        portList.add(new Puerto(135,0));
        return portList;
    } 

    private Boolean scanPort(String ip, Puerto port) throws Exception {
        if (Puerto.isReachable(ip, port.getValue())) {
            return true;
        }
        return false;
    }
}
