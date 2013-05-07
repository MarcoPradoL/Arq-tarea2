
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

class SCSMonitor extends Thread
{
	private EventManagerInterface em = null;// Interface object to the event manager
	private String EvtMgrIP = null;			// Event Manager IP address
	boolean puertaRota = false;				// Estos parametros se utilizaran para mostrar
	boolean ventanaRota = false;			// si existe alguna alerta de seguridad  
	boolean movimiento = false;				//									
	/*private float TempRangeHigh = 100;	// These parameters signify the temperature and humidity ranges in terms
	private float TempRangeLow = 0;			// of high value and low values. The ECSmonitor will attempt to maintain
	private float HumiRangeHigh = 100;		// this temperature and humidity. Temperatures are in degrees Fahrenheit
	private float HumiRangeLow = 0;			// and humidity is in relative humidity percentage.
	*/
	boolean Registered = true;				// Signifies that this class is registered with an event manager.
	boolean systemStatus = true;	// indica si el sistema esta habilitado.
	MessageWindow mw = null;				// This is the message window
	Indicator	pri;						// Indicador de Puerta rota
	Indicator	vri;						// Indicador de Ventana rota
	Indicator	dmi;						// indicador de Deteccion de movimiento
	/*Indicator ti;							// Temperature indicator
	Indicator hi;							// Humidity indicator
	*/

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
		/*float CurrentTemperature = 0;	// Current temperature as reported by the temperature sensor
		float CurrentHumidity= 0;		// Current relative humidity as reported by the humidity sensor
		*/
		int	Delay = 1000;				// The loop delay (1 second)
		boolean Done = false;			// Loop termination flag
		//boolean ON = true;				// Used to turn on heaters, chillers, humidifiers, and dehumidifiers
		//boolean OFF = false;			// Used to turn off heaters, chillers, humidifiers, and dehumidifiers

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
			/*ti = new Indicator ("TEMP UNK", mw.GetX()+ mw.Width(), 0);
			hi = new Indicator ("HUMI UNK", mw.GetX()+ mw.Width(), (int)(mw.Height()/2), 2 );
			 */
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

					} // if

				} // for
				if (systemStatus) {
					mw.WriteMessage("DOOR:: " + (statusPuertaRota ? "BROKEN": "OK") + " WINDOW:: " + 
							(statusVentanaRota ? "BROKEN": "OK") + " MOTION:: " + 
							(statusMovimiento ? "DETECTED": "OK"));
					
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
				}else {
					mw.WriteMessage("DOOR:: UNK WINDOW:: UNK MOTION:: UNK");
					pri.SetLampColorAndMessage("DOOR UNK", 0);
					vri.SetLampColorAndMessage("WINDOW UNK", 0);
					dmi.SetLampColorAndMessage("MOTION UNK", 0);
				}
				
				// Check temperature and effect control as necessary
				/*
				if (CurrentTemperature < TempRangeLow) // temperature is below threshhold
				{
					ti.SetLampColorAndMessage("TEMP LOW", 3);
					Heater(ON);
					Chiller(OFF);

				} else {

					if (CurrentTemperature > TempRangeHigh) // temperature is above threshhold
					{
						ti.SetLampColorAndMessage("TEMP HIGH", 3);
						Heater(OFF);
						Chiller(ON);

					} else {

						ti.SetLampColorAndMessage("TEMP OK", 1); // temperature is within threshhold
						Heater(OFF);
						Chiller(OFF);

					} // if
				} // if

				// Check humidity and effect control as necessary

				if (CurrentHumidity < HumiRangeLow)
				{
					hi.SetLampColorAndMessage("HUMI LOW", 3); // humidity is below threshhold
					Humidifier(ON);
					Dehumidifier(OFF);

				} else {

					if (CurrentHumidity > HumiRangeHigh) // humidity is above threshhold
					{
						hi.SetLampColorAndMessage("HUMI HIGH", 3);
						Humidifier(OFF);
						Dehumidifier(ON);

					} else {

						hi.SetLampColorAndMessage("HUMI OK", 1); // humidity is within threshhold
						Humidifier(OFF);
						Dehumidifier(OFF);

					} // if

				} // if
				 */
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
	* CONCRETE METHOD:: SetTemperatureRange
	* Purpose: This method sets the temperature range
	*
	* Arguments: float lowtemp - low temperature range
	*			 float hightemp - high temperature range
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/
	/*
	public void SetTemperatureRange(float lowtemp, float hightemp )
	{
		TempRangeHigh = hightemp;
		TempRangeLow = lowtemp;
		mw.WriteMessage( "***Temperature range changed to::" + TempRangeLow + "F - " + TempRangeHigh +"F***" );

	} // SetTemperatureRange
	*/
	/***************************************************************************
	* CONCRETE METHOD:: SetHumidityRange
	* Purpose: This method sets the humidity range
	*
	* Arguments: float lowhimi - low humidity range
	*			 float highhumi - high humidity range
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/
	/*
	public void SetHumidityRange(float lowhumi, float highhumi )
	{
		HumiRangeHigh = highhumi;
		HumiRangeLow = lowhumi;
		mw.WriteMessage( "***Humidity range changed to::" + HumiRangeLow + "% - " + HumiRangeHigh +"%***" );

	} // SetTemperatureRange
	*/
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
	* CONCRETE METHOD:: Heater
	* Purpose: This method posts events that will signal the temperature
	*		   controller to turn on/off the heater
	*
	* Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	*			 heater on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/
/*
	private void Heater( boolean ON )
	{
		// Here we create the event.

		Event evt;

		if ( ON )
		{
			evt = new Event( (int) 5, "H1" );

		} else {

			evt = new Event( (int) 5, "H0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending heater control message:: " + e);

		} // catch

	} // Heater
*/
	/***************************************************************************
	* CONCRETE METHOD:: Chiller
	* Purpose: This method posts events that will signal the temperature
	*		   controller to turn on/off the chiller
	*
	* Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	*			 chiller on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/
/*
	private void Chiller( boolean ON )
	{
		// Here we create the event.

		Event evt;

		if ( ON )
		{
			evt = new Event( (int) 5, "C1" );

		} else {

			evt = new Event( (int) 5, "C0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending chiller control message:: " + e);

		} // catch

	} // Chiller
*/
	/***************************************************************************
	* CONCRETE METHOD:: Humidifier
	* Purpose: This method posts events that will signal the humidity
	*		   controller to turn on/off the humidifier
	*
	* Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	*			 humidifier on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/
/*
	private void Humidifier( boolean ON )
	{
		// Here we create the event.

		Event evt;

		if ( ON )
		{
			evt = new Event( (int) 4, "H1" );

		} else {

			evt = new Event( (int) 4, "H0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending humidifier control message::  " + e);

		} // catch

	} // Humidifier
*/
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
/*
	private void Dehumidifier( boolean ON )
	{
		// Here we create the event.

		Event evt;

		if ( ON )
		{
			evt = new Event( (int) 4, "D1" );

		} else {

			evt = new Event( (int) 4, "D0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending dehumidifier control message::  " + e);

		} // catch

	}*/ // Dehumidifier

}// ECSMonitor