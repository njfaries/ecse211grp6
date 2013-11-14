package robot.bluetooth;
/**
* @author Sean Lawlor, Stepan Salenikovich, Francois OD
* @date November 6, 2013
* @class ECSE 211 - Design Principle and Methods
*/
import lejos.nxt.Button;
import lejos.nxt.LCD;

public class BTTest {
	// example call of the transmission protocol
	// The print function is just for debugging to make sure data is received correctly
	// make sure to import the bluetooth.BluetoothConnection library
	@SuppressWarnings("unused")
	public static void main(String [] args) {
		BluetoothConnection conn = new BluetoothConnection();
		// as of this point the bluetooth connection is closed again, and you can pair to another NXT (or PC) if you wish
		
		// example usage of Tranmission class
		Transmission t = conn.getTransmission();
		if (t == null) {
			LCD.drawString("Failed to read transmission", 0, 5);
		} else {
			StartCorner corner = t.startingCorner;
			PlayerRole role = t.role;
			// green zone is defined by these (bottom-left and top-right) corners:
			int[] greenZone = t.greenZone;
			
			// red zone is defined by these (bottom-left and top-right) corners:
			int[] redZone = t.redZone;
			
			// print out the transmission information to the LCD
			conn.printTransmission();
		}
		// stall until user decides to end program
		Button.waitForAnyPress();
	}
}
