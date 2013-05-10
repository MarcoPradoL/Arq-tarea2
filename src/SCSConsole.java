
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
import TermioPackage.*;
import EventPackage.*;

public class SCSConsole
{
	public static void main(String args[])
	{
    	Termio UserInput = new Termio();	// Termio IO Object
		boolean Done = false;				// Main loop flag
		String Option = null;				// Menu choice from user
		Event Evt = null;					// Event object
		boolean Error = false;				// Error flag
		SCSMonitor Monitor = null;	// The environmental control system monitor
		boolean puertaRota = false;			// Estos parametros se utilizaran para mostrar
		boolean ventanaRota = false;		// si existe alguna alerta de seguridad  
		boolean movimiento = false;			//									
	
		/*float TempRangeHigh = (float)100.0;	// These parameters signify the temperature and humidity ranges in terms
		float TempRangeLow = (float)0.0;	// of high value and low values. The ECSmonitor will attempt to maintain
		float HumiRangeHigh = (float)100.0;	// this temperature and humidity. Temperatures are in degrees Fahrenheit
		float HumiRangeLow = (float)0.0;	// and humidity is in relative humidity percentage.
		*/
		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the event manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length != 0 )
 		{
			// event manager is not on the local system

			Monitor = new SCSMonitor( args[0] );

		} else {

			Monitor = new SCSMonitor();

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
				System.out.println( "Security Control System (SCS) Command Console: \n" );

				if (args.length != 0)
					System.out.println( "Using event manger at: " + args[0] + "\n" );
				else
					System.out.println( "Using local event manger \n" );
				System.out.println( "System  status:" + (Monitor.isSystemStatus() ? "Enable":"Disable") + "\n");
			//	System.out.println( "Set Temperature Range: " + TempRangeLow + "F - " + TempRangeHigh + "F" );
			//	System.out.println( "Set Humidity Range: " + HumiRangeLow + "% - " + HumiRangeHigh + "%\n" );
				System.out.println( "Select an Option: \n" );
				System.out.println( "1: Enable system" );
				System.out.println( "2: Disable System" );
				System.out.println( "3: Disable Door Alarm" );
				System.out.println( "4: Disable Window Alarm" );
				System.out.println( "5: Disable Motion Alarm" );
				System.out.println( "X: Stop System\n" );
				System.out.print( "\n>>>> " );
				Option = UserInput.KeyboardReadString();

				//////////// option 1 ////////////
				if ( Option.equals( "1" ) )
				{
					Monitor.setSystemStatus(true);

				}
				//////////// option 2 ////////////
				if ( Option.equals( "2" ) )
				{
					Monitor.setSystemStatus(false);

				}
				//////////// option 3 ////////////
				if ( Option.equals( "3" ) )
				{
					Monitor.Door(false);

				}
				//////////// option 4 ////////////
				if ( Option.equals( "4" ) )
				{
					Monitor.Window(false);

				}
				//////////// option 5 ////////////
				if ( Option.equals( "5" ) )
				{
					Monitor.Motion(false);

				}
			
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
					System.out.println( "\nConsole Stopped... Exit Security Monitor  to return to command prompt." );
					Monitor.Halt();

				} // if

			} // while

		} else {

			System.out.println("\n\nUnable start the monitor.\n\n" );

		} // if

  	} // main

} // ECSConsole
