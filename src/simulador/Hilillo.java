package simulador;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Hilillo 
{
	private List<int[]> instrucciones = new ArrayList<int[]>();
	public int etiqueta;
	public int cantidadInstrucciones;
	
	public Hilillo(int etiqueta)
	{
		this.etiqueta = etiqueta;
		this.cantidadInstrucciones = 0;
	}
	
	//Copia las instrucciones del archivo de texto al hilillo
	public void cargarInstrucciones(int[] filaInstruccion)
	{
		instrucciones.add(filaInstruccion);
	}
	
	//Retorna una instruccion de 4 enteros del hilillo
	public int[] getInstruccion(int index)
	{
		return instrucciones.get(index);
	}
	
	public void setCantidadInst(int cantidad)
	{
		this.cantidadInstrucciones = cantidad;
	}
	
	public int getCantidadInst()
	{
		return cantidadInstrucciones;
	}
	
	public int getEtiqueta()
	{
		return etiqueta;
	}
}
