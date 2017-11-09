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
    public int[][] cacheDatos;
    
    
    /**
     * Constructor for objects of class Nucleo
     */
    public Nucleo(int nombre)
    {
        pc = 0;
        quantum = 0;
        this.nombre = nombre;
        registro = new int[32];
        cacheDatos = new int[4][6];
        //Se inicializa el registro en 0
        for(int i = 0;i < 32; i++)
        {
            registro[i] = 0;
        }
        //Se inicializa la cache de datos en 0 con etiquetas -1 y en estado Invalido (I = 0, C = 1 y M = 2)
        for(int i = 0; i < cacheDatos.length; i++)
        {
        	for(int j = 0; j < cacheDatos[i].length; j++)
            {
	        	if(j == 4) //Las etiquetas se inicializan en 1
	        	{
	        		cacheDatos[i][j] = -1;
	        	}
	        	else
	        	{
	        		cacheDatos[i][j] = 0;
	        	}
            }
        }
        System.out.println("Nucleo " + nombre + " inicializado.");
        //imprimirArreglo(cacheDatos, 24);
    }
    
    public void simularNucleo()
    {
    	System.out.println("Comenzando simulacion de Nucleo " + nombre + ".");
    	int etiqueta;
    	while(!Procesador.colaContextos.isEmpty())
    	{
    		
    		synchronized(Procesador.colaContextos) //Si la cola de contextos no está bloqueada, bloquearla
    		{
    			etiqueta = Procesador.colaContextos.get(0).getEtiqueta(); //Se obtiene la etiqueta de un contexto
    			copiarARegistro(Procesador.colaContextos.get(0)); //Se copian los valores del contexto al registro
    			pc = Procesador.colaContextos.get(0).getPc(); //Se actualiza el pc con el valor de dicho contexto
        		
        		actualizarCola(); //Se saca el contexto de la cabeza de la cola y se añade al final
    		}
    		
    		int[] instruccion = getInstruccion();
    		
    		while(instruccion[0] != 63 && quantum > 0) //Se leen y ejecutan las instrucciones de un hilillo hasta que este se acabe o se termine el quantum
    		{
    			ejecutarOperacion(instruccion); //Al ser ejecutada, tanto el quantum como el PC son actualizados
    			instruccion = getInstruccion(); //Se agarra la siguiente instruccion del hilillo
    		}
    		
    		quantum = Controlador.quantum; //Se resetea el valor del quantum
    		
    		if(instruccion[0] == 63) //Si se llego a la instruccion FIN, se saca el contexto del hilillo de la cola de contextos
			{
    			synchronized(Procesador.colaContextos)
    			{
    				Contexto contextoRemovido = Procesador.colaContextos.remove(etiqueta); //Se elimina el contexto del hilillo de la cola de contextos
    				Procesador.matrizContextos.add(contextoRemovido); //El contexto eliminado es incluido en la matriz de contextos para ser desplegado al final de la simulacion
    				System.out.println("Ejecucion de hilillo " + etiqueta + " finalizada.");
    			}
			}
    		else //Si se acabó el quantum para este hilillo, se realiza un cambio de contexto
    		{
    			synchronized(Procesador.colaContextos)
    			{
    				copiarAContexto(Procesador.colaContextos.get(etiqueta)); //Se copian los valores de registro y pc al contexto relevante
    				System.out.println("Cambio de contexto del nucleo " + nombre + ".");
    			}
    			
    		}
    	}
    	//Si la cola de contextos está vacia entonces no hay mas hilillo que ejecutar. El nucleo espera su terminacion.
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
    public void ejecutarOperacion(int[] instruccion)
    {
    	//TODO: switch the operaciones y metodos para cada una
    }
    
    public int[] getInstruccion()
    {
    	int[] instruccion = new int[4];
    	//TODO: fetch instruccion de mem y manejar fail de cache
    	return instruccion;
    }
    
    //Metodo que copia los registros del primer contexto de la cola de contextos al registro del nucleo
    private void copiarARegistro(Contexto contexto)
    {
    	System.arraycopy(contexto.getRegistros(), 0, registro, 0, contexto.getRegistros().length );
    }
    
    //Metodo que actualiza el contexto dentro de la cola de contextos cuando se realiza un cambio de contexto
    private void copiarAContexto(Contexto contexto)
    {
    	contexto.pc = pc; //Se actualiza el pc del contexto
    	contexto.setRegistros(registro); //Se actualizan los registros del contexto
    }
    
    //Remueve la cabeza de la cola y la añade al final
    private void actualizarCola()
    {
    	Contexto cabeza = Procesador.colaContextos.remove(0);
    	Procesador.colaContextos.add(cabeza);
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
