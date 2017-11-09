package simulador;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Write a description of class main here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Controlador
{
    // instance variables - replace the example below with your own
	public static int hilosTerminados; //Determina la cantidad de nucleos listos para terminar su terminacion
	public static int hilosListosParaTic;
	public static int reloj;
	public static int quantum; //El valor del quantum ingresado por el usuario
    Procesador procesador;
    /**
     * Constructor for objects of class main
     */
    public static void main(String[] args)
    {
      hilosTerminados = 0;
      hilosListosParaTic = 0;
      reloj = 0;
      
      
    	
      System.out.println("Inicializando procesador 0:");
      Procesador P0 = new Procesador(2, 16, 384, 64, "p0.txt");
      
      System.out.println("Inicializando procesador 1:"); 
      Procesador P1 = new Procesador(1, 16, 256, 32, "p1.txt");

      
    }

   public void avanzarReloj()
   {
	   while(hilosTerminados < 3)
	      {
	    	  if(hilosListosParaTic == 3)
	    	  {
	    		  reloj++;
	    		  hilosListosParaTic = 0;
	    	  }
	      }
   } 
   
   public void imprimirResultador()
   {
	   
   } 
}
