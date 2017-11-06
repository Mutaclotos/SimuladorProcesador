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
    public static int[] memDatos; //La memoria compartida de datos del procesador
    public static int[][] directorio; //El directorio del procesador
    public static List<int[]> contexto = new ArrayList<int[]>(); //Permanece vacio hasta el primer cambio de contexto
    public static List<Contexto> colaContextos = new ArrayList<Contexto>(); //Cola circular de hililos

    /**
     * Constructor for objects of class Procesador
     */
    public Procesador(int cantidadNucleos, int tamanoCache, int tamanoMemoriaI, int tamanoMemoriaD, String archivo)
    {
    	//Se inicializan estructuras de datos
    	cacheInstrucciones = new int [tamanoCache];
    	memInstrucciones = new int [tamanoMemoriaI];
    	memDatos = new int [tamanoMemoriaD];
    	directorio = new int [tamanoMemoriaD / 4][5];
    	
    	for(int i = 0; i < tamanoCache; i++)
	    {
    		cacheInstrucciones[i] = 0;
	    }
    	
    	for(int i = 0; i < tamanoMemoriaI; i++)
	    {
    		memInstrucciones[i] = 0;
	    }
    	
    	for(int i = 0; i < tamanoMemoriaD; i++)
	 	{
	        memDatos[i] = 0;
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
    
    public void llenarcolaContextos(Contexto hilillo)
    {
    	colaContextos.add(hilillo);
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
        int indice = 0;
        Contexto contexto = new Contexto(contador); //Se crea un nuevo contexto vacio
        
        while(line != null)
        {
            //System.out.println(line);
            int[] instruccion = convertirLinea(line);
            guardarInstrucciones(instruccion, indice);
            indice = indice + 4; //Se avanza el indice de la memoria de instrucciones para guardar la siguiente instruccion
            if(instruccion[0] == 63 && br.readLine() != null) //Si un programa se termina y el texto aun no termina, se crea un nuevo contexto
            {
            	contador++;
            	llenarcolaContextos(contexto); //Se inserta el contexto en la cola de contexto
            	contexto = new Contexto(contador);
            }

            line = br.readLine(); 
        }
        
        
        int size = colaContextos.size();
        //System.out.println("Tamaño cola de hilillos: " + size);
        //imprimirArreglo(colaContextos.get(0).getInstruccion(0), 4);
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
    
    //Metodo que copia las instrucciones leidas del documento de texto a la memoria de instrucciones de cada procesador
    public void guardarInstrucciones(int[] instruccion, int indice)
    {
    	for(int i = 0; i < instruccion.length; i++)
        {
    		memInstrucciones[i + indice] = instruccion[i];
        }
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
