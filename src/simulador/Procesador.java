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
    public int[][] cacheInstrucciones; //El cache de instrucciones del procesador
    public int[] memInstrucciones; //La memoria local de instrucciones del procesador
    public int[] memDatos; //La memoria compartida de datos del procesador
    public int[][] directorio; //El directorio del procesador
    public List<Contexto> colaContextos = new ArrayList<Contexto>(); //Cola circular de contextos
    public List<Contexto> matrizContextos = new ArrayList<Contexto>(); //Matriz que guarda los contextos finales de cada hilillo para ser desplegados al final de la simulacion
    
    public int nombre;
    public Procesador p;
    //Variables de informacion de cache de instrucciones
    public int posicionCacheX;
    public int posicionCacheY;
    public int etiquetaBloque;

    /**
     * Constructor for objects of class Procesador
     */
    public Procesador(int nombre, int tamanoCache, int tamanoMemoriaI, int tamanoMemoriaD, String archivo)
    {
    	//Se inicializan estructuras de datos
    	cacheInstrucciones = new int [5][tamanoCache];
    	memInstrucciones = new int [tamanoMemoriaI];
    	memDatos = new int [tamanoMemoriaD];
    	directorio = new int [tamanoMemoriaD / 4][5];
    	this.nombre = nombre;
    	
    	posicionCacheX = 0;
    	posicionCacheY = 0;
    	etiquetaBloque = -1;
    	
    	for(int i = 0; i < cacheInstrucciones.length; i++)
        {
        	for(int j = 0; j < cacheInstrucciones[i].length; j++)
            {
        		if(i == 4)
        		{
        			cacheInstrucciones[i][j] = -1; //La etiqueta de cada bloque es inicializada en -1
        		}
        		else
        		{
        			cacheInstrucciones[i][j] = 0;
        		}
	        	
            }
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
    	}catch(IOException e){
    		e.printStackTrace();
    	}
       
    }
    
    public void llenarcolaContextos(Contexto contexto)
    {
    	colaContextos.add(contexto);
    }
    
    //Metodo que convierte un numero de bloque y palabra a una direccion en la memoria de datos
    public int convertirADireccionMemoriaDatos(int numBloqueMem, int palabra)
    {
    	return numBloqueMem * 4 + palabra;
    }
    
    //Metodo que imprime una matriz
    public static void imprimirMatriz(int matriz[][])
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
    
    //Metodo que lee un archivo de instrucciones y crea tantos contextos como hayan instrucciones de FIN.
    //Cada contexto guarda el pc respectivo. Tambien se copian todas las instrucciones a la memoria de instrucciones
    public void leerArchivo(String archivo) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        String line = br.readLine();
        int contador = 0;
        int indice = 0;
        Contexto contexto = new Contexto(contador); //Se crea un nuevo contexto vacio
        if(archivo.equals("p0.txt"))
        {
        	indice = indice + 256; //Para P0, la memoria de instrucciones empieza en la direccion 256
        }										
        else
        {
        	indice = indice + 128; //Para P1, la memoria de instrucciones empieza en la direccion 128
        }
        contexto.setPc(indice); //Se guarda la direccion de memoria de la primera instruccion del hilillo
        //System.out.println("Contexto: " + contexto.getPc());
        //Se resetea el indice
        indice = 0;
        while(line != null)
        {
            //System.out.println(line);
            int[] instruccion = convertirLinea(line);
            guardarInstrucciones(instruccion, indice);
            //imprimirArreglo(instruccion, 4);
            indice = indice + 4; //Se avanza el indice de la memoria de instrucciones para guardar la siguiente instruccion
            if(instruccion[0] == 63) //Si un hilillo se termina, se crea un nuevo contexto
            {
            	contador++;
            	llenarcolaContextos(contexto); //Se inserta el contexto en la cola de contexto
            	contexto = new Contexto(contador);
            	
            	if(archivo.equals("p0.txt"))
                {
                	contexto.setPc(indice + 256);
                }										
                else
                {
                	contexto.setPc(indice + 128);
                }
            }

            line = br.readLine(); 
         }
        
        
        int size = colaContextos.size();
        System.out.println("Tamano cola de hilillos: " + size);
        //imprimirArreglo(colaContextos.get(0).getInstruccion(0), 4);
        br.close();
        
        System.out.println("Memoria de instrucciones del Procesador " + nombre);
        imprimirArreglo(memInstrucciones, memInstrucciones.length);
    }
    
    //Metodo que convierte un String en un arreglo de ints
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
    
    //Retorna la cola de contextos
    public List<Contexto> getColaContextos()
    {
    	return colaContextos;
    }
    
    //Metodo que imprime un arreglo
    public void imprimirArreglo(int[] arreglo, int tamano)
    {
    	for(int i = 0; i < tamano; i++)
        {
    		System.out.print(arreglo[i] + ", ");
        }
    	System.out.println();
    }
    
}
