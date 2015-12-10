public class AnalizaPuerto implements Callable<Puerto> {

	private int puertoTratar;
	
	public AnalizaPuerto(int puertoTratar){
		this.puertoTratar = puertoTratar;
	}


	@Override
	public Puerto call() throws Exception {
		Puerto puerto = new Puerto(puertoTratar);
		
		//tratamiento
		
		return puerto;
	}
	
}
