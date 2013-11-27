package robot.test;

import robot.bluetooth.*;
import lejos.nxt.Button;
import lejos.nxt.LCD;

/**
 * Contains the main method for the robot. Initiates classes and passes them the
 * necessary motors, sensors, and various constants. Controls and and delegates
 * tasks to various subroutines.
 * 
 * @author Andreas, Nathaniel
 * @version 1.2.0
 * @since 2013-11-04
 */
public class BluetoothTest extends Thread {
	StartCorner corner;
	PlayerRole role;
	int[] greenZone;
	int[] redZone;

	public static void main(String[] args) {
		new BluetoothTest();
	}

	/**
	 * The robot controller delegates the starting and ending of various
	 * subtasks like localization, searching and collection.
	 */
	public BluetoothTest() {
		receive();
		this.start();
	}

	// Runs all the control code (calling localization, navigation,
	// identification, etc)
	public void run() {
		boolean running = true;		
		while (running) {
			LCD.clear();
			LCD.drawString(role.toString(),0,0);
			LCD.drawString(corner.toString(),0,1);
			LCD.drawString(greenZone[0] + "," + greenZone[1] + "," + greenZone[2] + ", "+ greenZone[3],0,2);
			LCD.drawString(redZone[0] + "," + redZone[1] + "," + redZone[2] + ", "+ redZone[3],0,3);
			try { Thread.sleep(500); } catch (InterruptedException e) { }
		}
	}

	// Receives instruction via bluetooth
	private void receive() {
		BluetoothConnection conn = new BluetoothConnection();
		// as of this point the bluetooth connection is closed again, and you can pair to another NXT (or PC) if you wish
		
		Transmission t = conn.getTransmission();
		if (t == null) {
			LCD.drawString("Failed to read transmission", 0, 5);
		} else {
			corner = t.startingCorner;
			role = t.role;
			// green zone is defined by these (bottom-left and top-right) corners:
			greenZone = t.greenZone;
			
			// red zone is defined by these (bottom-left and top-right) corners:
			redZone = t.redZone;
		}
		// stall until user decides to end program
		Button.waitForAnyPress();
	}
}
