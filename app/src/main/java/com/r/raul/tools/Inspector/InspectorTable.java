public class InspectorTable {
	
	String macdevice;  
	String macpadre; 
	String nombre;
	int favorito;
	
	public InspectorTable() {
		super();
		// TODO Auto-generated constructor stub
	}

	public InspectorTable(String macdevice, String macpadre, String nombre, Boolean favorito) {
		super();
		this.macdevice = macdevice;
		this.macpadre = macpadre;
		this.nombre = nombre;
		
		if (favorito){
			this.favorito = 1;
		}else {
			this.favorito = 0;	
		}	
	}



	public String getMacdevice() {
		return macdevice;
	}

	public void setMacdevice(String macdevice) {
		this.macdevice = macdevice;
	}

	public String getMacpadre() {
		return macpadre;
	}

	public void setMacpadre(String macpadre) {
		this.macpadre = macpadre;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Boolean getFavorito() {
		
		if (favorito == 0){
			return false;
		}else if(favorito == 1){
			return true;			
		}
		return false;
		
	}

	public void setFavorito(Boolean favorito) {
		
		if (favorito){
			this.favorito = 1;
		}else {
			this.favorito = 0;	
		}		
	} 
}
