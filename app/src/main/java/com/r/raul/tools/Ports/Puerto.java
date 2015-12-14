package com.r.raul.tools.Ports;

public class Puerto {

    private int puerto;
    private int isOpen;

    public Puerto(int puerto, int isOpen) {
        super();
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
}


