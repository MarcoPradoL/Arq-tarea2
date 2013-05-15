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

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;


class SprinklerController
{
	private static JOptionPane frame = new JOptionPane("Encender Rociadores");
	private static JLabel lblMensaje = new JLabel("Fuego detectado: los rociadores encendaran en 15 segundos");
	private static JDialog dialogo = null;
	
	public static void main(String args[])
	{
		String EvtMgrIP;					// Event Manager IP address
		Event Evt = null;					// Event object
		EventQueue eq = null;				// Message Queue
		int EvtId = 0;						// User specified event ID
		EventManagerInterface em = null;	// Interface object to the event manager
		boolean statusFuego = false; 	// actual estado del sensor de Deteccion de fuego 
		boolean Sprinkler = false;			// Estado del rociador			
		boolean nuevoEvento = false;		// utilizado para determinar si es un nuevo evento de fuego.
		int	Delay = 2500;					// The loop delay (2.5 seconds)
		boolean Done = false;				// Loop termination flag

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
			float WinPosY = 0.45f;	//This is the Y position of the message window in terms 
								 	//of a percentage of the screen height 
			
			MessageWindow mw = new MessageWindow("Sprinkler Controller Status Console", WinPosX, WinPosY);

			// ahora colocamos el indicador de los rociadores
						
			Indicator hi = new Indicator ("Sprinkler OFF", mw.GetX(), mw.GetY()+mw.Height());
		
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
				try
				{
					eq = em.GetEventQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting event queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for EventIDs = 14, this is a request to turn the
				// humidifier or dehumidifier on/off. Note that we get all the messages
				// at once... there is a 2.5 second delay between samples,.. so
				// the assumption is that there should only be a message at most.
				// If there are more, it is the last message that will effect the
				// output of the humidity as it would in reality.

				int qlen = eq.GetSize();
				//System.out.println("longitud de cola : " + qlen);
				for ( int i = 0; i < qlen; i++ )
				{
					Evt = eq.GetEvent();
					if ( Evt.GetEventId() == 12 ) // leyendo dato de deteccion de fuego
					{
				
						if(statusFuego == Boolean.parseBoolean(Evt.GetMessage())){
							nuevoEvento= false;
							//System.out.println("no es nuevo even");
						}
						else{
							nuevoEvento= true;
							statusFuego = Boolean.parseBoolean(Evt.GetMessage());
							//System.out.println("es un nuevo evento");
						}

					} // if
					//System.out.println("el valor de esta cosa es: " + Evt.GetEventId());
					if ( Evt.GetEventId() == 14 )	//fire event
					{
						if (Evt.GetMessage().equalsIgnoreCase("S1") && !Sprinkler && nuevoEvento) // Sprinkler on
						{
							Sprinkler = true;
							mw.WriteMessage("Received Sprinkler turn on event" );

							//preguntar si se desea encender o cancelar los rociadores
							Sprinkler = confirmSprinkler();
							//System.out.println("el valor de esta cosa es: " + Sprinkler);
							
							nuevoEvento=false;
							// Confirm that the message was recieved and acted on
							/*if (Sprinkler) {
								ConfirmMessage( em, "F0" , 13);
							}*/
							break;

						} // if

						if (Evt.GetMessage().equalsIgnoreCase("S0")) // Sprinkler off
						{
							Sprinkler = false;
							mw.WriteMessage("Received Sprinkler turn off event" );

							// Confirm that the message was recieved and acted on

							//ConfirmMessage( em, "F1" , 13 );

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

				    	mw.WriteMessage( "\n\nSimulation Stopped. \n");
				    	
				    	hi.dispose();

					} // if

				} // for				
				
				// actualizar el estado del indicador de los rociadores
				
				if (Sprinkler)
				{
					// Set to green, humidifier is on

					hi.SetLampColorAndMessage("SPRINKLER ON", 1);

				} else {

					// Set to black, humidifier is off
					hi.SetLampColorAndMessage("SPRINKLER OFF", 0);

				} // if
				
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

	static private  boolean  confirmSprinkler() {
		frame.setMessage(lblMensaje);
		frame.setOptionType(JOptionPane.OK_CANCEL_OPTION);
		if ( null == dialogo )
        {
            dialogo = frame.createDialog(null, "Encender los Rociadores");
        }
        else
        {
            dialogo.setTitle("Encender los Rociadores");
        }
		isRun();
		dialogo.setVisible(true);
		if ((int)frame.getValue() == JOptionPane.OK_OPTION) {
			
			return true;
		}
		return false;
		
	}

	
	private static void isRun() {
		TimerTask task;
		final Timer tiempo = new Timer();
		task= new TimerTask() {
			int contador=0;
			public void run() {
				lblMensaje.setText("Fuego detectado: los rociadores encendaran en "+ (15-contador) +" segundos");
				if(contador == 15){
					frame.setValue(JOptionPane.OK_OPTION);
					dialogo.setVisible(false);
					tiempo.cancel();
			}
				contador++;
			} 
		}; 
		tiempo.schedule(task,0,1000); 
		
	}

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

} // SecurityControllers