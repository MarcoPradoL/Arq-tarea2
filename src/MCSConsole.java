/******************************************************************************************************************
* File:ECSConsole.java
* Course: 17655
* Project: Assignment 3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 February 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description: This class is the console for the museum environmental control system. This process consists of two
* threads. The ECSMonitor object is a thread that is started that is responsible for the monitoring and control of
* the museum environmental systems. The main thread provides a text interface for the user to change the temperature
* and humidity ranges, as well as shut down the system.
*
* Parameters: None
*
* Internal Methods: None
*
******************************************************************************************************************/
import java.util.HashMap;
import java.util.Map.Entry;

import TermioPackage.*;
import EventPackage.*;

public class MCSConsole
{
	public static void main(String args[])
	{
    	Termio UserInput = new Termio();	// Termio IO Object
		boolean Done = false;				// Main loop flag
		String Option = null;				// Menu choice from user
		Event Evt = null;					// Event object
		MCSMonitor Monitor = null;			// The Maintenance control system monitor
		HashMap<String, String []> dispositivos;	//hashmap utilizado para almacenar los dispositivos conectados
		String [] datos;						// arreglo para almacenar los datos de id de participante, nombre y descripcion
		int i=0;									// condador para enumerar los dispositivos


		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the event manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length != 0 )
 		{
			// event manager is not on the local system

			Monitor = new MCSMonitor( args[0] );

		} else {

			Monitor = new MCSMonitor();

		} // if


		// Here we check to see if registration worked. If ef is null then the
		// event manager interface was not properly created.

		if (Monitor.IsRegistered() )
		{
			Monitor.start(); // Here we start the monitoring and control thread

			while (!Done)
			{
				// Here, the main thread continues and provides the main menu

				System.out.println( "\n\n\n\n" );
				System.out.println( "Maintenance Control System (ECS) Command Console: \n" );

				if (args.length != 0)
					System.out.println( "Using event manger at: " + args[0] + "\n" );
				else
					System.out.println( "Using local event manger \n" );

				System.out.println( "Select an Option: \n" );
				System.out.println( "1: Show installed devices" );
				System.out.println( "X: Stop System\n" );
				System.out.print( "\n>>>> " );
				Option = UserInput.KeyboardReadString();

				//////////// option 1 ////////////

				if ( Option.equals( "1" ) )
				{
					i = 1;
					System.out.println( "\n\n" );
					
					dispositivos = Monitor.getDispositivos();
					
					for(Entry<String, String[]> entry : dispositivos.entrySet())
					{
						datos = entry.getValue();
						System.out.println(i+".-  "+datos[1]);
						System.out.println(datos[2]);
						System.out.println();
						i++;
					}

				} // if

				//////////// option 2 ////////////

				

				//////////// option X ////////////

				if ( Option.equalsIgnoreCase( "X" ) )
				{
					// Here the user is done, so we set the Done flag and halt
					// the environmental control system. The monitor provides a method
					// to do this. Its important to have processes release their queues
					// with the event manager. If these queues are not released these
					// become dead queues and they collect events and will eventually
					// cause problems for the event manager.

					Monitor.Halt();
					Done = true;
					System.out.println( "\nConsole Stopped... Exit monitor mindow to return to command prompt." );
					Monitor.Halt();

				} // if

			} // while

		} else {

			System.out.println("\n\nUnable start the monitor.\n\n" );

		} // if

  	} // main

} // ECSConsole
