package simulador;

import java.sql.Timestamp;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private boolean bandera;
    public int[][] cacheDatos;
    private static Object syncNucleo = new Object();
    private Timestamp timestamp;
   
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
        cacheDatos = new int[6][4];
        bandera = false;
        this.procesador = procesador;
        
        posicionCacheX = 0;
    	posicionCacheY = 0;
    	etiquetaBloque = -1;
    	estadoBloque = 0;
    	timestamp = new Timestamp(System.currentTimeMillis());
    	
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
	        	if(i == 4) //Las etiquetas se inicializan en 1
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
    }
    
    public synchronized void simularNucleo()
    {
    	System.out.println("Comenzando simulacion de Nucleo " + nombre + " del Procesador " + procesador.nombre);
    	
    	while(!procesador.ultimoHilillo)
    	{
    		
    		synchronized(procesador.colaContextos)
    		{
    			
    			if(procesador.colaContextos.size() == 1 && !procesador.ultimoHilillo)
    			{
    				procesador.ultimoHilillo = true;
    				bandera = true;
    			}
    		}
    		
    		if(!bandera)
    		{
    			//System.out.println("Quantum de nucleo " + nombre + " del Procesador " + procesador.nombre + " es " + quantum);
        		synchronized(procesador.colaContextos) //Si la cola de contextos no est� bloqueada, bloquearla
        		{
        			
        			this.etiquetaContexto = procesador.colaContextos.get(0).getEtiqueta(); //Se obtiene la etiqueta de un contexto
        			copiarARegistro(procesador.colaContextos.get(0)); //Se copian los valores del contexto al registro
        			this.pc = procesador.colaContextos.get(0).getPc(); //Se actualiza el pc con el valor de dicho contexto
            		actualizarCola(); //Se saca el contexto de la cabeza de la cola y se a�ade al final
            		
            		System.out.println("Nucleo " + nombre + " del Procesador " + procesador.nombre + " ejecutando hilillo " + etiquetaContexto);
        		}
        		
        		int[] instruccion = getInstruccion();
        		
        		float inicio = timestamp.getTime(); //Inicio de la ejecucion del hilillo
        		
        		while(instruccion[0] != 63 && quantum > 0) //Se leen y ejecutan las instrucciones de un hilillo hasta que este se acabe o se termine el quantum
        		{
        			imprimirArreglo(instruccion, instruccion.length);
        			ejecutarOperacion(instruccion); //Al ser ejecutada, tanto el quantum como el PC son actualizados
        			instruccion = getInstruccion(); //Se agarra la siguiente instruccion del hilillo
        		}
        		
        		float fin = timestamp.getTime(); //Fin de la ejecucion del hilillo
        		
        		quantum = Principal.quantum; //Se resetea el valor del quantum
        		
        		if(instruccion[0] == 63) //Si se llego a la instruccion FIN, se saca el contexto del hilillo de la cola de contextos
    			{
        			synchronized(procesador.colaContextos)
        			{
        				int size = procesador.colaContextos.size();
        		        System.out.println("Tamano cola de contextos de procesador " + procesador.nombre + ": " + size);
        		        //System.out.println("Etiqueta de contexto a eliminar por el procesador " + procesador.nombre + ": " + this.etiquetaContexto);
        		        //System.out.println("Cabeza de la cola del procesador " + procesador.nombre + ": " + procesador.colaContextos.get(0).getEtiqueta());
        		        
        		        if(getIndiceCola(etiquetaContexto) != -1)
        		        {
        		        	procesador.colaContextos.get(getIndiceCola(etiquetaContexto)).setTiempoEjecucion(fin - inicio);
        		        	Contexto contextoRemovido = procesador.colaContextos.remove(getIndiceCola(etiquetaContexto)); //Se elimina el contexto del hilillo de la cola de contextos
                			procesador.matrizContextos.add(contextoRemovido); //El contexto eliminado es incluido en la matriz de contextos para ser desplegado al final de la simulacion
        		        }
        		        else
        		        {
        		        	System.out.println("----Contexto no encontrado en la cola del procesador " + procesador.nombre);
        		        }
        		        
        				System.out.println("Ejecucion de hilillo " + etiquetaContexto + " finalizada por el nucleo " + nombre + " del Procesador " + procesador.nombre);
        			}
    			}
        		else //Si se acaba el quantum para este hilillo, se realiza un cambio de contexto
        		{
        			synchronized(procesador.colaContextos)
        			{
        				if(getIndiceCola(etiquetaContexto) != -1)
        		        {
        					copiarAContexto(procesador.colaContextos.get(getIndiceCola(etiquetaContexto)), fin - inicio); //Se copian los valores de registro y pc al contexto relevante
        					System.out.println("Cambio de contexto del nucleo " + nombre + " del Procesador " + procesador.nombre);
        		        }
        				else
        		        {
        		        	System.out.println("----Contexto no encontrado en la cola del procesador " + procesador.nombre);
        		        }
        			}
        			
        		}
    		}
    		
    	}

    	//Si la cola de contextos est� vacia entonces no hay mas hilillo que ejecutar. El nucleo espera su terminacion.

    	esperarTerminacion();
    }
    
    //Metodo que prepara a un hilo para dar tics hasta que todos los hilos hayan finalizado su ejecucion
    public void esperarTerminacion()
    {
    		Principal.hilosTerminados++;
    		//System.out.println("Tamano cola: " + procesador.colaContextos.size());
    		System.out.println("---------------------");
    		System.out.println("Nucleo " + nombre + " del Procesador" + procesador.nombre + " esperando terminacion.");
    		System.out.println("---------------------");
	    	while(Principal.hilosTerminados < 3)
	    	{
	    		esperarAvanceTic();
	    	}
	    	
	    	System.out.println("Hilo de nucleo " + this.nombre + " del Procesador" + procesador.nombre + " terminado.");
    }
    
    //Metodo de sincronizacion de hilos: los hilos esperan hasta estar todos listos para avanzar al siguiente tic y le comunican este hecho al hilo principal
    public synchronized void esperarAvanceTic()
    {
    	synchronized(syncNucleo)
    	{
	    	Principal.hilosListosParaTic++;
	    	if(Principal.hilosListosParaTic < 3) //Los primeros dos hilos en entrar a este bloque de codigo se quedan en espera
	    	{
	    		try 
	        	{
	    			//System.out.println("Nucleo " + nombre + " del Procesador " + procesador.nombre + " esperando avance del tic.");
	    			syncNucleo.wait();
	            } catch (InterruptedException e) 
	        	{
	               e.printStackTrace();
	            }
	    	}
	    	
	    	synchronized(Principal.syncPrincipal)
	    	{
				if(Principal.hilosListosParaTic == 3)
		    	{
	    			Principal.hilosListosParaTic = 0; //Se reinicializa el contador
	    			System.out.println("Todos los hilos listos para el avance de tic.");
	    			Principal.syncPrincipal.notify(); //Se notifica al hilo principal para que este avance el tic
	    			try 
		        	{
		    			//System.out.println("Nucleo " + nombre + " del Procesador " + procesador.nombre + " esperando respuesta de principal.");
		    			
		    			Principal.syncPrincipal.wait(); //El nucleo espera hasta 
		            } catch (InterruptedException e) 
		        	{
		               e.printStackTrace();
		            }
	    		}
	    		
	    	}
	    	
	    	syncNucleo.notify(); //Cada hilo de nucleo notifica a otro para que este pare de esperar y continue con su ejecucion
    	}
    	
    }
    
    //Metodo que retorna el indice de un contexto de la cola de contextos
    public synchronized int getIndiceCola(int etiquetaContexto)
    {
    	for(int i = 0; i < procesador.colaContextos.size(); i++)
    	{
    		if(procesador.colaContextos.get(i).getEtiqueta() == etiquetaContexto)
    		{
    			return i;
    		}
    	}
    	return -1;
    }
    
    

    //Metodo que obtiene los indices y valores de un dato en la cache de datos
    public void getInformacionCacheD(int numBloqueCache, int palabra)
    {
    	posicionCacheX = palabra;
    	posicionCacheY = numBloqueCache;
    	synchronized(cacheDatos)
    	{
    		etiquetaBloque = cacheDatos[4][numBloqueCache]; //La etiqueta de un bloque se guarda en la quinta fila de la matriz
    		estadoBloque = cacheDatos[5][numBloqueCache]; //El estado de un bloque se guarda en la sexta fila de la matriz
    	}
    }
    
  //Metodo que convierte una direccion de memoria a un numero de bloque
    public int convertirDireccionANumBloque(int direccionMem)
    {
    	return direccionMem / 4; //El tama�o de bloque de la cache de instrucciones es 4
    }
    
  //Metodo que convierte una direccion de memoria a una posicion de cache
    public int convertirDireccionAPosicionCache(int direccionMem)
    {
    	return convertirDireccionANumBloque(direccionMem) % 4; //En una cache hay 4 bloques 
    }
    
  //Metodo que convierte una direccion de memoria a una palabra
    public int convertirDireccionANumPalabra(int direccionMem)
    {
    	//return (direccionMem % 4) / 4;
    	return direccionMem % 4;
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
    	posicionCacheX = palabra;
    	posicionCacheY = numBloqueCache * 4;
    	synchronized(procesador.cacheInstrucciones)
    	{
    		etiquetaBloque = procesador.cacheInstrucciones[4][numBloqueCache * 4]; //La etiqueta de un bloque se guarda en la quinta fila de la matriz
    	}
    }

    //Metodo encargado de ejecutar la operacion descrita en una instruccion
    public void ejecutarOperacion(int[] ins)
    {
    	//System.out.println("soy el nucleo:" + this.nombre + " y acceso al nucelo:" + this.procesador.p.nucleos[0].nombre);
    	switch (ins[0]) 
    	{
	    	case 8: // daddi
	    		System.out.println("Ejecutando DADDI " + ins[1] + ", " + ins[2] + ", " + ins[3] + " :");
	    		this.registro[ins[2]] = this.registro[ins[1]] + ins[3];
	    		this.pc += 4;
	    		break;
	    	case 32: // dadd
	    		System.out.println("Ejecutando DADD " + ins[1] + ", " + ins[2] + ", " + ins[3] + " :");
	    		this.registro[ins[3]] = this.registro[ins[1]] + this.registro[ins[2]];
	    		this.pc += 4;
	    		break;
	    	case 34: // dsub
	    		System.out.println("Ejecutando DSUB " + ins[1] + ", " + ins[2] + ", " + ins[3] + " :");
	    		this.registro[ins[3]] = this.registro[ins[1]] - this.registro[ins[2]];
	    		this.pc += 4;
	    		break;
	    	case 12: // dmul
	    		System.out.println("Ejecutando DMUL " + ins[1] + ", " + ins[2] + ", " + ins[3] + " :");
	    		this.registro[ins[3]] = this.registro[ins[1]] * this.registro[ins[2]];
	    		this.pc += 4;
	    		break;
	    	case 14: // ddiv
	    		System.out.println("Ejecutando DDIV " + ins[1] + ", " + ins[2] + ", " + ins[3] + " :");
	    		this.registro[ins[3]] = this.registro[ins[1]] / this.registro[ins[2]];
	    		this.pc += 4;
	    		break;
	    	case 4: // beqz
	    		System.out.println("Ejecutando BEQZ " + ins[1] + ", " + ins[3] + " :");
	    		if(this.registro[ins[1]] == 0)
	    			this.pc += ins[3] * 4;
	    		break;
	    	case 5: // bnez
	    		System.out.println("Ejecutando BNEZ " + ins[1] + ", " + ins[3] + " :");
	    		if(this.registro[ins[1]] != 0)
	    			this.pc += ins[3] * 4;
	    		break;
	    	case 3: // jal
	    		System.out.println("Ejecutando JAL "+ ins[3] + " :");
	    		this.registro[31]= this.pc;
	    		this.pc += ins[3];
	    		break;
	    	case 2: // jr
	    		System.out.println("Ejecutando JR " + ins[1] + " :");
	    		this.pc = this.registro[ins[1]];
	    		break;
	    	case 35: // lw
	    		System.out.println("Ejecutando LW " + ins[2] + ", " + ins[3] + "(" + ins[1] + ") :");
	    		loadWord(ins[3] + this.registro[ins[1]], ins[2]);
	    		break;
	    	case 43: // sw
	    		System.out.println("Ejecutando SW " + ins[2] + ", " + ins[3] + "(" + ins[1] + ") :");
	    		storeWord(ins[3] + this.registro[ins[1]], ins[2]);
	    		break;
	    	case 63: // fin
	    		System.out.println("Instruccion FIN ejecutada.");
	    		this.pc += 4;
	    		break;
	    	default:
	    		System.out.println("Instruccion no valida.");
	    		this.pc += 4;
	    		break;
    	}
    	this.quantum--; //Se reduce el quantum de 1 por cada instruccion ejecutar
    	esperarAvanceTic();
    }
    
    //Metodo que retorna la instruccion a ser ejecutada dependiendo del pc. En caso de que no se encuentre el 
    //numero de bloque apropiado, se produce un fallo de cache de instrucciones
    public int[] getInstruccion()
    {
    	int[] instruccion = new int[4];

    	//Se consigue la posicion de la instruccion en la cach� de instrucciones junto con su etiqueta dependiendo del pc
    	getInformacionCacheI(convertirDireccionAPosicionCache(pc), convertirDireccionANumPalabra(pc));
    	
    	//Si el numero de bloque es distinto al numero de hilillo, entonces hay un fallo de cache
    	if(etiquetaBloque != convertirDireccionANumBloque(pc))
    	{
    		System.out.println("Nucleo " + nombre + " de Procesador " + procesador.nombre + " obtuvo un cache FAIL.");
    		copiarAcacheInstrucciones(); //Se resuelve el fallo de cache al copiar el bloque de memoria al bloque de cache
    	}
    	System.out.println("Pc de nucleo " + nombre + " de Procesador " + procesador.nombre + ": " + convertirPC());
    	//Se copia la instruccion de cache a la variable
    	
    	synchronized(procesador.cacheInstrucciones)
    	{//Se copia una palabra de la cache de instrucciones a la variable a retornar
    		System.arraycopy(procesador.cacheInstrucciones[posicionCacheX], posicionCacheY, instruccion, 0, instruccion.length );
    	}
    	return instruccion;
    }
    
    //Metodo que copia una instruccion de la memoria de instrucciones a la cache de instruccion cuando ocurre un fallo de cache    
    private synchronized void copiarAcacheInstrucciones()
    {
    	int[][] tempArray;
    	int indice = 0;
    	//System.out.println("Pc de nucleo " + nombre + " de Procesador " + procesador.nombre + ": " + convertirPC());
    	synchronized(procesador.memInstrucciones)
    	{
    		tempArray = new int[4][4];
    		//System.out.println(procesador.memInstrucciones.length + " " +convertirPC());
    		
    		for(int i = 0; i < 4; i++)//Se copia el bloque entero de la memoria de instrucciones a una matriz temporal
    		{
    			System.arraycopy(procesador.memInstrucciones, convertirPC() + indice, tempArray[i], 0, tempArray[i].length);
    			indice += 4;
    		}
    		
    		
    	}
    	
    	synchronized(procesador.cacheInstrucciones)
    	{
    		for(int i = 0; i < 4; i++)//Se copia la matriz temporal al bloque respectivo de la cache de instrucciones
    		{
    			System.arraycopy(tempArray[i], 0, procesador.cacheInstrucciones[i], posicionCacheY, procesador.cacheInstrucciones[i].length / 4);
    		}
    		
	    	//imprimirArreglo(procesador.cacheInstrucciones[posicionCacheX], procesador.cacheInstrucciones[posicionCacheX].length);
	    	procesador.cacheInstrucciones[4][convertirDireccionAPosicionCache(pc) * 4] = convertirDireccionANumBloque(pc); //Se actualiza la etiqueta del bloque
	    	//Procesador.imprimirMatriz(procesador.cacheInstrucciones);
    	}
    }
    
    //Metodo que copia los registros del primer contexto de la cola de contextos al registro del nucleo
    private void copiarARegistro(Contexto contexto)
    {
    	synchronized(contexto)
    	{
    		System.arraycopy(contexto.getRegistros(), 0, registro, 0, contexto.getRegistros().length );
    	}
    }
    
    //Metodo que actualiza el contexto dentro de la cola de contextos cuando se realiza un cambio de contexto
    private synchronized void copiarAContexto(Contexto contexto, float tiempo)
    {
	    contexto.setPc(pc); //Se actualiza el pc del contexto
	    contexto.setRegistros(registro); //Se actualizan los registros del contexto
    	contexto.setTiempoEjecucion(tiempo);
    }
    
    //Metodo que convierte la direccion de memoria del pc a un indice que puede ser utilizado en los arreglos de memoria de instrucciones
    private int convertirPC()
    {
    	if(procesador.nombre == 0)
    	{
    		return pc - 256;
    	}
    	return pc - 128;
    }
    
    //Remueve la cabeza de la cola y la a�ade al final
    private synchronized void actualizarCola()
    {
    	if(!procesador.colaContextos.isEmpty() && procesador.colaContextos.size() > 1)
    	{
    		Contexto cabeza = procesador.colaContextos.remove(0);
        	procesador.colaContextos.add(cabeza);
    	}
    	
    }
    
    private synchronized boolean verificarCola()
    {
    	return procesador.colaContextos.isEmpty();
    }
    
    
    //Metodo que imprime un arreglo
    public synchronized void imprimirArreglo(int[] arreglo, int tamano)
    {
    	System.out.print("Arreglo: ");
    	for(int i = 0; i < tamano; i++)
        {
    		System.out.print(arreglo[i] + ", ");
        }
    	System.out.println();
    }
    
    //Metodo que ejecuta la instruccion LW
    private void loadWord(int dir, int reg)
	{
		int palabraMem = convertirDireccionANumPalabra(dir);    //numero de palabra que se desea poner en registro 
		int bloqueMem = convertirDireccionANumBloque(dir);      //numero de bloque que se desea poner en cache
		int bloqueCach = convertirDireccionAPosicionCache(dir);      //numero de bloque en cache donde se va a subir
		Lock cache ;
		Lock direcP0 = new ReentrantLock();
		Lock direcP1 = new ReentrantLock();
		Lock busDatos = new ReentrantLock();
		Lock cacheRemota1 = new ReentrantLock();
		Lock cacheRemota2 = new ReentrantLock();
		Lock cacheRemota3 = new ReentrantLock();
		Procesador pro;
		if(this.nombreP==0)
		{
			if(this.nombre==0)
				cache=cacheRemota1;
			else
				cache=cacheRemota2;
		}else
			cache=cacheRemota3;
		boolean flagCache = cache.tryLock();
		boolean cargo = false;
		while (!cargo)
		{
			if (flagCache)
			{
				try
				{
					int bloqueVictMem = this.cacheDatos[5][bloqueCach];    
					int estado = this.cacheDatos[4][bloqueCach];
					if (bloqueVictMem == bloqueMem)
					{
						this.registro[reg] = this.cacheDatos[bloqueCach][palabraMem];
						cargo=true;
					} else
					{
						//El estado del bloque victima es modificado 
						if (estado == 2)
						{
							int dirVicFisica = bloqueVictMem * 4; //  dirVicFisica es la direccion donde comienza el bloque en la memoria compartida
							if (this.nombreP == 1)
							{
								dirVicFisica = dirVicFisica - 256;
							}
							//Siguiente if verifica condicion de que si el bloque victima es menor a 16 y el nucleo es del procesador 0
							//o el bloque victima es mayor a 16 y el nucleo es del procesador 1 este use el directorio de su procesador
							//de lo contrario use el directorio del otro procesador.
							if ((bloqueVictMem < 16 && this.nombreP == 0) || (bloqueVictMem > 16 && this.nombreP == 1))
							{
								if (this.nombreP == 1)
								{
									bloqueVictMem = bloqueVictMem - 16;
								}
								boolean flagDir = direcP0.tryLock();
								if (flagDir)
								{
									try
									{
										System.arraycopy(this.procesador.memDatos, dirVicFisica, this.cacheDatos[bloqueCach], 0, 4);
										this.procesador.directorio[bloqueVictMem][3] = 1; //Modifica en estado del bloque victima en el directorio correspondiente
										for (int i = 0; i < 16; i++)
										{ //Espera la cantidad de tic necesarios
											esperarAvanceTic();
										}
									} finally
									{
										direcP0.unlock(); //Libero directorio local 
									}
								}
							} else
							{
								boolean flagBus = busDatos.tryLock();
								if (flagBus)
								{
									try
									{
										if (this.nombreP == 1)
										{
											bloqueVictMem = bloqueVictMem - 16;
										}
										boolean flagDir = direcP1.tryLock();
										if (flagDir)
										{
											try
											{
												System.arraycopy(this.procesador.p.memDatos, dirVicFisica, this.cacheDatos[bloqueCach], 0, 4);
												this.procesador.p.directorio[bloqueVictMem][3] = 1; //Modifica en estado del bloque victima en el directorio correspondiente
												for (int i = 0; i < 40; i++)
												{ //Espera la cantidad de tic necesarios
													esperarAvanceTic();
												}
											} finally
											{
												direcP1.unlock(); //Libero directorio externo
											}
										}
									} finally
									{
										busDatos.unlock();
									}
								}
							}
						}
						//Pregunta para saber si el bloque a cargar pertenece a la memoria compartida a cargo del procesador 
						//a cual pertecese este nucleo
						if ((bloqueMem < 16 && this.nombreP == 0) || (bloqueMem > 16 && this.nombreP == 1))
						{
							pro=this.procesador;
						}else
						{
							pro=this.procesador.p;
						}
						if (this.nombreP == 1)
						{
							bloqueMem = bloqueMem - 16;
						}
						boolean flagDir = direcP0.tryLock();
						if (flagDir)
						{
							try
							{
								int estadoBloque = pro.directorio[bloqueMem][3];
								switch (estadoBloque)
								{
								case 0: //bloque no esta en ningun cache
									System.arraycopy(pro.memDatos, (bloqueMem * 4), this.cacheDatos[bloqueMem], 0, 4);
									break;
								case 1: //bloque esta en algun cache, pero no a sido modificado
									System.arraycopy(pro.memDatos, (bloqueMem * 4), this.cacheDatos[bloqueMem], 0, 4);
									break;
								case 2: //bloque esta en algun cache, pero a sido modificado
									boolean flagCacheRemota;
									Nucleo n;
									for(int i=0;i<3;i++)
										{
										if(pro.directorio[bloqueMem][i]==1)
										{
											switch(i){
											case 0:
												 flagCacheRemota=cacheRemota1.tryLock();
												 n=pro.nucleos[0];
												break;
											case 1:
												flagCacheRemota=cacheRemota2.tryLock();
												n=pro.nucleos[1];
												break;
											default:
												flagCacheRemota=cacheRemota3.tryLock();
												n=pro.nucleos[2];
												break;	
									
											}
											int dirFisica=bloqueMem * 4;
											if(this.nombreP==1){
												dirFisica=dirFisica-256;
											}
											if((bloqueMem<16&&this.nombreP==0)||(bloqueMem>=16&&this.nombreP==1)){
												System.arraycopy(n.cacheDatos[bloqueCach], 0, pro.memDatos, dirFisica , 4);
											}
										}
									}
									break;
								default:
									System.out.println("Error en tipo de estado");
									break;
								}
								this.cacheDatos[4][bloqueMem] = bloqueMem; //indico bloque de memoria al que pertenece
								this.cacheDatos[5][bloqueMem] = 1; //indico estdo del  bloque
								if (this.nombreP == 0) //actualizo el directorio
								{
									int tipo=(this.nombre == 0)?1:2;
									pro.directorio[bloqueMem][tipo] = 1;
								} else
								{
									pro.directorio[bloqueMem][3] = 1;
								}
								pro.directorio[bloqueMem][4] = 1;
								
								System.arraycopy(pro.memDatos, 0, this.cacheDatos[bloqueCach], bloqueMem * 4 , 4);
								pro.directorio[4][bloqueCach] = 1;
								pro.directorio[5][bloqueCach] = 1;
							} finally
							{
								direcP0.unlock();
							}
						}
				
					}
				} finally
				{
					cache.unlock();
				}
			}
			esperarAvanceTic();
		}
	}


    private void storeWord(int dir, int reg) {
    	int palabra = convertirDireccionANumPalabra(dir);
		int bloque = convertirDireccionANumBloque(dir);
		int bCache = convertirDireccionAPosicionCache(dir);
		Lock cache = new ReentrantLock();
		boolean flagCache = cache.tryLock();
		if (flagCache) 
		{
			try
			{
				
			}finally
			{
				cache.unlock();
			}
		}
    }

}
