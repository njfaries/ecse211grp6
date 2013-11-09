package gui;

import java.awt.*;
import java.awt.event.*;
import transmission.*;
import universal.Universal;
import gui.defaults.*;

@SuppressWarnings("serial")
public class NXTInformationPanel extends DPMPanel implements ActionListener {
	private Server server;
	private Button start, stop, clear;
	private TextField bNxtName, bStartCorner;
	private TextField gNxtName, gStartCorner;
	private TextField greenZone1X, greenZone1Y, greenZone2X, greenZone2Y;
	private TextField redZone1X, redZone1Y, redZone2X, redZone2Y;
	private SendSelectPanel sendSelectPanel;

	public NXTInformationPanel(MainWindow mw) {
		// initalize information panel
		super(mw);
		this.layoutPanel(mw);
		server = new Server(mw);
	}

	private void layoutPanel(MainWindow mw) {
		GridBagConstraints c = new GridBagConstraints();
		this.setFont(new Font("Helvetica", Font.PLAIN, 14));
		this.setLayout(new GridBagLayout());

		// send select check box
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.ipady = 25;
		sendSelectPanel = new SendSelectPanel(mw);
		this.add(sendSelectPanel, c);

		// BUILDER NAME
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.ipady = 5;
		Label nxtNameLbl = new Label("Builder NXT Name: ", Label.RIGHT);
		this.add(nxtNameLbl, c);

		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
		bNxtName = new TextField(11);
		new DPMToolTip("Enter the NXT's Bluetooth name here", bNxtName);
		this.add(bNxtName, c);
		
		// BUILDER CORNER
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		Label startCornerLabel = new Label("Builder Start Corner: ",
				Label.RIGHT);
		this.add(startCornerLabel, c);

		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 2;
		bStartCorner = new TextField(11);
		new DPMToolTip("Enter the NXT's starting corner (1-4)", bStartCorner);
		this.add(bStartCorner, c);

		
		// GARBAGE COLLECTOR NAME
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		Label aNxtNameLabel = new Label("Garbage Collector NXT Name: ", Label.RIGHT);
		this.add(aNxtNameLabel, c);

		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 2;
		gNxtName = new TextField(11);
		new DPMToolTip("Enter the NXT's Bluetooth name here", gNxtName);
		this.add(gNxtName, c);
		
		
		// GARBAGE COLLECTOR CORNER
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
		Label aStartCornerLabel = new Label("Garbage Collector Start Corner: ",
				Label.RIGHT);
		this.add(aStartCornerLabel, c);

		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 2;
		gStartCorner = new TextField(11);
		new DPMToolTip("Enter the NXT's starting corner (1-4)", gStartCorner);
		this.add(gStartCorner, c);

		
		// GREEN ZONE BOTTOM-LEFT CORNER
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 1;
		Label greenZone1Label = new Label("Green Zone bottom-left corner (x, y): ", Label.RIGHT);
		this.add(greenZone1Label, c);

		c.gridx = 1;
		c.gridy = 6;
		c.gridwidth = 1;
		greenZone1X = new TextField(4);
		new DPMToolTip("Enter X location of the Green Zone bottom-left corner here", greenZone1X);
		this.add(greenZone1X, c);
		
		c.gridx = 2;
		c.gridy = 6;
		c.gridwidth = 1;
		greenZone1Y = new TextField(4);
		new DPMToolTip("Enter Y location of the Green Zone bottom-left corner here", greenZone1Y);
		this.add(greenZone1Y, c);
		
		
		// GREEN ZONE TOP-RIGHT CORNER
		c.gridx = 0;
		c.gridy = 7;
		c.gridwidth = 1;
		Label greenZone2Label = new Label("Green Zone top-right corner (x, y): ", Label.RIGHT);
		this.add(greenZone2Label, c);

		c.gridx = 1;
		c.gridy = 7;
		c.gridwidth = 1;
		greenZone2X = new TextField(4);
		new DPMToolTip("Enter X location of the Green Zone top-right corner here", greenZone2X);
		this.add(greenZone2X, c);
		
		c.gridx = 2;
		c.gridy = 7;
		c.gridwidth = 1;
		greenZone2Y = new TextField(4);
		new DPMToolTip("Enter Y location of the Green Zone top-right corner here", greenZone2Y);
		this.add(greenZone2Y, c);
		
		
		// RED ZONE BOTTOM-LEFT CORNER
		c.gridx = 0;
		c.gridy = 8;
		c.gridwidth = 1;
		Label redZone1Label = new Label("Red Zone bottom-left corner (x, y): ", Label.RIGHT);
		this.add(redZone1Label, c);

		c.gridx = 1;
		c.gridy = 8;
		c.gridwidth = 1;
		redZone1X = new TextField(4);
		new DPMToolTip("Enter X location of the Red Zone bottom-left corner here", redZone1X);
		this.add(redZone1X, c);
		
		c.gridx = 2;
		c.gridy = 8;
		c.gridwidth = 1;
		redZone1Y = new TextField(4);
		new DPMToolTip("Enter Y location of the Red Zone bottom-left corner here", redZone1Y);
		this.add(redZone1Y, c);
		
		
		// RED ZONE TOP-RIGHT CORNER
		c.gridx = 0;
		c.gridy = 9;
		c.gridwidth = 1;
		Label redZone2Label = new Label("Red Zone top-right corner (x, y): ", Label.RIGHT);
		this.add(redZone2Label, c);

		c.gridx = 1;
		c.gridy = 9;
		c.gridwidth = 1;
		redZone2X = new TextField(4);
		new DPMToolTip("Enter X location of the Red Zone top-right corner here", redZone2X);
		this.add(redZone2X, c);
		
		c.gridx = 2;
		c.gridy = 9;
		c.gridwidth = 1;
		redZone2Y = new TextField(4);
		new DPMToolTip("Enter Y location of the Red Zone top-right corner here", redZone2Y);
		this.add(redZone2Y, c);

		
		
		c.gridx = 0;
		c.gridy = 11;
		c.gridwidth = 1;
		this.start = new Button("Start");
		this.start.addActionListener(this);
		new DPMToolTip("Start the program", this.start);
		this.add(start, c);

		c.gridx = 1;
		this.stop = new Button("Stop");
		this.stop.addActionListener(this);
		new DPMToolTip("Stop the program", this.stop);
		this.add(stop, c);

		c.gridx = 2;
		this.clear = new Button("Clear");
		this.clear.addActionListener(this);
		new DPMToolTip("Clear all entered values", this.clear);
		this.add(clear, c);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Button bt = (Button) e.getSource();
		try {
			if (bt.equals(this.start)) {
				Universal.TransmitRule sendTo = this.sendSelectPanel
						.getSendSelction();
				Universal.TRANSMIT_RULE = sendTo;

				int bStartCornerN = Integer.parseInt(this.bStartCorner
						.getText().trim());

				if (sendTo == Universal.TransmitRule.BOTH
						|| sendTo == Universal.TransmitRule.BUILDER_ONLY) {
					if (bStartCornerN > 4 || bStartCornerN < 1) {
						new DPMPopupNotification("Builder starting corner of "
								+ bStartCornerN + " is out of the range 1-4",
								this.mw);
						return;
					}
				}

				int gStartCornerN = Integer.parseInt(this.gStartCorner
						.getText().trim());

				if (sendTo == Universal.TransmitRule.BOTH
						|| sendTo == Universal.TransmitRule.GARBAGECOLLECTOR_ONLY) {
					if (gStartCornerN > 4 || gStartCornerN < 1) {
						new DPMPopupNotification("Garbage Collector starting corner of "
								+ gStartCornerN + " is out of the range 1-4",
								this.mw);
						return;
					}
				}

				if (sendTo == Universal.TransmitRule.BOTH) {
					if (bStartCornerN == gStartCornerN) {
						new DPMPopupNotification(
								"Builder and Garbage Collector starting positions can't be the same",
								this.mw);
						return;
					}
				}

				int gz1x = Integer.parseInt(this.greenZone1X.getText().trim()), 
						gz1y = Integer.parseInt(this.greenZone1Y.getText().trim()), 
						gz2x = Integer.parseInt(this.greenZone2X.getText().trim()), 
						gz2y = Integer.parseInt(this.greenZone2Y.getText().trim()), 
						rz1x = Integer.parseInt(this.redZone1X.getText().trim()), 
						rz1y = Integer.parseInt(this.redZone1Y.getText().trim()), 
						rz2x = Integer.parseInt(this.redZone2X.getText().trim()), 
						rz2y = Integer.parseInt(this.redZone2Y.getText().trim());
				if (gz2x <= gz1x || gz2y <= gz1y) {
					new DPMPopupNotification("Green Zone top-right corner coordinates should be " +
							"strictly larger than bottom-left corner coordinates", this.mw);
					return;
				}
				if (rz2x <= rz1x || rz2y <= rz1y) {
					new DPMPopupNotification("Red Zone top-right corner coordinates should be " +
							"strictly larger than bottom-left corner coordinates", this.mw);
					return;	
				}

				String bNxtNameN = this.bNxtName.getText().trim();
				String gNxtNameN = this.gNxtName.getText().trim();

				String[] names = new String[] { bNxtNameN, gNxtNameN };
				char[] roles = new char[] { 'B', 'G' };
				int[] startCorners = new int[] { bStartCornerN, gStartCornerN };
				int[] greenZoneCoords = new int[] { gz1x, gz1y, gz2x, gz2y };
				int[] redZoneCoords = new int[] { rz1x, rz1y, rz2x, rz2y };

				// try bluetooth transmission
				int success = 0;
				success = server.transmit(names, roles, startCorners, greenZoneCoords, redZoneCoords);
				if (success != 0) {
					new DPMPopupNotification(
							"Some Bluetooth error occured trying to connect to attacker or defender, see project output",
							this.mw);
				}
				/*
				 * int success = 0; if (!started) { success =
				 * server.transmit(fNxtNameN, 'F', fStartCornerN, d1, d2, w1);
				 * if (success == 0) { success = server.transmit(dNxtNameN, 'D',
				 * dStartCornerN, d1, d2, w1); } else { new
				 * DPMPopupNotification(
				 * "Some Bluetooth error occured trying to connect to attacker",
				 * this.mw); } }
				 */
				if (success == 0) {
					this.mw.startTimer();
				} else
					// transmission failed
					new DPMPopupNotification(
							"Some Bluetooth error occured trying to connect to defender, please retry",
							this.mw);
			} else if (bt.equals(this.stop)) {
				// stop button pressed
				this.mw.pauseTimer();
			} else if (bt.equals(this.clear)) {
				// clear button pressed, clear fields, reset timer, and clear
				// Bluetooth
				this.clearFields();
				this.mw.clearTimer();
				this.mw.clearBluetoothPanel();
			} else {
				System.out.println("Non-handeled event...");
			}
		} catch (NumberFormatException ex) {
			// string where number should be
			new DPMPopupNotification(
					"One of the numerical values was not a number", this.mw);
			return;
		}

	}

	private void clearFields() {
		this.bNxtName.setText("");
		this.bStartCorner.setText("");
		this.greenZone1X.setText("");
		this.greenZone1Y.setText("");
		this.greenZone2X.setText("");
		this.greenZone2Y.setText("");
		this.redZone1X.setText("");
		this.redZone1Y.setText("");
		this.redZone2X.setText("");
		this.redZone2Y.setText("");
		this.gNxtName.setText("");
		this.gStartCorner.setText("");
	}
}
