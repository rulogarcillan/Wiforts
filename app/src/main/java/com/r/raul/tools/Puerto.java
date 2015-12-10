package es.caser.campanaretencion.base.campana.test;

public class Puerto{

	private static final int ABIERTO = 1;
	private static final int CERRADO = 2;
	private static final int NOTRATADO = 0;

	private int puertoNumero;
	private int estado;

	public Puerto(int puertoNumero) {	
		this.puertoNumero = puertoNumero;
		this.puertoNumero = NOTRATADO;		
	}

	public int getPuertoNumero() {
		return puertoNumero;
	}

	public void setPuertoNumero(int puertoNumero) {
		this.puertoNumero = puertoNumero;
	}

	public int getEstado() {
		return estado;
	}

	public void setEstado(int estado) {
		this.estado = estado;
	}

}
