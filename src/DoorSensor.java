/******************************************************************************************************************
* File:DoorSensor.java
* Course: Arquitectura de Software
* Project: Tarea 2
* Copyright: Copyright (c) 2013 Cimat A.C.
* Versions:
*	1.0 Mayo 2013 - 
*
* Description:
* Esta clase simula un sensor de una Puerta. escanea el event manager por eventos que cambien el estado de 
* la alarma, podra prender la alarma si esta esta apagada, para simular una Puerta rota. El actual estado es 
* posteado en el event manager
* 
* Parameters: dirreccion IP del event mnager (on    command line). si esta en blanco, asuminos que el event manager
* esta el la maquina local 
*
* Internal Methods:
*   void PostDoorState(EventManagerInterface ei, boolean state )
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import EventPackage.*;
import java.util.*;

class DoorSensor
{
		
	public static void main(String args[])
	{
		String EvtMgrIP;					// Event Manager IP address
		Event Evt = null;					// Event object
		EventQueue eq = null;				// Message Queue
		int EvtId = 0;						// User specified event ID
		EventManagerInterface em = null;	// Interface object to the event manager
		boolean doorState = false;			// estado del sensor de puerta
		boolean eventStatus = false;		// determinar si hay algun evento  
		int	Delay = 2500;					// The loop delay (2.5 seconds)
		boolean Done = false;				// Loop termination flag
		int max = 15000;					// maximo  tiempo que duerme el hilo 
		int min = 2501;						// minimo tiempo que duerme el hilo


		
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
			
			MessageWindow mw = new MessageWindow("Door Sensor", WinPosX, WinPosY);

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

			mw.WriteMessage("\nInitializing Door Simulation::" );
			mw.WriteMessage("   Initial Door state Set:: " + doorState );
			
			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			mw.WriteMessage("Beginning Simulation... ");


			while ( !Done )
			{
				// posteamos el estado actual del sensor de puerta

				PostDoorState( em, doorState );

				mw.WriteMessage("Current Door statey:: " + (doorState ? "BROKEN": "OK"));

				// Get the message queue

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
				 * observamos si hay Eventos con ID = - 6 esto significa que la alarma 
				 * de pueta a sido encendida/apagada.
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
							eventStatus = true;

						} // if

						if (Evt.GetMessage().equalsIgnoreCase("D0")) // Alarm off
						{
							doorState = false;
							eventStatus = true;

						} // if

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

					} // if

				} // for

				/*
				 * Ahora verificamos si el estado de la puerta es falso (OK) y lo cambiamos
				 * a estado true (BROKEN), para simular que una puerta esta rota.
				 */
				
				if (!doorState && !eventStatus) {
					doorState = true;
				}

				// Here we wait for a 2.5 seconds before we start the next sample
				try
				{
					
					Thread.sleep( Delay );
					

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Sleep error:: " + e );

				} // catch
				
				// aqui se determina si hay algun evento para crea
				// el tiempo que dormira el hilo aleatoreamente
				
				if (!eventStatus) {
					Delay = 2500;
				}else {
					Delay = (int)(Math.random()*(max-min))+min;			
					eventStatus = false;
				}

			} // while

		} else {

			System.out.println("Unable to register with the event manager.\n\n" );

		} // if

	} // main

	
	/***************************************************************************
	* CONCRETE METHOD:: PostHumidity
	* Purpose: This method posts the specified relative humidity value to the
	* specified event manager. This method assumes an event ID of 2.
	*
	* Arguments: EventManagerInterface ei - this is the eventmanger interface
	*			 where the event will be posted.
	*
	*			 float humidity - this is the humidity value.
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/

	static private void PostDoorState(EventManagerInterface ei, boolean state )
	{
		// Here we create the event.

		Event evt = new Event( (int) 6, String.valueOf(state) );

		// Here we send the event to the event manager.

		try
		{
			ei.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Posting Door state:: " + e );

		} // catch

	} // PostDoorState

} // Door Sensor