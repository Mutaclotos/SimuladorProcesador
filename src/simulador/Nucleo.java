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
    public int quantum;
    private int[] registro;
    public int[] cacheDatos;
    
    
    /**
     * Constructor for objects of class Nucleo
     */
    public Nucleo(int nombre)
    {
        pc = 0;
        quantum = 0;
        this.nombre = nombre;
        registro = new int[32];
        cacheDatos = new int[24];
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
    		int hililloAEjecutar;
    		synchronized(Procesador.colaHilillos) //Si la cola de hilillos no está bloqueada, bloquearla
    		{
    			/*try 
            	{
    				Procesador.colaHilillos.wait();
                } catch (InterruptedException e) 
            	{
                   e.printStackTrace();
                }*/
    			hililloAEjecutar = copiarAMemoriaInstrucciones();
        		
        		actualizarCola();
    		}
    		
    		synchronized(Procesador.contexto) //Si el contexto no está bloqueado, bloquearlo
    		{
    			if(Procesador.cantidadContextos > 0)
    			{
    				buscarContexto(hililloAEjecutar);
    			}
    		}

    		//Procesador.colaHilillosBloqueada = true; //Si la cola de hilillos no está bloqueada, bloquearla
    		
    		//Procesador.colaHilillosBloqueada = false; //Se libera la cola de hilillos
    		//notifyAll();
    		esperarAvanceTic();
    	}
    	esperarTerminacion();
    }
    
    public void esperarTerminacion()
    {
    	Controlador.hilosTerminados++;
    	if(Controlador.hilosTerminados < 3)
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
    	this.notifyAll();
    	
    }
    
    public void esperarAvanceTic()
    {
    	Controlador.hilosListosParaTic++;
    	if(Controlador.hilosListosParaTic < 3)
    	{
    		try 
        	{
               this.wait();
            } catch (InterruptedException e) 
        	{
               e.printStackTrace();
            }
    	}
    	
    	System.out.println("Todos los hilos listos para el avance de tic.");
    	this.notifyAll();
    }
    
    //Metodo encargado de ejecutar la operacion descrita en una instruccion
    public void ejecutarOperacion()
    {
    	
    }
    
    public void buscarContexto(int etiqueta)
    {
    	int[] contextoActual;
    	for(int i = 0; i < Procesador.cantidadContextos; i++)
    	{
    		contextoActual = Procesador.contexto.get(i);
    		if(contextoActual[0] == etiqueta) //Solo se cargan los registros y pc del hilillo correspondiente
    		{
    			this.pc = contextoActual[1];
    			for(int j = 0; j < registro.length; j++)
    	    	{
    				registro[j] = contextoActual[j + 2];
    	    	}
    		}
    		
    	}
    }
    
    //Copia todas las instrucciones de un hilillo a la memoria local de instrucciones de un procesador
    private int copiarAMemoriaInstrucciones()
    {
    	int cantidad = Procesador.colaHilillos.get(0).getCantidadInst();
    	int etiqueta = Procesador.colaHilillos.get(0).getEtiqueta();
    	int[] instruccion;
    	int indice = 0;
    	int index = 0;
    	//TODO: Actualizar pc
    	for(int i = pc; i < cantidad; i++)
        {
    		instruccion = Procesador.colaHilillos.get(0).getInstruccion(i); //Solo se copian las instrucciones de la cabeza de la cola
    		for(int j = indice; j < indice + 4; j++)
            {
        		Procesador.memInstrucciones[indice] = instruccion[index];
        		index++;
            }
    		indice = indice + 4;
    		index = 0;
        }
    	return etiqueta;
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
