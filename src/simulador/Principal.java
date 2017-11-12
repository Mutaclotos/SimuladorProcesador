package simulador;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Principal /*extends threads*/ {

	public static int hilosTerminados; //Determina la cantidad de nucleos listos para terminar su terminacion
	public static int hilosListosParaTic;
	public static int reloj;
	public static int quantum; //El valor del quantum ingresado por el usuario
	public static int tipoModulacion; //El tipo de modo del correr dado por el usuario.	
	static Thread t;
	public static List<Integer> list;
	//private final boolean lock = new boolean();
	
	static StringBuilder lock= new StringBuilder();
    Procesador procesador;
    Controlador C;
    /**
     * Constructor for objects of class main
     */
    public Principal()
    {
    	hilosTerminados = 0;
        hilosListosParaTic = 0;
        reloj = 0;
        list = new ArrayList<>();
        interfaz();
        //Principal p=new Principal();
        System.out.println("Inicializando procesador 0:");
        Procesador P0 = new Procesador(2, 16, 384, 64, "p0.txt");
        
        System.out.println("Inicializando procesador 1:"); 
        Procesador P1 = new Procesador(1, 16, 256, 32, "p1.txt");

        avanzarReloj();
      }

     private  void avanzarReloj()
     {
    	  t = Thread.currentThread();
    	  //System.out.println("Status1: "+t.getState());
    	  //System.out.println("Current thread = "+t.getName());
    	 synchronized(lock){
    		 //System.out.println("hilosListosParaTic = "+hilosListosParaTic);
  	   while(hilosTerminados < 3)
  	      {
  		  /* try{
  			   System.out.println("hilosListosParaTic = "+hilosListosParaTic);
  			   list.wait();
  		   }
  		   catch(InterruptedException e) 
           {
  			   e.printStackTrace();
           }*/if(hilosListosParaTic==3){
        	   reloj++;
    			 hilosListosParaTic = 0;
    			 System.out.println("Hacer tic");  
           }
  			 
  	    	  
  	      } 
    	 }
     }
     public void imprimirResultador()
     {
  	   
     }
     
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

