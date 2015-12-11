package com.r.raul.tools;

public static class Puerto {
	
    private int puerto;
    private boolean isOpen;

    public Puerto(int puerto, boolean isOpen) {
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

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

}
