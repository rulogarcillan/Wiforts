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


package es.caser.campanaretencion.base.campana.test;

import java.util.ArrayList;

public class ordena {

	public ordena() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
/*
	public boolean parsea(String lista){
	
		ArrayList<Integer> listaPuertos = new ArrayList<Integer>();
		
		if (lista.contains(",")){	
			String[] partsComa = lista.split(",");
			for (String portsComa : partsComa){
				if (portsComa.contains("-")){
					String[] partsGuion = portsComa.split("-");
					if (partsGuion.length == 2){
						for (int i = Integer.parseInt(partsGuion[0]); i = Integer.parseInt(partsGuion[1]); i++){
							listaPuertos.add(i);
						}
					
					}else{
						return error;
					}			
					
				}else{		
					listaPuertos.add(portsComa);			
				}	
			}
		}else if (string.contains("-")){
					String[] partsGuion = string.split("-");
					If (partsGuion.count = 2){
						for (int i = partsGuion[0]; i = partsGuion[0]; i++){
							listaPuertos.add(i);
						}			
					}else{
						return error;
					}			
					
				}
		}else{
		listaPuertos.add(string);
		}
		
	}


	
}	*/
