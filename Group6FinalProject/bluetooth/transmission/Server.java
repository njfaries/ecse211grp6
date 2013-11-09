/*
 * @author Sean Lawlor, Stepan Salenikovich, Francois OD
 * @date November 6, 2013
 * @class ECSE 211 - Design Principle and Methods
 */
package transmission;

import lejos.pc.comm.*;

import java.io.*;

import universal.Universal;

import gui.*;

/*
 * This is the main PC Server function for the NXT communication for the Fall 2012 ECSE 211 Final Project
 */

public class Server {

	private MainWindow mw;

	public Server(MainWindow mw) {
		this.mw = mw;
	}

	private int nxtConnectorTransmit(String[] names, char[] roles, int[] startingCorners, int[] greenZoneCoords, int[] redZoneCoords) {
		NXTConnector conn = new NXTConnector();
		conn.addLogListener(new NXTCommLogListener() {
			public void logEvent(String message) {
				// System.out.println("BTSend Log.listener: "+message);
				mw.displayOutput("Connection Listener: Log.listener: " + message, true);
			}

			public void logEvent(Throwable throwable) {
				/*
				 * System.out.println("BTSend Log.listener - stack trace: "); throwable.printStackTrace();
				 */
				mw.displayOutput("Connection Listener: Log.listener - stack trace: ", false);
				mw.displayOutput(throwable.getLocalizedMessage(), true);
			}

		});
		DataOutputStream[] doss = new DataOutputStream[names.length];

		NXTInfo[][] nxtInfo = new NXTInfo[names.length][];
		this.mw.displayOutput("Searching for NXTs with the specified names", true);
		for (int i = 0; i < names.length; i++) {
			nxtInfo[i] = conn.search(names[i], null, NXTCommFactory.BLUETOOTH);
			if (nxtInfo[i].length < 1) {
				this.mw.displayOutput("No NXTs connected by name: " + names[i], false);
				this.mw.displayOutput("Please make sure you have the correct name and have paired first", true);
				return -1;
			} else {
				this.mw.displayOutput("Found " + nxtInfo.length + " NXTs with the matching name connected,", false);
				this.mw.displayOutput("using the first one", true);
				conn.connectTo(nxtInfo[i][0], NXTComm.PACKET);
				doss[i] = new DataOutputStream(conn.getOutputStream());
			}
		}

		try {
			// All paired, try transmission
			for (int i = 0; i < names.length; i++) {
				Transmission trans = new Transmission(doss[i]);
				if (!trans.transmit(roles[i], startingCorners[i], greenZoneCoords, redZoneCoords)) {
					this.mw.displayOutput("Transmission failed, trying once more then quitting...", true);
					if (!trans.transmit(roles[i], startingCorners[i], greenZoneCoords, redZoneCoords)) {
						this.mw.displayOutput("Transmission failed a second time, quitting...", true);
						doss[i].close();
						conn.close(); // close all comms
						return -1;
					}
				}
				doss[i].close(); // close specific comms chan
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			this.mw.displayOutput("Failed to provide nxt name", true);
			return -1;
		} catch (IOException e) {
			// e.printStackTrace();
			this.mw.displayOutput("Failed to init comms factory, do you have Bluetooth...?", true);
			return -1;
		}
		return 0;
	}
	
	private int nxtCommSuperNewTransmit(String [] names, char [] roles, int [] startingCorners, int[] greenZoneCoords, int[] redZoneCoord) {
		DataOutputStream[] doss = new DataOutputStream[names.length];
		try {
			NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			this.mw.displayOutput("Searching for NXTs with the specified names", true);
			NXTInfo[][] nxtInfo = new NXTInfo[names.length][];
			for (int i = 0; i < names.length; i++) {
				nxtInfo[i] = nxtComm.search(names[i], NXTCommFactory.BLUETOOTH);
				if (nxtInfo[i].length < 1) {
					this.mw.displayOutput("No NXTs connected by name: " + names[i], false);
					this.mw.displayOutput("Please make sure you have the correct name and have paired first", true);
					return -1;
				}
			}
			for (int i = 0; i < names.length; i ++) {
				this.mw.displayOutput("Found " + nxtInfo[i].length + " NXTs with the matching name of " + names[i] + " connected,", false);
				this.mw.displayOutput("using the first one", true);
				nxtComm.open(nxtInfo[i][0]);
				doss[i] = new DataOutputStream(nxtComm.getOutputStream());
				Transmission trans = new Transmission(doss[i]);
				if (!trans.transmit(roles[i], startingCorners[i], greenZoneCoords, redZoneCoord)) {
					this.mw.displayOutput("Transmission failed, trying once more then quitting...", true);
					if (!trans.transmit(roles[i], startingCorners[i], greenZoneCoords, redZoneCoord)) {
						this.mw.displayOutput("Transmission failed a second time, quitting...", true);
						doss[i].close();
						return -1;
					}
				}
				doss[i].close(); // close specific comms chan	
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			this.mw.displayOutput("Failed to provide nxt name", true);
			return -1;
		} catch (NXTCommException e) {
			// e.printStackTrace();
			this.mw.displayOutput("Failed to close streams", true);
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			this.mw.displayOutput("Failed to init comms factory, do you have Bluetooth...?", true);
			return -1;
		}
		return 0;
	}
	
	private int nxtCommNewTransmit(String [] names, char [] roles, int [] startingCorners, int[] greenZoneCoords, int[] redZoneCoord) {
		DataOutputStream[] doss = new DataOutputStream[names.length];
		try {
			NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			this.mw.displayOutput("Searching for NXTs with the specified names", true);
			NXTInfo[][] nxtInfo = new NXTInfo[names.length][];
			for (int i = 0; i < names.length; i++) {
				nxtInfo[i] = nxtComm.search(names[i], NXTCommFactory.BLUETOOTH);
				if (nxtInfo[i].length < 1) {
					this.mw.displayOutput("No NXTs connected by name: " + names[i], false);
					this.mw.displayOutput("Please make sure you have the correct name and have paired first", true);
					return -1;
				} else {
					this.mw.displayOutput("Found " + nxtInfo.length + " NXTs with the matching name connected,", false);
					this.mw.displayOutput("using the first one", true);
					nxtComm.open(nxtInfo[i][0]);
					doss[i] = new DataOutputStream(nxtComm.getOutputStream());
					Transmission trans = new Transmission(doss[i]);
					if (!trans.transmit(roles[i], startingCorners[i], greenZoneCoords, redZoneCoord)) {
						this.mw.displayOutput("Transmission failed, trying once more then quitting...", true);
						if (!trans.transmit(roles[i], startingCorners[i], greenZoneCoords, redZoneCoord)) {
							this.mw.displayOutput("Transmission failed a second time, quitting...", true);
							doss[i].close();
							nxtComm.close(); // close all comms
							return -1;
						}
					}
					doss[i].close(); // close specific comms chan	
				}
			}
			/*
			// All paired, try transmission
			for (int i = 0; i < names.length; i++) {
				Transmission trans = new Transmission(doss[i]);
				if (!trans.transmit(roles[i], startingCorners[i], d1, d2, w1)) {
					this.mw.displayOutput("Transmission failed, trying once more then quitting...", true);
					if (!trans.transmit(roles[i], startingCorners[i], d1, d2, w1)) {
						this.mw.displayOutput("Transmission failed a second time, quitting...", true);
						doss[i].close();
						nxtComm.close(); // close all comms
						return -1;
					}
				}
				doss[i].close(); // close specific comms chan
			}
			*/
		} catch (ArrayIndexOutOfBoundsException e) {
			this.mw.displayOutput("Failed to provide nxt name", true);
			return -1;
		} catch (NXTCommException e) {
			// e.printStackTrace();
			this.mw.displayOutput("Failed to close streams", true);
			return -1;
		} catch (IOException e) {
			// e.printStackTrace();
			this.mw.displayOutput("Failed to init comms factory, do you have Bluetooth...?", true);
			return -1;
		}
		return 0;
	}

	private int nxtCommTransmit(String[] names, char[] roles, int[] startingCorners, int[] greenZoneCoords, int[] redZoneCoord) {
		DataOutputStream[] doss = new DataOutputStream[names.length];
		try {
			NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			this.mw.displayOutput("Searching for NXTs with the specified names", true);
			NXTInfo[][] nxtInfo = new NXTInfo[names.length][];
			for (int i = 0; i < names.length; i++) {
				nxtInfo[i] = nxtComm.search(names[i], NXTCommFactory.BLUETOOTH);
				if (nxtInfo[i].length < 1) {
					this.mw.displayOutput("No NXTs connected by name: " + names[i], false);
					this.mw.displayOutput("Please make sure you have the correct name and have paired first", true);
					return -1;
				} else {
					this.mw.displayOutput("Found " + nxtInfo.length + " NXTs with the matching name connected,", false);
					this.mw.displayOutput("using the first one", true);
					nxtComm.open(nxtInfo[i][0]);
					doss[i] = new DataOutputStream(nxtComm.getOutputStream());
				}
			}
			// All paired, try transmission
			for (int i = 0; i < names.length; i++) {
				Transmission trans = new Transmission(doss[i]);
				if (!trans.transmit(roles[i], startingCorners[i], greenZoneCoords, redZoneCoord)) {
					this.mw.displayOutput("Transmission failed, trying once more then quitting...", true);
					if (!trans.transmit(roles[i], startingCorners[i], greenZoneCoords, redZoneCoord)) {
						this.mw.displayOutput("Transmission failed a second time, quitting...", true);
						doss[i].close();
						return -1;
					}
				}
				doss[i].close(); // close specific comms chan
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			this.mw.displayOutput("Failed to provide nxt name", true);
			return -1;
		} catch (NXTCommException e) {
			// e.printStackTrace();
			this.mw.displayOutput("Failed to close streams", true);
			return -1;
		} catch (IOException e) {
			// e.printStackTrace();
			this.mw.displayOutput("Failed to init comms factory, do you have Bluetooth...?", true);
			return -1;
		}
		return 0;
	}

	public int transmit(String[] names, char[] roles, int[] startingCorners, int[] greenZoneCoords, int[] redZoneCoord) {
		switch (Universal.TRANSMIT_RULE) {
		case BOTH:
			if (names.length != roles.length && roles.length != startingCorners.length) {
				this.mw.displayOutput("Length of names, roles, and starting corners don't match", true);
				return -1;
			}
			switch (Universal.CONNECTION_OPTION) {
			case NXT_COMM:
				this.mw.displayOutput("Transmission of data to both NXTs via NXTComm option", true);
				return nxtCommTransmit(names, roles, startingCorners, greenZoneCoords, redZoneCoord);
			case NXT_COMM_NEW:
				this.mw.displayOutput("Transmission of data to both NXTs via the NEW NXTComm option", true);
				return nxtCommNewTransmit(names, roles, startingCorners, greenZoneCoords, redZoneCoord);
			case NXT_CONNECTOR:
				this.mw.displayOutput("Transmission of data to both NXTs via NxtConnection option", true);
				return nxtConnectorTransmit(names, roles, startingCorners, greenZoneCoords, redZoneCoord);
			case NXT_COMM_SUPER_NEW:
				this.mw.displayOutput("Transmission of data to both NXTs via the SUPER new NXTComm option", true);
				return nxtCommSuperNewTransmit(names, roles, startingCorners, greenZoneCoords, redZoneCoord);
			case OLD:
				this.mw.displayOutput("Transmitting to both NXTs using the old method", true);
				int result = -1;
				for (int i = 0; i < names.length; i ++) {
					result = this.transmit(names[i], roles[i], startingCorners[i], greenZoneCoords, redZoneCoord);
					if (result == -1)
						return -1;
				}
				return result;
				
			default:
				this.mw.displayOutput("Failed to determine Debug.CONNECTION_OPTION", true);
				return -1;
			}
		case BUILDER_ONLY:
			for (int i = 0; i < roles.length; i++)
				if (roles[i] == 'B')
					return transmit(names[i], roles[i], startingCorners[i], greenZoneCoords, redZoneCoord);
			this.mw.displayOutput("No builder specified to transmit to", true);
			return -1;
		case GARBAGECOLLECTOR_ONLY:
			for (int i = 0; i < roles.length; i++)
				if (roles[i] == 'G')
					return transmit(names[i], roles[i], startingCorners[i], greenZoneCoords, redZoneCoord);
			this.mw.displayOutput("No garbage collector specified to transmit to", true);
			return -1;
		default:
			this.mw.displayOutput("Something went wrong detecting the startup mode from Debug class", true);
			return -1;
		}
	}

	public int transmit(String name, char role, int startingCorner, int[] greenZoneCoords, int[] redZoneCoord) {
		DataOutputStream dos = null;
		try {
			// initalize bluetooth channel
			NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			// search for all available nxts by the specified name
			this.mw.displayOutput("Searching for all NXTs by the name: " + name, true);
			NXTInfo[] nxtInfo = nxtComm.search(name, NXTCommFactory.BLUETOOTH);

			if (nxtInfo.length < 1) {
				// failed to find any nxts with that name
				this.mw.displayOutput("No NXTs connected by that name, please pair nxt fist,", false);
				this.mw.displayOutput("then make sure you have the correct name, then run program again", true);
				return -1;
			} else {
				// found a valid NXT
				this.mw.displayOutput("Found " + nxtInfo.length + " NXTs with the matching name connected,", false);
				this.mw.displayOutput("using the first one", true);
				nxtComm.open(nxtInfo[0]);
				dos = new DataOutputStream(nxtComm.getOutputStream());
				Transmission trans = new Transmission(dos);
				if (!trans.transmit(role, startingCorner, greenZoneCoords, redZoneCoord)) {
					this.mw.displayOutput("Transmission failed, trying once more then quitting...", true);
					if (!trans.transmit(role, startingCorner, greenZoneCoords, redZoneCoord)) {
						this.mw.displayOutput("Transmission failed a second time, quitting...", true);
						dos.close();
						nxtComm.close();
						return -1;
					}
				}
				dos.flush();
				this.mw.displayOutput("Transmission Successful, quitting...", true);
				// nxtComm.close();
				return 0;
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			this.mw.displayOutput("Failed to provide nxt name", true);
			return -1;
		} catch (IOException e) {
			this.mw.displayOutput("Failed to close streams", true);
			return -1;
		} catch (NXTCommException e) {
			this.mw.displayOutput("Failed to init comms factory, do you have Bluetooth...?", true);
			e.printStackTrace();
			return -1;
		}
	}

	// This call was used in Winter 2011, not used here
	public int transmit(String name, int startingCorner, int goalX, int goalY) {
		DataOutputStream dos = null;

		try {
			// initalize bluetooth channel
			NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			// search for all available nxts by the specified name
			this.mw.displayOutput("Searching for all NXTs by the name: " + name, true);
			NXTInfo[] nxtInfo = nxtComm.search(name, NXTCommFactory.BLUETOOTH);

			if (nxtInfo.length < 1) {
				// failed to find any nxts with that name
				this.mw.displayOutput("No NXTs connected by that name, please pair nxt fist,", false);
				this.mw.displayOutput("then make sure you have the correct name, then run program again", true);
				return -1;
			} else {

				// found a valid NXT
				this.mw.displayOutput("Found " + nxtInfo.length + " NXTs with the matching name connected,", false);
				this.mw.displayOutput("using the first one", true);
				nxtComm.open(nxtInfo[0]);
				dos = new DataOutputStream(nxtComm.getOutputStream());
				Transmission trans = new Transmission(dos);
				if (!trans.transmit(startingCorner, goalX, goalY)) {
					this.mw.displayOutput("Transmission failed, traying once more then quitting...", true);
					if (!trans.transmit(startingCorner, goalX, goalY)) {
						this.mw.displayOutput("Transmission failed again, quitting...", true);
						return -1;
					}
				} else {
					this.mw.displayOutput("Transmission Successful, quitting...", true);
					return 0;
				}
				dos.close();
				nxtComm.close();
			}
			// catch all exceptions if errors occur, and print status message
		} catch (ArrayIndexOutOfBoundsException e) {
			this.mw.displayOutput("Failed to provide nxt name", true);
			return -1;
		} catch (IOException e) {
			this.mw.displayOutput("Failed to close streams", true);
			return -1;
		} catch (NXTCommException e) {
			this.mw.displayOutput("Failed to init comms factory, do you have Bluetooth...?", true);
			// e.printStackTrace();
			return -1;
		}
		return -1;
	}
}
