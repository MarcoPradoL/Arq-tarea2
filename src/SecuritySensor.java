/******************************************************************************************************************
* File:WindowSensor.java
* Course: Arquitectura de Software
* Project: Tarea 2
* Copyright: Copyright (c) 2013 Cimat A.C.
* Versions:
*	1.0 Mayo 2013 - 
*
* Description:
* Esta clase simula un sensor de una ventana. escanea el event manager por eventos que cambien el estado de 
* la alarma, podra prender la alarma si esta esta apagada, para simular una ventana rota. El actual estado es 
* posteado en el event manager
* 
* Parameters: dirreccion IP del event mnager (on    command line). si esta en blanco, asuminos que el event manager
* esta el la maquina local 
*
* Internal Methods:
*   void PostWindowState(EventManagerInterface ei, boolean state )
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import TermioPackage.Termio;
import EventPackage.*;
import EventPackage.EventManagerInterface;

import java.util.*;

class SecuritySensor
{
	private static boolean windowState = false;			// estado del sensor de ventana
	private	static boolean doorState = false;			// estado del sensor de puerta
	private static boolean motionState = false;			// estado del sensor del detector de movimiento
	private static boolean fireState = false;			// estado del sensor del detector de incendios
		
	public static void main(String args[])
	{
		String EvtMgrIP;					// Event Manager IP address
		Event Evt = null;					// Event object
		EventQueue eq = null;				// Message Queue
		int EvtId = 0;						// User specified event ID
		EventManagerInterface em = null;	// Interface object to the event manager 
		int	Delay = 2500;					// The loop delay (2.5 seconds)
		boolean Done = false;				// Loop termination flag
		String mensaje= null;				// almacena la informacion de nombre y descripcion del dispositivo


		
		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the event manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length == 0 )
 		{
			// event manager is on the local system

			System.out.println("\n\nAttempting to register on the local machine..." );

			try
			{
				// Here we create an event manager interface object. This assumes
				// that the event manager is on the local machine

				em = new EventManagerInterface();
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating event manager interface: " + e);

			} // catch

		} else {

			// event manager is not on the local system

			EvtMgrIP = args[0];

			System.out.println("\n\nAttempting to register on the machine:: " + EvtMgrIP );

			try
			{
				// Here we create an event manager interface object. This assumes
				// that the event manager is NOT on the local machine

				em = new EventManagerInterface( EvtMgrIP );
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating event manager interface: " + e);

			} // catch

		} // if

		// Here we check to see if registration worked. If ef is null then the
		// event manager interface was not properly created.

		if (em != null)
		{

			// We create a message window. Note that we place this panel about 1/2 across 
			// and 2/3s down the screen
			
			float WinPosX = 0.0f; 	//This is the X position of the message window in terms 
									//of a percentage of the screen height
			float WinPosY = 0.25f;	//This is the Y position of the message window in terms 
								 	//of a percentage of the screen height 
			
			MessageWindow mw = new MessageWindow("Security Sensor", WinPosX, WinPosY);

			mw.WriteMessage("Registered with the event manager." );

	    	try
	    	{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
				mw.WriteMessage("Error:: " + e);

			} // catch

			mw.WriteMessage("\nInitializing Security Simulation::" );
			mw.WriteMessage("   Initial Window state Set:: " + windowState );
			mw.WriteMessage("   Initial Door state Set:: " + doorState );
			mw.WriteMessage("   Initial Motion state Set:: " + motionState );
			mw.WriteMessage("   Initial Fire state Set:: " + fireState );

			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			mw.WriteMessage("Beginning Simulation... ");

			eventSecurity();
			while ( !Done )
			{
				
				// colocamos el estado actual del sensor de seguridad
				heartbeat(em);
				PostState( em, doorState, 6);
				PostState( em, windowState, 7 );
				PostState( em, motionState, 8);
				PostState( em, fireState, 12);
				mw.WriteMessage("Current Door state:: " + (doorState ? "BROKEN": "OK") + 
						" Current Window state:: " + (windowState ? "BROKEN": "OK" ) + 
						" Current Motion state:: " + (motionState ? "DETECTED": "OK") + 
						" Current Fire state:: " + (fireState ? "DETECTED": "OK"));
				


				try
				{
					eq = em.GetEventQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting event queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for EventIDs = -4, this means the the humidify or
				// dehumidifier has been turned on/off. Note that we get all the messages
				// from the queue at once... there is a 2.5 second delay between samples,..
				// so the assumption is that there should only be a message at most.
				// If there are more, it is the last message that will effect the
				// output of the humidity as it would in reality.
				
				
				/*
				 * Si hay mensajes en la cola, los leemos
				 * observamos si hay Eventos con ID = - 7 esto significa que la alarma 
				 * de ventana a sido encendida/apagada.
				 */

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Evt = eq.GetEvent();
					
					if ( Evt.GetEventId() == -9 )
					{
						if (Evt.GetMessage().equalsIgnoreCase("D1")) // Alarm on
						{
							doorState = true;

						} // if

						if (Evt.GetMessage().equalsIgnoreCase("D0")) // Alarm off
						{
							doorState = false;

						} // if

					} // if
					
					if ( Evt.GetEventId() == -10 )
					{
						if (Evt.GetMessage().equalsIgnoreCase("W1")) // Alarm on
						{
							windowState = true;

						} // if
						
						if (Evt.GetMessage().equalsIgnoreCase("W0")) // Alarm off
						{
							windowState = false;

						} // if
					} // if
					
					if ( Evt.GetEventId() == -11 )
					{
						if (Evt.GetMessage().equalsIgnoreCase("M1")) // alarm on
						{
							motionState = true;
						} // if

						if (Evt.GetMessage().equalsIgnoreCase("M0")) // alarm off
						{
							motionState = false;
						} // if

					} // if
					
					if ( Evt.GetEventId() == -13 )
					{
						if (Evt.GetMessage().equalsIgnoreCase("F1")) // alarm on
						{
							fireState = true;
						} // if

						if (Evt.GetMessage().equalsIgnoreCase("F0")) // alarm off
						{
							fireState = false;
						} // if

					} // if
					
					if ( Evt.GetEventId() == 22 )
					{
						try {
							mensaje = String.valueOf(em.GetMyId())+ "---Nombre: Security Sensor---"+
									"Descripcion: Este sensor es utilizado para simular alarmas de Puerta rota "+
									"ventana rota, deteccion de movimiento y deteccion de fuego";
						} catch (Exception e) {
							mw.WriteMessage("Error:: " + e);
						}
						datosMantenimiento(em,mensaje);

					} // if
					// If the event ID == 100 then this is a signal that the simulation
					// is to end. At this point, the loop termination flag is set to
					// true and this process unregisters from the event manager.

					if ( Evt.GetEventId() == 100 )
					{
						Done = true;

						try
						{
							em.UnRegister();

				    	} // try

				    	catch (Exception e)
				    	{
							mw.WriteMessage("Error unregistering: " + e);

				    	} // catch

				    	mw.WriteMessage("\n\nSimulation Stopped. \n");
				    	System.out.println( "\nConsole Stopped... Exit Security Sensor to return to command prompt." );

					} // if

				} // for

				// Here we wait for a 2.5 seconds before we start the next sample
				try
				{
					Thread.sleep( Delay );

				} // try

				
				catch( Exception e )
				{
					mw.WriteMessage("Sleep error:: " + e );

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the event manager.\n\n" );

		} // if

	} // main

	private static void eventSecurity() {
				
		new Thread(new Runnable()
		{
			Termio UserInput = new Termio();	// Termio IO Object
			String Option = null;				// Menu choice from user
			boolean Done = false;				// simulation  termination flag
			@Override
			public void run()
			{
				while (!Done)
				{
					// Here, the main thread continues and provides the main menu
		
					System.out.println( "\n\n\n\n" );
					System.out.println( "Security Simulation Command Console: \n" );
		
					
					System.out.println( "Select an Option: \n" );
					System.out.println( "1: Door Security Event" );
					System.out.println( "2: Window Secutity Event" );
					System.out.println( "3: Motion Secutity Event" );
					System.out.println( "4: Fire Secutity Event" );
					System.out.print( "\n>>>> " );
					Option = UserInput.KeyboardReadString();
		
					//////////// option 1 ////////////
					if ( Option.equals( "1" ) )
					{
						doorState = true;
					}
					//////////// option 2 ////////////
					if ( Option.equals( "2" ) )
					{
						windowState = true;
					}
					//////////// option 3 ////////////
					if ( Option.equals( "3" ) )
					{
						motionState = true;
					}
					if ( Option.equals( "4" ) )
					{
						fireState = true;
					}
					
				} // while
			}
		}).start();
	}

	/***************************************************************************
	* CONCRETE METHOD:: PostState
	* Purpose: Este metodo manda el estado de la ventana para al Event manager
	* Este metodo asume que el ID del evento es 7 This method assumes an event ID of 2.
	*
	* Arguments: EventManagerInterface ei - this is the event manger interface
	*			 where the event will be posted.
	*
	*			 boolean state - es el estado del sensor
	*	
	*			 int eventID  - el id del evento que se va a generar.
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/

	static private void PostState(EventManagerInterface ei, boolean state, int eventId )
	{
		// Here we create the event.

		Event evt = new Event( (int) eventId, String.valueOf(state) );

		// Here we send the event to the event manager.

		try
		{
			ei.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Posting Security event:: " + e );

		} // catch

	} // PostWindowState

	private static void heartbeat(EventManagerInterface ei){
		// Here we send the event to the event manager.

		try
		{
			Event evt = new Event( (int) 20, String.valueOf(ei.GetMyId()) );
			ei.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Posting Heartbeat event:: " + e );

		} // catch
	}

	private static void datosMantenimiento(EventManagerInterface ei, String mensaje) {
		// Here we send the event to the event manager.

		try
		{
			Event evt = new Event( (int) 21, mensaje );
			ei.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Posting Heartbeat event:: " + e );

		} // catch
	}
} // Window Sensor