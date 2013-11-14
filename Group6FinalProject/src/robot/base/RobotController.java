package robot.base;

import robot.collection.*;
import robot.localization.Localization;
import robot.localization.Localization.StartCorner;
import robot.mapping.Map;
import robot.mapping.Scan;
import robot.navigation.*;
import robot.sensors.*;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.ColorSensor;
import lejos.nxt.UltrasonicSensor;

/**
 * Contains the main method for the robot. Initiates classes and passes them the
 * necessary motors, sensors, and various constants. Controls and and delegates
 * tasks to various subroutines.
 * 
 * @author Andreas, Nathaniel
 * @version 1.2.0
 * @since 2013-11-04
 */
public class RobotController extends Thread {
	public enum FunctionType {
		IDLE, RECEIVE, LOCALIZE, SEARCH, IDENTIFY, BLOCK_NAVIGATE, COLLECT, END_NAVIGATE, RELEASE
	};

	public enum RobotMode {
		STACKER, GARBAGE
	};

	private static double WHEEL_RADIUS = 2.125, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST;
	private static int endX1, endY1, endX2, endY2;

	private double scanStart = 0;
	private int scanAngle = 90, scanDirection = 0;

	private NXTRegulatedMotor leftMotor;
	private NXTRegulatedMotor rightMotor;
	private NXTRegulatedMotor clawMotor;

	private UltrasonicSensor usFront;

	private ColorSensor csFront;
	private ColorSensor csBack;
	private ColorSensor csBlockReader;

	private CollectionSystem collection;

	private Odometer odo;
	private OdometryCorrection corrector;

	private Navigation nav;
	private TwoWheeledRobot robo;

	private USGather us;
	private ColorGather cg;

	private Localization loc;

	private Identify id;

	/*
	 * private BluetoothConnection bt; private Transmission transmission;
	 */

	private FunctionType function = FunctionType.IDLE;
	private RobotMode mode = null;

	private double[] pos = new double[3];

	public static void main(String[] args) {
		new RobotController();
	}

	/**
	 * The robot controller delegates the starting and ending of various
	 * subtasks like localization, searching and collection.
	 */
	public RobotController() {
		receive();
		
		new Map(endX1, endY1, endX2, endY2);
		new LCDInfo();

		us = new USGather(usFront);
		cg = new ColorGather(csFront, csBack, csBlockReader);

		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		nav = new Navigation(robo);
		loc = new Localization(us, cg, StartCorner.BOTTOM_LEFT, nav);
		
		corrector = new OdometryCorrection(cg, WHEEL_RADIUS, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST);
		odo = new Odometer(robo , corrector);

		id = new Identify(cg, us, nav);

		collection = new CollectionSystem(clawMotor, nav);

		this.start();
	}

	// Runs all the control code (calling localization, navigation,
	// identification, etc)
	public void run() {
		while (true) {
			if (function == FunctionType.LOCALIZE)
				localize();
			else if (function == FunctionType.SEARCH)
				search(scanStart, scanAngle, scanDirection);
			else if (function == FunctionType.BLOCK_NAVIGATE)
				navigateToBlock();
			else if (function == FunctionType.IDENTIFY)
				identify();
			else if (function == FunctionType.COLLECT)
				collect();
			else if (function == FunctionType.RELEASE)
				release();

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {

			}
		}
	}

	// Receives instruction via bluetooth
	private void receive() {
		/*
		 * bt = new BluetoothConnection(); transmission = bt.getTransmission();
		 * mode = (transmission.role.equals(PlayerRole.BUILDER)) ?
		 * RobotMode.STACKER : RobotMode.GARBAGE;
		 */}

	// Initiates the localization of the robot
	private void localize() {
		/*
		 * loc = new Localization(usFront, csBack, csBack, csBack,
		 * transmission.startingCorner, robo); loc.localize();
		 */
		function = FunctionType.SEARCH; // Once finished localizing robot goes
										// immediately to search mode.
	}

	// Search method (performs scans)
	private void search(double fromAngle, double toAngle, int direction) {
		nav.turnTo(fromAngle, direction);
		while (!nav.isDone()) {
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
			}
		}
		nav.turnTo(toAngle, direction);

		new Scan(nav, us);

		while (!Scan.scanParsed()) {
			try { Thread.sleep(400); } 
			catch (InterruptedException e) { }
		}

		if (Map.hasNewWaypoint()) {
			double[] wp = new double[] { 0, 0 };
			Map.getWaypoint(wp);
			nav.travelTo(wp[0], wp[1]);

			function = FunctionType.BLOCK_NAVIGATE;
		}
	}

	// Handles navigating to a point (allows the scanner to continue in case an
	// unexpected obstacle appears (i.e. the other player)
	private void navigateToBlock() {
		Odometer.runCorrection(true);
		while (!nav.isDone()) {
			// Keep the ultrasonic sensor scanning to detect obstacles
			// If an obstacle appears (probably the other player)
			// nav.stop();
		}

		function = FunctionType.IDENTIFY;
	}

	// Identifies a specific block
	private void identify() {
		collection.lowerCage();
		while (!collection.isDone()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		// if the block is blue collect it
		if (id.needToCollectBlue()) {
			function = FunctionType.COLLECT;
		}
		// else the robot has backed up and does a search
		else {
			// add the wooden block to map
			Map.getCurrentBlock().investigate();
			function = FunctionType.SEARCH;
		}
	}

	// Collects said block
	private void collect() {
		collection.collectBlock();

		while (!collection.isDone()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}

		Odometer.getPosition(pos);
		scanStart = pos[2];
		scanAngle = 359;
		scanDirection = 1;

		function = FunctionType.SEARCH;
	}

	// Handles the navigation to the end
	private void navigateToEnd() {

	}

	// Releases the entire stack (only done at the end of the match)
	private void release() {
		collection.releaseStack();

		while (!collection.isDone()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}

		function = FunctionType.IDLE;
	}

}
