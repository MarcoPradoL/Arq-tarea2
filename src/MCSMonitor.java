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
import EventPackage.*;

import java.util.*;
import java.util.Map.Entry;

class MCSMonitor extends Thread
{
	private EventManagerInterface em = null;// Interface object to the event manager
	private String EvtMgrIP = null;			// Event Manager IP address
	private float TempRangeHigh = 100;		// These parameters signify the temperature and humidity ranges in terms
	private float TempRangeLow = 0;			// of high value and low values. The ECSmonitor will attempt to maintain
	private float HumiRangeHigh = 100;		// this temperature and humidity. Temperatures are in degrees Fahrenheit
	private float HumiRangeLow = 0;			// and humidity is in relative humidity percentage.
	boolean Registered = true;				// Signifies that this class is registered with an event manager.
	private MessageWindow mw = null;				// This is the message window
	private Indicator ti;							// Temperature indicator
	private Indicator hi;							// Humidity indicator
	private String [] datos;						// arreglo para almacenar los datos de id de participante, nombre y descripcion
	private HashMap<String, String []> dispositivos= null;	//hashmap utilizado para almacenar los dispositivos conectados
	private HashMap<String, Boolean>  heartbeat= null ;		//hashmap utiliazdo para almacenar los latidos de los dispositivos
	private String mensaje= null;				// almacena la informacion de nombre y descripcion del dispositivo

	public MCSMonitor()
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
			System.out.println("MCSMonitor::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch

	} //Constructor

	public MCSMonitor( String EvmIpAddress )
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
			System.out.println("MCSMonitor::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public void run()
	{
		Event Evt = null;				// Event object
		EventQueue eq = null;			// Message Queue
		int EvtId = 0;					// User specified event ID
		float CurrentTemperature = 0;	// Current temperature as reported by the temperature sensor
		float CurrentHumidity= 0;		// Current relative humidity as reported by the humidity sensor
		int	Delay = 2500;				// The loop delay (1 second)
		boolean Done = false;			// Loop termination flag
		boolean ON = true;				// Used to turn on heaters, chillers, humidifiers, and dehumidifiers
		boolean OFF = false;			// Used to turn off heaters, chillers, humidifiers, and dehumidifiers

		if (em != null)
		{
			// Now we create the ECS status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status 
			// indicators are placed directly to the right, one on top of the other
			dispositivos = new HashMap<String, String []>();
			heartbeat = new HashMap<String,Boolean>();
			mw = new MessageWindow("MCS Monitoring Console", 0, 0);
			//ti = new Indicator ("TEMP UNK", mw.GetX()+ mw.Width(), 0);
			//hi = new Indicator ("HUMI UNK", mw.GetX()+ mw.Width(), (int)(mw.Height()/2), 2 );

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
				
				heartbeat(em);
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

					if ( Evt.GetEventId() == 20) // Heartbeat reading
					{
						if (dispositivos.containsKey(Evt.GetMessage())) {
							heartbeat.put(Evt.GetMessage(), true);
						}else{
							pedirInformacion();
						}
					} // if

					if ( Evt.GetEventId() == 21 ) // Nombre y descripcion de dispositivos reading
					{

				
							datos = Evt.GetMessage().split("---");
							
							if (!dispositivos.containsKey(datos[0])) {
								dispositivos.put(datos[0], datos);
								//heartbeat.put(Evt.GetMessage(), true);
							}
							


					} // if
					
					if ( Evt.GetEventId() == 22 )
					{
						try {
							mensaje = String.valueOf(em.GetMyId())+ "---Nombre: Maintenance Monitor---"+
									"Descripcion: Monitor donde sera reportado cuanquier dispositivo que no responda ";
						} catch (Exception e) {
							mw.WriteMessage("Error:: " + e);
						}
						datosMantenimiento(em,mensaje);

					}

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

						//hi.dispose();
						//ti.dispose();

					} // if

				} // for
				if (dispositivos != null) {
					for(Entry<String, String[]> entry : dispositivos.entrySet())
					{
						if (!heartbeat.containsKey(entry.getKey())) {
							
							datos =entry.getValue();
							mw.WriteMessage(datos[1]+" no responde");
						}
					}
					
				} 
				
				heartbeat.clear();

				
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

	} // SetTemperatureRange

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


	/***************************************************************************
	* CONCRETE METHOD:: Deumidifier
	* Purpose: This method posts events that will signal the humidity
	*		   controller to turn on/off the dehumidifier
	*
	* Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	*			 dehumidifier on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/

	private void pedirInformacion()
	{
		// Here we create the event.

		Event evt;


			evt = new Event( (int) 22, "xxx" );

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending dehumidifier control message::  " + e);

		} // catch

	} // Dehumidifier

	/**
	 * @return the dispositivos
	 */
	public HashMap<String, String[]> getDispositivos() {
		return dispositivos;
	}
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



	
} // MCSMonitor