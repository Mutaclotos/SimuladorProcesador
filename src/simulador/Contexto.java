package simulador;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Contexto 
{
	private int[] registros;
	public int etiqueta;
	public float tiempoEjecucion;
	public int numeroCiclos;
	public int pc;
	
	//Constructor
	public Contexto(int etiqueta)
	{
		this.etiqueta = etiqueta;
		registros = new int[32];
		tiempoEjecucion = 0;
		numeroCiclos = 0;
		pc = 0;
		
		for(int i = 0; i < registros.length; i++)
    	{
			this.registros[i] = 0;
    	}
	}
	
	//Copia las registros del archivo de texto al hilillo
	public void cargarRegistros(int[] registros)
	{
		for(int i = 0; i < registros.length; i++)
    	{
			this.registros[i] = registros[i];
    	}
	}
	
	//Retorna una instruccion de 4 enteros del hilillo
	public int[] getRegistros()
	{
		return registros;
	}
	
	//Reemplaza los valores de registros del contexto con los valores del arreglo de entrada
	public void setRegistros(int[] registros)
	{
		System.arraycopy(registros, 0, this.registros, 0, registros.length);
	}
	
	public int getEtiqueta()
	{
		return etiqueta;
	}
	
	public int getCiclos()
	{
		return numeroCiclos;
	}
	
	public void setCiclos(int ciclos)
	{
		this.numeroCiclos = numeroCiclos + ciclos;
	}
	
	public int getPc()
	{
		return pc;
	}
	
	public void setPc(int pc)
	{
		this.pc = pc;
	}
	
	public float getTiempoEjecucion()
	{
		return tiempoEjecucion;
	}
	
	public void setTiempoEjecucion(float tiempo)
	{
		this.tiempoEjecucion = tiempoEjecucion + tiempo;
	}
	
}
