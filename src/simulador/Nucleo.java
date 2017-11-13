package simulador;

import java.util.ArrayList;
import java.util.List;

//import sun.applet.Main;

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
    private int nombre; //Nombre del nucleo
    private int nombreP; //Nombre del procesador al que pertenece este nucleo
    public int quantum;
    private int[] registro;
    public int[][] cacheDatos;
   
    Procesador procesador;
  //Variables de informacion de cache de datos
    public int posicionCacheX;
    public int posicionCacheY;
    public int etiquetaBloque;
    public int estadoBloque;
    public int etiquetaContexto;
    
    /**
     * Constructor for objects of class Nucleo
     */
    public Nucleo(int nombre, Procesador procesador) 
    {
        pc = 0;
        quantum = Principal.quantum;
        etiquetaContexto = -1;
        this.nombre = nombre;
        registro = new int[32];
        cacheDatos = new int[4][6];
        
        this.procesador = procesador;
        
        posicionCacheX = 0;
    	posicionCacheY = 0;
    	etiquetaBloque = -1;
    	estadoBloque = 0;
    	
    	setNombreProcesador(this.procesador.nombre);
    	
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
    	System.out.println("Comenzando simulacion de Nucleo " + nombre + " del Procesador " + procesador.nombre);
    	
    	while(!procesador.colaContextos.isEmpty())
    	{
    		System.out.println("Quantum de nucleo " + nombre + " del Procesador " + procesador.nombre + " es " + quantum);
    		synchronized(procesador.colaContextos) //Si la cola de contextos no estï¿½ bloqueada, bloquearla
    		{
    			etiquetaContexto = procesador.colaContextos.get(0).getEtiqueta(); //Se obtiene la etiqueta de un contexto
    			copiarARegistro(procesador.colaContextos.get(0)); //Se copian los valores del contexto al registro
    			pc = procesador.colaContextos.get(0).getPc(); //Se actualiza el pc con el valor de dicho contexto
        		
        		actualizarCola(); //Se saca el contexto de la cabeza de la cola y se aï¿½ade al final
    		}
    		
    		int[] instruccion = getInstruccion();
    		
    		while(instruccion[0] != 63 && quantum > 0) //Se leen y ejecutan las instrucciones de un hilillo hasta que este se acabe o se termine el quantum
    		{
    			ejecutarOperacion(instruccion); //Al ser ejecutada, tanto el quantum como el PC son actualizados
    			instruccion = getInstruccion(); //Se agarra la siguiente instruccion del hilillo
    		}
    		
    		quantum = Principal.quantum; //Se resetea el valor del quantum
    		
    		if(instruccion[0] == 63) //Si se llego a la instruccion FIN, se saca el contexto del hilillo de la cola de contextos
			{
    			synchronized(procesador.colaContextos)
    			{
    				Contexto contextoRemovido = procesador.colaContextos.remove(etiquetaContexto); //Se elimina el contexto del hilillo de la cola de contextos
    				procesador.matrizContextos.add(contextoRemovido); //El contexto eliminado es incluido en la matriz de contextos para ser desplegado al final de la simulacion
    				System.out.println("Ejecucion de hilillo " + etiquetaContexto + " finalizada.");
    			}
			}
    		else //Si se acaba el quantum para este hilillo, se realiza un cambio de contexto
    		{
    			synchronized(procesador.colaContextos)
    			{
    				copiarAContexto(procesador.colaContextos.get(etiquetaContexto)); //Se copian los valores de registro y pc al contexto relevante
    				System.out.println("Cambio de contexto del nucleo " + nombre + " del Procesador " + procesador.nombre);
    			}
    			
    		}
    	}

    	//Si la cola de contextos está vacia entonces no hay mas hilillo que ejecutar. El nucleo espera su terminacion.
    	esperarAvanceTic();

    	//Si la cola de contextos estï¿½ vacia entonces no hay mas hilillo que ejecutar. El nucleo espera su terminacion.

    	esperarTerminacion();
    }
    
    public void esperarTerminacion()
    {
    	synchronized(this){
    		Principal.hilosTerminados++;
    	if(Principal.hilosTerminados < 3)
    	{
    		try 
        	{
               this.wait();
            } catch (InterruptedException e) 
        	{
               e.printStackTrace();
            }
    	}
    	
    	System.out.println("Hilo de nucleo " + this.nombre + " del Procesador" + procesador.nombre + " terminado.");
    	this.notifyAll();
    	}	
    }
    
    public void esperarAvanceTic()
    {
    	synchronized(this)
    	{
    	Principal.hilosListosParaTic++;
    	if(Principal.hilosListosParaTic < 3)
    	{
    		try 
        	{
    			System.out.println("Nucleo " + nombre + " del Procesador" + procesador.nombre + " esperando avance del tic.");
               this.wait();
            } catch (InterruptedException e) 
        	{
               e.printStackTrace();
            }
    	}
    	
    	System.out.println("Todos los hilos listos para el avance de tic.");
    	this.notifyAll();
    	//this.notify();
    	//this.notify();
    	//System.out.println("Se notifico");
    //	System.out.println("Status1: "+Principal.t.getState());
    	//System.out.println("Status2: "+Thread.currentThread().getState());
    	//Principal.list.notify();
    	//System.out.println("Status: "+Principal.t.getState());
    	//Principal.result.notify();
    
    	}
    }

    //Metodo que obtiene los indices y valores de un dato en la cache de datos
    public void getInformacionCacheD(int numBloqueCache, int palabra)
    {
    	posicionCacheX = numBloqueCache;
    	posicionCacheY = palabra;
    	etiquetaBloque = cacheDatos[numBloqueCache][4]; //La etiqueta de un bloque se guarda en la quinta fila de la matriz
    	estadoBloque = cacheDatos[numBloqueCache][5]; //El estado de un bloque se guarda en la sexta fila de la matriz
    }
    
  //Metodo que convierte una direccion de memoria a un numero de bloque
    public int convertirDireccionANumBloque(int direccionMem)
    {
    	return direccionMem / 16; //El tamaño de bloque de la cache de instrucciones es 16
    }
    
  //Metodo que convierte una direccion de memoria a una posicion de cache
    public int convertirDireccionAPosicionCache(int direccionMem)
    {
    	return convertirDireccionANumBloque(direccionMem) % 4; //En una cache hay 4 bloques 
    }
    
  //Metodo que convierte una direccion de memoria a una palabra
    public int convertirDireccionANumPalabra(int direccionMem)
    {
    	return (direccionMem % 4) / 4;
    }
    
  //Metodo que convierte un numero de bloque y palabra a una direccion en la memoria de instrucciones
    public int convertirADireccionMemoriaInstrucciones(int numBloqueMem, int palabra, int tamanoMemoria)
    {
    	if(tamanoMemoria == 384) //Si la memoria de instrucciones es de P0, hay que restar 16 * 4 palabras
    	{
    		return (numBloqueMem * 4 - 64 + palabra) * 4;
    	}
    	return (numBloqueMem * 4 - 32 + palabra) * 4; //Si la memoria de instrucciones es de P1, hay que restar 8 * 4 palabras
    }
    
  //Metodo que retorna los indices y etiqueta de una instruccion de la cache de instrucciones
    public void getInformacionCacheI(int numBloqueCache, int palabra)
    {
    	posicionCacheX = numBloqueCache * 4;
    	posicionCacheY = palabra;
    	etiquetaBloque = procesador.cacheInstrucciones[numBloqueCache * 4][4]; //La etiqueta de un bloque se guarda en la quinta fila de la matriz
    }

    //Metodo encargado de ejecutar la operacion descrita en una instruccion
    public void ejecutarOperacion(int[] instruccion)
    {
    	//TODO: switch the operaciones y metodos para cada una
    	System.out.println("Instruccion ejecutada: ");
    	imprimirArreglo(instruccion, instruccion.length);
    	quantum--;
    	esperarAvanceTic();
    }
    
    public int[] getInstruccion()
    {
    	int[] instruccion = new int[4];
    	//TODO: fetch instruccion de mem y manejar fail de cache
    	//Se consigue la posicion de la instruccion en la caché de instrucciones junto con su etiqueta dependiendo del pc
    	getInformacionCacheI(convertirDireccionAPosicionCache(pc), convertirDireccionANumPalabra(pc));
    	
    	//Si el numero de bloque es distinto al numero de hilillo, entonces hay un fallo de cache
    	if(etiquetaBloque != etiquetaContexto)
    	{
    		resolverFalloCacheI();
    	}
    	//Se copia la instruccion de cache a la variable
    	System.arraycopy(procesador.cacheInstrucciones[posicionCacheX], posicionCacheY, instruccion, 0, instruccion.length );
    	
    	return instruccion;
    }
    
    //Metodo que copia una instruccion de la memoria de instrucciones a la cache de instruccion cuando ocurre un fallo de cache
    public void resolverFalloCacheI()
    {
    	//Se deben bloquear tanto la cache de instrucciones como la memoria de instrucciones
    	//TODO: Revisar esto
    	synchronized(procesador.cacheInstrucciones)
    	{
    		synchronized(procesador.memInstrucciones)
        	{
    			copiarAcacheInstrucciones();
        	}
    	}
    }
    
    //Metodo que copia una instruccion de la memoria de instrucciones a la cache de instrucciones
    private void copiarAcacheInstrucciones()
    {
    	System.arraycopy(procesador.memInstrucciones, convertirPC(), procesador.cacheInstrucciones[posicionCacheY], 0, procesador.cacheInstrucciones[posicionCacheY].length );
    	procesador.cacheInstrucciones[convertirDireccionAPosicionCache(pc) * 4][4] = etiquetaContexto; //Se actualiza la etiqueta del bloque
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
    
    private int convertirPC()
    {
    	if(procesador.nombre == 0)
    	{
    		return pc - 256;
    	}
    	return pc - 128;
    }
    
    //Remueve la cabeza de la cola y la añade al final
    private void actualizarCola()
    {
    	Contexto cabeza = procesador.colaContextos.remove(0);
    	procesador.colaContextos.add(cabeza);
    }
    
    private void setNombreProcesador(int nombre)
    {
    	this.nombreP = nombre;
    }
    

    public void imprimirArreglo(int[] arreglo, int tamano)
    {
    	for(int i = 0; i < tamano; i++)
        {
    		System.out.print(arreglo[i] + ", ");
        }
    	System.out.println();
    }
    
    public void ejecutar(int op, int o1, int o2, int o3) {    	
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
    	Controlador.hilosListosParaTic++;
    }
}
