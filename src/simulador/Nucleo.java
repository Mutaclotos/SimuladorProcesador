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
    	while(!Procesador.colaContextos.isEmpty())
    	{
    		int etiqueta;
    		synchronized(Procesador.colaContextos) //Si la cola de contextos no est� bloqueada, bloquearla
    		{
    			etiqueta = Procesador.colaContextos.get(0).getEtiqueta();
    			copiarARegistro(Procesador.colaContextos.get(0)); //Se copian los valores del contexto al registro
    			pc = Procesador.colaContextos.get(0).getPc();
        		
        		actualizarCola();
    		}

    		//esperarAvanceTic();
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
    
    //Metodo que copia los valores del primer contexto de la cola de contextos al registro del nucleo
    private void copiarARegistro(Contexto contexto)
    {
    	registro = contexto.getRegistros();
    }
    
    //Remueve la cabeza de la cola y la a�ade al final
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
    
    public void ejecutar() {
    	int op = 0;
    	int o1 = 0;
    	int o2 = 0;
    	int o3 = 0;
    	
    	switch (op) {
    	case 8: // daddi
    		this.registro[o1] = this.registro[o2] + o3;
    		break;
    	case 32: // dadd
    		this.registro[o1] = this.registro[o2] + this.registro[o3];
    		break;
    	case 34: // dsub
    		this.registro[o1] = this.registro[o2] - this.registro[o3];
    		break;
    	case 12: // dmul
    		this.registro[o1] = this.registro[o2] * this.registro[o3];
    		break;
    	case 14: // ddiv
    		this.registro[o1] = this.registro[o2] / this.registro[o3];
    		break;
    	}
    }
}
