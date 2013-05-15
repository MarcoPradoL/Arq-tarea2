
/******************************************************************************************************************
* File:ECSMonitor.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class monitors the environmental control systems that control museum temperature and humidity. In addition to
* monitoring the temperature and humidity, the ECSMonitor also allows a user to set the humidity and temperature
* ranges to be maintained. If temperatures exceed those limits over/under alarm indicators are triggered.
*
* Parameters: IP address of the event manager (on command line). If blank, it is assumed that the event manager is
* on the local machine.
*
* Internal Methods:
*	static private void Heater(EventManagerInterface ei, boolean ON )
*	static private void Chiller(EventManagerInterface ei, boolean ON )
*	static private void Humidifier(EventManagerInterface ei, boolean ON )
*	static private void Dehumidifier(EventManagerInterface ei, boolean ON )
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import TermioPackage.Termio;
import EventPackage.*;
import java.util.*;

import javax.swing.JOptionPane;

class SCSMonitor extends Thread
{
	private EventManagerInterface em = null;// Interface object to the event manager
	private String EvtMgrIP = null;			// Event Manager IP address
	boolean Registered = true;				// Signifies that this class is registered with an event manager.
	boolean systemStatus = true;	// indica si el sistema esta habilitado.
	MessageWindow mw = null;				// This is the message window
	Indicator	pri;						// Indicador de Puerta rota
	Indicator	vri;						// Indicador de Ventana rota
	Indicator	dmi;						// indicador de Deteccion de movimiento
	Indicator	dfi;						// indicador de Deteccion de fuego
	boolean ON = true;				// Used to turn on sprinkler
	boolean OFF = false;				// Used to turn off sprinkler
	public SCSMonitor()
	{
		// event manager is on the local system

		try
		{
			// Here we create an event manager interface object. This assumes
			// that the event manager is on the local machine

			em = new EventManagerInterface();

		}

		catch (Exception e)
		{
			System.out.println("SCSMonitor::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch

	} //Constructor

	public SCSMonitor( String EvmIpAddress )
	{
		// event manager is not on the local system

		EvtMgrIP = EvmIpAddress;

		try
		{
			// Here we create an event manager interface object. This assumes
			// that the event manager is NOT on the local machine

			em = new EventManagerInterface( EvtMgrIP );
		}

		catch (Exception e)
		{
			System.out.println("SCSMonitor::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public void run()
	{
		Event Evt = null;				// Event object
		EventQueue eq = null;			// Message Queue
		int EvtId = 0;					// User specified event ID
		boolean statusPuertaRota = false;	// actual estado del sensor de Puertas rotas.
		boolean statusVentanaRota = false; 	// actual estado del sensor de ventanas rotas
		boolean statusMovimiento = false; 	// actual estado del sensor de Deteccion de movimiento 
		boolean statusFuego = false; 	// actual estado del sensor de Deteccion de fuego 
		int	Delay = 1000;				// The loop delay (1 second)
		boolean Done = false;			// Loop termination flag
		boolean ON = true;				// Used to turn on sprinkler


		if (em != null)
		{
			// Now we create the ECS status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status 
			// indicators are placed directly to the right, one on top of the other

			mw = new MessageWindow("SCS Monitoring Security Console", 0, 0);
			pri = new Indicator("DOOR UNK",mw.GetX()+ mw.Width(), 0);
			vri = new Indicator("WINDOW UNK", mw.GetX()+ mw.Width(), (int)(mw.Height()/2), 2);
			dmi =  new Indicator("MOTION UNK", mw.GetX()+ mw.Width() + pri.Width()+(pri.Width()/2), 0 , 2);
			dfi =  new Indicator("FIRE UNK", mw.GetX()+ mw.Width() + vri.Width()+(vri.Width()/2), 0 , 2);
		
			mw.WriteMessage( "Registered with the event manager." );

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
				// Here we get our event queue from the event manager

				try
				{
					eq = em.GetEventQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting event queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for EventIDs = 1 or 2. Event IDs of 1 are temperature
				// readings from the temperature sensor; event IDs of 2 are humidity sensor
				// readings. Note that we get all the messages at once... there is a 1
				// second delay between samples,.. so the assumption is that there should
				// only be a message at most. If there are more, it is the last message
				// that will effect the status of the temperature and humidity controllers
				// as it would in reality.

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Evt = eq.GetEvent();

					if ( Evt.GetEventId() == 6 ) // leyendo dato de puerta rota
					{
						try
						{
							statusPuertaRota =  Boolean.parseBoolean(Evt.GetMessage());
 
						} // try

						catch( Exception e )
						{
							mw.WriteMessage("Error reading Door: " + e);

						} // catch

					} // if

					if ( Evt.GetEventId() == 7 ) // leyendo dato de Ventana rota
					{
						try
						{
				
							statusVentanaRota = Boolean.parseBoolean(Evt.GetMessage());

						} // try

						catch( Exception e )
						{
							mw.WriteMessage("Error reading Window: " + e);

						} // catch

					} // if
					
					if ( Evt.GetEventId() == 8 ) // leyendo dato de deteccion de movimiento
					{
						try
						{
				
							statusMovimiento = Boolean.parseBoolean(Evt.GetMessage());

						} // try

						catch( Exception e )
						{
							mw.WriteMessage("Error reading Motion: " + e);

						} // catch

					} // if
					
					if ( Evt.GetEventId() == 12 ) // leyendo dato de deteccion de fuego
					{
						try
						{
				
							statusFuego = Boolean.parseBoolean(Evt.GetMessage());

						} // try

						catch( Exception e )
						{
							mw.WriteMessage("Error reading Fire: " + e);

						} // catch

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

						// Get rid of the indicators. The message panel is left for the
						// user to exit so they can see the last message posted.

						pri.dispose();
						vri.dispose();
						dmi.dispose();
						dfi.dispose();

					} // if

				} // for
				
				if (systemStatus) {
					mw.WriteMessage("DOOR:: " + (statusPuertaRota ? "BROKEN": "OK") + " WINDOW:: " + 
							(statusVentanaRota ? "BROKEN": "OK") + " MOTION:: " + 
							(statusMovimiento ? "DETECTED": "OK") + " FIRE:: " + 
									(statusFuego ? "DETECTED": "OK"));
					
					// checar las alarmas y su estatus
					if (statusPuertaRota) {
						pri.SetLampColorAndMessage("DOOR BROKEN", 3);
					}else {
						pri.SetLampColorAndMessage("DOOR OK", 1);
					}
					if (statusVentanaRota) {
						vri.SetLampColorAndMessage("WINDOW BROKEN", 3);
					}else {
						vri.SetLampColorAndMessage("WINDOW OK", 1);
					}
					if (statusMovimiento) {
						dmi.SetLampColorAndMessage("MOTION DETECTED", 2);
					}else {
						dmi.SetLampColorAndMessage("MOTION OK", 1);
					}
					if (statusFuego) {
						dfi.SetLampColorAndMessage("FIRE DETECTED", 2);
					}else {
						dfi.SetLampColorAndMessage("FIRE OK", 1);
					}
					
					// checamos si  se ha detectado fuego para encender los rociadores
					
					if (statusFuego) {
						Sprinkler(ON);
					}
				}else {
					mw.WriteMessage("DOOR:: UNK WINDOW:: UNK MOTION:: UNK FIRE:: UNK");
					pri.SetLampColorAndMessage("DOOR UNK", 0);
					vri.SetLampColorAndMessage("WINDOW UNK", 0);
					dmi.SetLampColorAndMessage("MOTION UNK", 0);
					dfi.SetLampColorAndMessage("FIRE UNK", 0);
				}
				
				// This delay slows down the sample rate to Delay milliseconds
				


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
	* CONCRETE METHOD:: IsRegistered
	* Purpose: This method returns the registered status
	*
	* Arguments: none
	*
	* Returns: boolean true if registered, false if not registered
	*
	* Exceptions: None
	*
	***************************************************************************/

	public boolean IsRegistered()
	{
		return( Registered );

	} 

	
	/***************************************************************************
	* CONCRETE METHOD:: Halt
	* Purpose: This method posts an event that stops the environmental control
	*		   system.
	*
	* Arguments: none
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/

	public void Halt()
	{
		mw.WriteMessage( "***HALT MESSAGE RECEIVED - SHUTTING DOWN SYSTEM***" );

		// Here we create the stop event.

		Event evt;

		evt = new Event( (int) 100, "XXX" );

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending halt message:: " + e);

		} // catch

	} // Halt

	public boolean isSystemStatus() {
		return systemStatus;
	}

	public void setSystemStatus(boolean systemStatus) {
		this.systemStatus = systemStatus;
	}

	/***************************************************************************
	* CONCRETE METHOD:: Door
	* Purpose: This method posts events that will signal the Security
	*		   controller to turn on/off the door alarm
	*
	* Arguments: boolean status ON(true)/OFF(false) - indicates whether to turn the
	*			 door alarm  on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/

	public void Door( boolean status )
	{
		// Here we create the event.

		Event evt;

		if ( status )
		{
			evt = new Event( (int) 9, "D1" );

		} else {

			evt = new Event( (int) 9, "D0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending Door control message:: " + e);

		} // catch

	} // Door

	/***************************************************************************
	* CONCRETE METHOD:: Window
	* Purpose: This method posts events that will signal the Security
	*		   controller to turn on/off the window alarm
	*
	* Arguments: boolean status ON(true)/OFF(false) - indicates whether to turn the
	*			 window alarm  on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/

	public void Window( boolean status )
	{
		// Here we create the event.

		Event evt;

		if ( status )
		{
			evt = new Event( (int) 10, "W1" );

		} else {

			evt = new Event( (int) 10, "W0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending Window control message:: " + e);

		} // catch

	} // Window

	/***************************************************************************
	* CONCRETE METHOD:: Motion
	* Purpose: This method posts events that will signal the Security
	*		   controller to turn on/off the motion alarm
	*
	* Arguments: boolean status ON(true)/OFF(false) - indicates whether to turn the
	*			 motion alarm  on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/

	public void Motion( boolean status )
	{
		// Here we create the event.

		Event evt;

		if ( status )
		{
			evt = new Event( (int) 11, "M1" );

		} else {

			evt = new Event( (int) 11, "M0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending Motion control message::  " + e);

		} // catch

	} // Motion
	
	public void Sprinkler( boolean status )
	{
		// Here we create the event.

		Event evt;

		if ( status )
		{
			evt = new Event( (int) 14, "S1" );

		} else {

			evt = new Event( (int) 14, "S0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending sprinkler control message::  " + e);

		} // catch

	} // Sprinkler
	
	public void Fire( boolean status )
	{
		// Here we create the event.

		Event evt;

		if ( status )
		{
			evt = new Event( (int) 13, "F1" );

		} else {

			evt = new Event( (int) 13, "F0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending Fire control message::  " + e);

		} // catch

	} // Motion

}// ECSMonitor