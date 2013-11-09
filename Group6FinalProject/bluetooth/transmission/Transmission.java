/*
* @author Sean Lawlor, Stepan Salenikovich, Francois OD
* @date November 6, 2013
* @class ECSE 211 - Design Principle and Methods
*/
package transmission;

import java.io.*;

public class Transmission {

	private DataOutputStream dos;
	
	// store the output stream to write to the channel in the default constructor
	public Transmission(DataOutputStream dos) {
		this.dos = dos;
	}
	
	// transmit the data specified, and return true if data transmitted successfully, otherwise false
	// which signifies and error
	// For Fall 2013
	public boolean transmit(char role, int startingCorner, int[] greenZoneCoords, int[] redZoneCoords) {
		try {
			switch(role) {
			case 'B':
				dos.writeInt(1);
				break;
			case 'G':
				dos.writeInt(2);
				break;
			default:
				dos.writeInt(1);
				break;
			}
			dos.flush();
			dos.writeChar(',');
			dos.flush();
			dos.writeInt(startingCorner);
			dos.flush();
			for(int i = 0; i < greenZoneCoords.length; i++) {
				dos.writeChar(',');
				dos.flush();
				dos.writeInt(greenZoneCoords[i]);
				dos.flush();
			}
			for(int i = 0; i < redZoneCoords.length; i++) {
				dos.writeChar(',');
				dos.flush();
				dos.writeInt(redZoneCoords[i]);
				dos.flush();
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	
	
	// transmit the data specified, and return true if data transmitted successfully, otherwise false
	// which signifies and error
	// For Fall 2012
	public boolean transmit(char role, int startingCorner, int fx, int fy, int dx, int dy) {
		try {
			switch(role) {
			case 'D':
				dos.writeInt(1);
				break;
			case 'A':
				dos.writeInt(2);
				break;
			default:
				dos.writeInt(1);
				break;
			}
			dos.flush();
			dos.writeChar(',');
			dos.flush();
			dos.writeInt(startingCorner);
			dos.flush();
			dos.writeChar(',');
			dos.flush();
			dos.writeInt(fx);
			dos.flush();
			dos.writeChar(',');
			dos.flush();
			dos.writeInt(fy);
			dos.flush();
			dos.writeChar(',');
			dos.flush();
			dos.writeInt(dx);
			dos.flush();
			dos.writeChar(',');
			dos.flush();
			dos.writeInt(dy);
			dos.flush();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	
	// transmit the data specified, and return true if data transmitted successfully, otherwise false
	// which signifies and error
	// For Winter 2012
	public boolean transmit(char role, int startingCorner, int w1, int w2, int bx, int by, int bsigma) {
		try {
			switch(role) {
			case 'F':
				dos.writeInt(1);
				break;
			case 'D':
				dos.writeInt(2);
				break;
			default:
				dos.writeInt(1);
				break;
			}
			dos.flush();
			dos.writeChar(',');
			dos.flush();
			dos.writeInt(startingCorner);
			dos.flush();
			dos.writeChar(',');
			dos.flush();
			dos.writeInt(w1);
			dos.flush();
			dos.writeChar(',');
			dos.flush();
			dos.writeInt(w2);
			dos.flush();
			dos.writeChar(',');
			dos.flush();
			dos.writeInt(bx);
			dos.flush();
			dos.writeChar(',');
			dos.flush();
			dos.writeInt(by);
			dos.flush();
			dos.writeChar(',');
			dos.flush();
			dos.writeInt(bsigma);
			dos.flush();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	// Winter 2011, don't use for this semester
	public boolean transmit(int startingCorner, int goalX, int goalY) {
		try {
			dos.writeInt(startingCorner);
			dos.writeChar(',');
			dos.writeInt(goalX);
			dos.writeChar(',');
			dos.writeInt(goalY);
			dos.flush();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
