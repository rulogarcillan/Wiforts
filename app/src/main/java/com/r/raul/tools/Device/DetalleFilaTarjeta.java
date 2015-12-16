
package com.r.raul.tools.Device;

public class DetalleFilaTarjeta {

	private String titulo;
	private String contenido;

	public DetalleFilaTarjeta(String titulo, String contenido) {
		super();
		this.titulo = titulo;
		this.contenido = contenido;
	}

	public DetalleFilaTarjeta() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

}
