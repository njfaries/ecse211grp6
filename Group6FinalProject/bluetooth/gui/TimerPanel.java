package gui;

import gui.defaults.*;

import java.awt.*;
import java.util.*;

@SuppressWarnings("serial")
public class TimerPanel extends DPMPanel {

	private final static float MINUTES = 10f;
	private final static int ONE_SECOND_IN_MS = 1000;
	private final static int FULL_TIME = (int)(MINUTES * 60);
	private int currentTime;
	private OneSecond oneSecond;
	private Timer oneSecondTimer;
	private TextArea timerArea;

	public TimerPanel(MainWindow mw) {
		super(mw);
		this.currentTime = FULL_TIME;
		oneSecond = null;
		oneSecondTimer = null;
		this.setupPanel();
	}

	private void setupPanel() {
		// setup formatting constraints
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		
		// place label
		c.gridx = 0;
		c.gridy = 0;
		Label lbl = new Label("TIME LEFT: ", Label.CENTER);
		lbl.setFont(new Font("Serif", Font.BOLD, 32));
		this.add(lbl, c);
		
		// timer field
		c.gridy = 1;
		int min = FULL_TIME / 60;
		int sec = FULL_TIME % 60;
		timerArea = new TextArea(String.format("%02d:%02d", min, sec), 1, 5, TextArea.SCROLLBARS_NONE);
		timerArea.setFont(new Font("Serif", Font.BOLD, 64));
		timerArea.setEditable(false);
		this.add(timerArea, c);
	}

	public void start() {
		//oneSecondTimer.cancel();
		if (oneSecondTimer == null) {
			oneSecondTimer = new Timer();
			oneSecond = new OneSecond(this);
			oneSecondTimer.schedule(oneSecond, 0, ONE_SECOND_IN_MS);
		}
	}

	// stop timer
	public void stop() {
		if (oneSecondTimer != null) {
			oneSecondTimer.cancel();
			oneSecondTimer = null;
		}
	}

	// clear timer
	public void clear() {
		if (oneSecondTimer != null)
			stop();
		this.currentTime = FULL_TIME;
		int min = FULL_TIME / 60;
		int sec = FULL_TIME % 60;
		timerArea.setText(String.format("%02d:%02d", min, sec));
	}

	// decrement timer
	public void trigger() {
		if (currentTime > 0) {
			this.currentTime--;
			int min = currentTime / 60;
			int sec = currentTime % 60;
			//timerArea.append("\n" + String.format("%02d:%02d", min, sec));
			timerArea.replaceRange(String.format("%02d:%02d", min, sec), 0, 5);
		} else {
			// if time up, display notification
			stop();
			new DPMPopupNotification("TIME UP!!!", this.mw, new Font("Serif", Font.BOLD, 32));
		}
	}

	private class OneSecond extends TimerTask {
		private TimerPanel owner;

		public OneSecond(TimerPanel owner) {
			this.owner = owner;
		}

		@Override
		public void run() {
			this.owner.trigger();
		}
	}
}
