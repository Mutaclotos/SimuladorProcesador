package simulador;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

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
	public static int tipoModulacion; //El tipo de modo del correr dado por el usuario.

    /**
     * Constructor for objects of class main
     */
    public  static void main(String[] args)
    {
    	Principal principal = new Principal()
    	{
    	    public void run()
    	    {
    	    	avanzarReloj();
    	      //System.out.println("Nucleo de procesador");
    	    }
    	};
    	principal.start();
    }
}
