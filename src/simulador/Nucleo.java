package simulador;


/**
 * Write a description of class Nucleo here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Nucleo extends Thread
{
    // instance variables - replace the example below with your own

    private int pc;
    private int nombre;
    private int[] registro;
    public int[] cacheDatos;
    public static boolean cacheBloqueada; //Booleana que determina si la cache de datos esta bloqueada o no
    public static int hilosTerminados; //Booleana que determina si el nucleo esta listo para terminar su ejecucion
    
    /**
     * Constructor for objects of class Nucleo
     */
    public Nucleo(int nombre)
    {
        pc = 0;
        this.nombre = nombre;
        registro = new int[32];
        cacheDatos = new int[24];
        hilosTerminados = 0;
        cacheBloqueada = false;
        //Se inicializa el registro en 0
        for(int i = 0;i < 32; i++)
        {
            registro[i] = 0;
        }
        //Se inicializa la cache de datos en 0 con etiquetas -1 y en estado Invalido (I = 0, C = 1 y M = 2)
        for(int i = 0; i < 24; i++)
        {
        	if(i == 4 || i == 10 || i == 16 || i == 22) //Las etiquetas se inicializan en 1
        	{
        		cacheDatos[i] = -1;
        	}
        	else
        	{
        		cacheDatos[i] = 0;
        	}
            
        }
        System.out.println("Nucleo " + nombre + " inicializado.");
        //imprimirArreglo(cacheDatos, 24);
    }
    
    public void simularNucleo()
    {
    	System.out.println("Comenzando simulacion de Nucleo " + nombre + ".");
    	while(!Procesador.colaHilillos.isEmpty())
    	{
    		if(Procesador.colaHilillosBloqueada)
    		{
    			try 
            	{
    				this.wait();
                } catch (InterruptedException e) 
            	{
                   e.printStackTrace();
                }
    		}

    		Procesador.colaHilillosBloqueada = true; //Si la cola de hilillos no está bloqueada, bloquearla
    		
    		copiarAMemoriaInstrucciones();
    		
    		actualizarCola();
    		
    		Procesador.colaHilillosBloqueada = false; //Se libera la cola de hilillos
    		notifyAll();
    		
    	}
    	esperarTerminacion();
    }
    
    public synchronized void esperarTerminacion()
    {
    	hilosTerminados++;
    	if(hilosTerminados < 3)
    	{
    		try 
        	{
               this.wait();
            } catch (InterruptedException e) 
        	{
               e.printStackTrace();
            }
    	}
    	
    	System.out.println("Hilo de nucleo " + this.nombre + " terminado.");
    	notifyAll();
    	
    }
    
    //Copia todas las instrucciones de un hilillo a la memoria local de instrucciones de un procesador
    private void copiarAMemoriaInstrucciones()
    {
    	int cantidad = Procesador.colaHilillos.get(0).getCantidadInst();
    	int[] instruccion;
    	int indice = 0;
    	int index = 0;
    	//TODO: Actualizar pc
    	for(int i = pc; i < cantidad; i++)
        {
    		instruccion = Procesador.colaHilillos.get(0).getInstruccion(i);
    		for(int j = indice; j < indice + 4; j++)
            {
        		Procesador.memInstrucciones[indice] = instruccion[index];
        		index++;
            }
    		indice = indice + 4;
    		index = 0;
        }
    	
    }
    
    //Remueve la cabeza de la cola y la añade al final
    private void actualizarCola()
    {
    	Hilillo cabeza = Procesador.colaHilillos.remove(0);
    	Procesador.colaHilillos.add(cabeza);
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
