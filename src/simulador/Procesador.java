package simulador;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Write a description of class Procesador here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Procesador
{
    private int[] cacheInstrucciones; //El cache de instrucciones del procesador
    public static int[] memInstrucciones; //La memoria local de instrucciones del procesador
    public static int[][] directorio; //El directorio del procesador
    public static boolean directorioBloqueado; //Booleana que determina si el directorio esta bloqueado o no
    public static List<int[]> contexto = new ArrayList<int[]>(); //Permanece vacio hasta el primer cambio de contexto
    public static int cantidadContextos; //Cantidad de filas guardadas en la matriz de contextos
    public static List<Hilillo> colaHilillos = new ArrayList<Hilillo>(); //Cola circular de hililos
    public static boolean colaHilillosBloqueada; //Booleana que determina si la cola de hilillos esta bloqueada o no

    /**
     * Constructor for objects of class Procesador
     */
    public Procesador(int cantidadNucleos, int tamanoCache, int tamanoMemoriaI, int tamanoMemoriaD, String archivo)
    {
    	//Se inicializan estructuras de datos
    	cacheInstrucciones = new int [tamanoCache];
    	memInstrucciones = new int [tamanoMemoriaI];
    	directorio = new int [tamanoMemoriaD / 4][5];
    	directorioBloqueado = false;
    	cantidadContextos = 0;
    	
    	for(int i = 0; i < tamanoCache; i++)
	    {
    		cacheInstrucciones[i] = 0;
	    }
    	
    	for(int i = 0; i < tamanoMemoriaI; i++)
	    {
    		memInstrucciones[i] = 0;
	    }
    	
    	for(int i = 0; i < directorio.length; i++)
	    {
    		for(int j = 0; j < directorio[i].length; j++)
    	    {
    			if(j == 0)
    			{
    				directorio[i][j] = i;
    			}
    			else
    			{
    				directorio[i][j] = 0;
    			}
    	    }
	    }
    	
    	//System.out.println("Directorio: ");
    	//imprimirMatriz(directorio);
    	
    	try
    	{
    		leerArchivo(archivo); //Se lee el archivo indicado para cargar las instrucciones a la cola de hilillos
    	}catch(IOException e){}
    	
    	
	    for(int i=0; i < cantidadNucleos; i++)
	    {
	    	Nucleo nucleo = new Nucleo(i)
	    	{
	    	    public void run()
	    	    {
	    	    	simularNucleo();
	    	      //System.out.println("Nucleo de procesador");
	    	    }
	    	};

	    	nucleo.start();
	    }
       
    }
    
    public void llenarColaHilillos(Hilillo hilillo)
    {
    	colaHilillos.add(hilillo);
    }
    
    public void imprimirMatriz(int matriz[][])
    {
    	for(int i = 0; i < matriz.length; i++)
	    {
    		for(int j = 0; j < matriz[i].length; j++)
    	    {
    			
    			System.out.print(matriz[i][j] + " ");
    			 
    	    }
    		System.out.println();
	    }
    }
    
    public void leerArchivo(String archivo) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String line = br.readLine();
        int contador = 0;
        int cantidad = 0;
        
        Hilillo hilillo = new Hilillo(contador); //Se crea un nuevo hilillo para insertar las instrucciones leidas en el primer programa
        
        while(line != null)
        {
            //System.out.println(line);
            int[] instruccion = convertirLinea(line);
            hilillo.cargarInstrucciones(instruccion);
            
            if(instruccion[0] == 63 && br.readLine() != null) //Si un programa se termina
            {
            	contador++;
            	llenarColaHilillos(hilillo); //Se inserta el hilillo en la cola de hilillos
            	cantidad = 0;
            	hilillo = new Hilillo(contador);
            }
            else
            {
            	cantidad++;
            	hilillo.setCantidadInst(cantidad);
            	System.out.println("Instruccion añadida al hilillo: ");
            	imprimirArreglo(hilillo.getInstruccion(cantidad - 1), 4);
            	//System.out.println("Cantidad instrucciones hilillo: " + hilillo.getCantidadInst());
            }
            line = br.readLine(); 
        }
        
        
        int size = colaHilillos.size();
        //System.out.println("Tamaño cola de hilillos: " + size);
        //imprimirArreglo(colaHilillos.get(0).getInstruccion(0), 4);
        br.close();
    }
    
    public int[] convertirLinea(String linea)
    {
    	String[] instrucs = linea.split(" ");
    	int[] instrucciones = new int[4];
    	for(int i = 0; i < instrucs.length; i++)
    	{
    		instrucciones[i] = Integer.parseInt(instrucs[i]);
    	}
    	//imprimirArreglo(instrucciones, 4);
    	return instrucciones;
    }
    
    public void imprimirArreglo(int[] arreglo, int tamano)
    {
    	for(int i = 0; i < tamano; i++)
        {
    		System.out.print(arreglo[i] + ", ");
        }
    	System.out.println();
    }
    
}
