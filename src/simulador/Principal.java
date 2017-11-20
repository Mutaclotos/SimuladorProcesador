package simulador;

import java.util.Scanner;

public class Principal extends Thread 
{

	public static int hilosTerminados; //Determina la cantidad de nucleos listos para terminar su terminacion
	public static int hilosListosParaTic;
	public static int reloj;
	public static int quantum; //El valor del quantum ingresado por el usuario
	public static int tipoModulacion; //El tipo de modo del correr dado por el usuario.	
	static Thread t;
	public static Object syncPrincipal = new Object();
	Procesador P0;
	Procesador P1;
	
    Procesador procesador;

    /**
     * Constructor for objects of class Principal
     */
    public Principal()
    {
    	hilosTerminados = 0;
        hilosListosParaTic = 0;
        reloj = 0;
        interfaz();
        
        System.out.println("Inicializando procesador 0:");
        P0 = new Procesador(0, 16, 384, 64, "p0.txt");
        
        System.out.println("Inicializando procesador 1:"); 
        P1 = new Procesador(1, 16, 256, 32, "p1.txt");
        P0.p = P1;
        P1.p = P0;        
        //Se inicializan los tres hilos de nucleo, dos para P0 y uno para P1
        Nucleo nucleo0 = new Nucleo(0, P0)
    	{
    	    public void run()
    	    {
    	    	simularNucleo();
    	      //System.out.println("Nucleo de procesador");
    	    }
    	};
    	
    	Nucleo nucleo1 = new Nucleo(1, P0)
    	{
    	    public void run()
    	    {
    	    	simularNucleo();
    	      //System.out.println("Nucleo de procesador");
    	    }
    	};
    	
    	Nucleo nucleo2 = new Nucleo(0, P1)
    	{
    	    public void run()
    	    {
    	    	simularNucleo();
    	      //System.out.println("Nucleo de procesador");
    	    }
    	};
    	P0.nucleos[0] = nucleo0;
    	P0.nucleos[1] = nucleo1;
    	P0.nucleos[2] = nucleo2;
    	P1.nucleos[0] = nucleo0;
    	P1.nucleos[1] = nucleo1;
    	P1.nucleos[2] = nucleo2;
    	nucleo0.start();
    	nucleo1.start();
    	nucleo2.start();
      }

    //Metodo que avanza el reloj de los procesadores. El hilo principal se mantiene en espera hasta que los tres hilos de nucleo estan listos para avanzar el tic
     protected void avanzarReloj()
     {
    	 Scanner in = new Scanner(System.in);
    		 //System.out.println("hilosListosParaTic = "+hilosListosParaTic);
         while(hilosTerminados < 3) //La simulacion continua hasta que todos los hilos esten listos para ser terminados
  	      {
	  		  
  			  synchronized(syncPrincipal)
  			  {
	  			  if(hilosListosParaTic < 3) //Si no todos los hilos estan listos para avanzar el tic, el hilo principal espera
		          {
		  		      try
		  	  		  {
			  				//System.out.println("Hilo principal esperando nucleos...");
			  				syncPrincipal.wait();
		  	  		  }
		  	  		  catch(InterruptedException e) 
		  	          {
		  	  			   e.printStackTrace();
		  	          }   
		  		  }
		      }
  			  
  			   
    		  synchronized(syncPrincipal)
  			  {
    			//Si la ejecucion de la simulacion es manual, se debe oprimir una tecla para avanzar el reloj
    			  //System.out.println(tipoModulacion); 
        		  if(tipoModulacion == 2)
        		  {
        			  if(in.hasNextInt())
        			  {
        				  int num = in.nextInt();
        			  }
        		  }
        		  
    			  reloj++; //Se avanza el reloj
        		  System.out.println("Tick de reloj: " + reloj); 
        		  
        		  
	  			  if(hilosListosParaTic == 0)
		          {
	  				  syncPrincipal.notify(); //Se notifica a uno de los hilos de nucleo que el reloj fue avanzado
		  		  }
		      }
  	      } 
         imprimirResultados();
     }
     
     public void imprimirResultados()
     {
  	     P0.imprimirMatrizContextos();
  	     P1.imprimirMatrizContextos();
     }
     
     //Metodo que imprime la interfaz de usuario inicial para permitirle ingresar los valores de entrada
     public void interfaz()
     {
  	   String input=" ";
  	   boolean entradaValida=false;
  	   int modulo;
  	   while(false==entradaValida)
  	   {
  		   System.out.print("Inserte el valor del quantum: "); 
  		   Scanner in = new Scanner(System.in);
  		   //input = in.nextInt();
  		   try {
  			   if(in.hasNextInt())
  			   {
  				   quantum = in.nextInt();
  				   if(quantum>0)
  				   {
  					   entradaValida=true;  
  				   }else
  				   {
  					   System.out.println("El quantum: " + quantum + " no es valido(debe ser mayor a 0).");
  				   } 
  			   }else
  			   {
  				   System.out.println("La entrada no es valida.");
  				   }
  			  } catch (IllegalStateException u) {
  				  System.out.println("No se ha ingresado ningun valor.");
  			  }   
  	   }
  	   entradaValida=false;
  	   while(false==entradaValida)
  	   {
  		   System.out.println("De las modulacones: ");
  		   System.out.println("1- Automatica.");
  		   System.out.println("2- Manual.");
  		   System.out.println("Seleccione cual desea: ");
  		   Scanner in = new Scanner(System.in);
  		   
  		   try {
  			   if(in.hasNextInt())
  			   {
  				   modulo = in.nextInt();
  				   if(modulo==1||modulo==2)
  				   {
  					   tipoModulacion=modulo;
  					   entradaValida=true;  
  				   }else
  				   {
  					   System.out.println("El tipo de modulo: " + modulo + " no es valido(debe ser 1 o 2).");
  				   } 
  			   }else
  			   {
  				   System.out.println("La entrada no es valida.");
  				   }
  			  } catch (IllegalStateException u) {
  				  System.out.println("No se ha ingresado ningun valor.");
  			  }   
  	   }
     }
  }

