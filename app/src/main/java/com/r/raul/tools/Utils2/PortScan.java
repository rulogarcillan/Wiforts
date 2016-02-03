
package com.r.raul.tools.Utils2;

import java.util.ArrayList;

public class PortScan {
  
    private ScanResult scanResult;

    public PortScan(ScanResult scanResult) {
        this.scanResult = scanResult;
    }
    
    public PortScan() {
    }
    
    public void start(String ip) throws Exception {
        
        ArrayList<Port> standardPorts = getStandardPortList(); //puertos a tratar
        boolean activo = false;
        for (Port port : standardPorts) {
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

    private ArrayList<Port> getStandardPortList() {
        ArrayList<Port> portList = new ArrayList<>();
        portList.add(new Port(Port.TYPE_HTTP, 80));
        portList.add(new Port(Port.TYPE_HTTP, 135));
        portList.add(new Port(Port.TYPE_HTTP, 139));
        portList.add(new Port(Port.TYPE_HTTP, 22));
        portList.add(new Port(Port.TYPE_HTTP, 11));
        return portList;
    }

    private Boolean scanPort(String ip, Port port) throws Exception {
        if (Port.isReachable(ip, port.getValue())) {
            return true;
        }
        return false;
    }
}
