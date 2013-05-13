/******************************************************************************************************************
* File:HumidityController.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class simulates a device that controls a humidifier and dehumidifier. It polls the event manager for event
* ids = 4 and reacts to them by turning on or off the humidifier/dehumidifier. The following command are valid
* strings for controlling the humidifier and dehumidifier:
*
*	H1 = humidifier on
*	H0 = humidifier off
*	D1 = dehumidifier on
*	D0 = dehumidifier off
*
* The state (on/off) is graphically displayed on the terminal in the indicator. Command messages are displayed in
* the message window. Once a valid command is recieved a confirmation event is sent with the id of -5 and the command in
* the command string.
*
* Parameters: IP address of the event manager (on command line). If blank, it is assumed that the event manager is
* on the local machine.
*
* Internal Methods:
*	static private void ConfirmMessage(EventManagerInterface ei, String m )
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import EventPackage.*;

import java.util.*;

class SecurityController
{
	public static void main(String args[])
	{
		String EvtMgrIP;					// Event Manager IP address
		Event Evt = null;					// Event object
		EventQueue eq = null;				// Message Queue
		int EvtId = 0;						// User specified event ID
		EventManagerInterface em = null;	// Interface object to the event manager
		boolean puertaRota = false;			// Estos parametros se utilizaran para permitir apagar 
		boolean ventanaRota = false;		// las alarmas de seguridad 
		boolean movimiento = false;			//									
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

		// Here we check to see if registration worked. If em is null then the
		// event manager interface was not properly created.

		if (em != null)
		{
			System.out.println("Registered with the event manager." );

			/* Now we create the humidity control status and message panel
			** We put this panel about 2/3s the way down the terminal, aligned to the left
			** of the terminal. The status indicators are placed directly under this panel
			*/

			float WinPosX = 0.5f; 	//This is the X position of the message window in terms 
									//of a percentage of the screen height
			float WinPosY = 0.25f;	//This is the Y position of the message window in terms 
								 	//of a percentage of the screen height 
			
			MessageWindow mw = new MessageWindow("Security Controller Status Console", WinPosX, WinPosY);

			/*// Now we put the indicators directly under the humitity status and control panel
						
			Indicator hi = new Indicator ("Humid OFF", mw.GetX(), mw.GetY()+mw.Height());
			Indicator di = new Indicator ("DeHumid OFF", mw.GetX()+(hi.Width()*2), mw.GetY()+mw.Height());
			*/
			mw.WriteMessage("Registered with the event manager." );

	    	try
	    	{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
				System.out.println("Error:: " + e);

			} // catch

			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			while ( !Done )
			{
				
				heartbeat(em);
				try
				{
					eq = em.GetEventQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting event queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for EventIDs = 4, this is a request to turn the
				// humidifier or dehumidifier on/off. Note that we get all the messages
				// at once... there is a 2.5 second delay between samples,.. so
				// the assumption is that there should only be a message at most.
				// If there are more, it is the last message that will effect the
				// output of the humidity as it would in reality.

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Evt = eq.GetEvent();

					if ( Evt.GetEventId() == 9 )	//door event
					{
						if (Evt.GetMessage().equalsIgnoreCase("D1")) // alarm on
						{
							puertaRota = true;
							mw.WriteMessage("Received Door broken event" );

							// Confirm that the message was recieved and acted on

							ConfirmMessage( em, "D1", -9 );

						} // if

						if (Evt.GetMessage().equalsIgnoreCase("D0")) // alarm off
						{
							puertaRota = false;
							mw.WriteMessage("Received Door ok event" );

							// Confirm that the message was recieved and acted on

							ConfirmMessage( em, "D0" , -9);

						} // if

					} // if
					
					if ( Evt.GetEventId() == 10 )	//window event
					{
						if (Evt.GetMessage().equalsIgnoreCase("W1")) // alarm on
						{
							ventanaRota = true;
							mw.WriteMessage("Received Window broken event" );

							// Confirm that the message was recieved and acted on

							ConfirmMessage( em, "W1" , -10);

						} // if

						if (Evt.GetMessage().equalsIgnoreCase("W0")) // alarm off
						{
							ventanaRota = false;
							mw.WriteMessage("Received Window ok event" );

							// Confirm that the message was recieved and acted on

							ConfirmMessage( em, "W0", -10 );

						} // if

					} // if
					
					if ( Evt.GetEventId() == 11 )	//Motion event
					{
						if (Evt.GetMessage().equalsIgnoreCase("M1")) // alarm on
						{
							movimiento = true;
							mw.WriteMessage("Received Motion detected event" );

							// Confirm that the message was recieved and acted on

							ConfirmMessage( em, "M1" , -11);

						} // if

						if (Evt.GetMessage().equalsIgnoreCase("M0")) // alarm off
						{
							movimiento = false;
							mw.WriteMessage("Received Motion ok event" );

							// Confirm that the message was recieved and acted on

							ConfirmMessage( em, "M0" , -11 );

						} // if

					} // if
					
					if ( Evt.GetEventId() == 22 )
					{
						try {
							mensaje = String.valueOf(em.GetMyId())+ "---Nombre: Security Controller---"+
									"Descripcion: Este Controlador es utilizado para cambiar el estado del sensor de Puerta rota "+
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

				    	mw.WriteMessage( "\n\nSimulation Stopped. \n");

					} // if

				} // for
				
				try
				{
					Thread.sleep( Delay );

				} // try

				catch( Exception e )
				{
					System.out.println( "Sleep error:: " + e );

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the event manager.\n\n" );

		} // if

	} // main

	/***************************************************************************
	* CONCRETE METHOD:: ConfirmMessage
	* Purpose: This method posts the specified message to the specified event
	* manager. This method assumes an event ID of -4 which indicates a confirma-
	* tion of a command.
	*
	* Arguments: EventManagerInterface ei - this is the eventmanger interface
	*			 where the event will be posted.
	*
	*			 string m - this is the received command.
	*			 int id -	es el id que generara el evento 
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/

	static private void ConfirmMessage(EventManagerInterface ei, String m, int id )
	{
		// Here we create the event.

		Event evt = new Event( (int) id, m );

		// Here we send the event to the event manager.

		try
		{
			ei.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error Confirming Message:: " + e);

		} // catch

	} // ConfirmMessage
	
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

} // SecurityControllers