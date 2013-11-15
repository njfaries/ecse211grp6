package robot.base;

import robot.collection.*;
import robot.localization.Localization;
import robot.localization.Localization.StartCorner;
import robot.mapping.Map;
import robot.mapping.Scan;
import robot.navigation.*;
import robot.sensors.*;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;
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
		IDLE, RECEIVE, LOCALIZE, SEARCH, IDENTIFY, BLOCK_NAVIGATE, POINT_NAVIGATE, COLLECT, END_NAVIGATE, RELEASE
	};

	public enum RobotMode {
		STACKER, GARBAGE
	};

	private static double WHEEL_RADIUS = 2.125, ODOCORRECT_SENS_WIDTH,
			ODOCORRECT_SENS_DIST;

	private double scanStart = 0;
	private int scanAngle = 90, scanDirection = 0;
	private boolean firstScan = true;

	private static NXTRegulatedMotor leftMotor = new NXTRegulatedMotor(MotorPort.A);
	private static NXTRegulatedMotor rightMotor = new NXTRegulatedMotor(MotorPort.B);
	private static NXTRegulatedMotor cageMotor = new NXTRegulatedMotor(MotorPort.C);

	private static UltrasonicSensor usFront = new UltrasonicSensor(SensorPort.S4);

	private static ColorSensor csLeft = new ColorSensor(SensorPort.S1);
	private static ColorSensor csRight = new ColorSensor(SensorPort.S2);
	private static ColorSensor csBlockReader = new ColorSensor(SensorPort.S3);

	private CollectionSystem collection;

	private OdometryCorrection corrector;

	private Navigation2 nav;
	private TwoWheeledRobot robo;

	private USGather us;
	private ColorGather cg;

	private Localization loc;

	private Identify id;

/*	StartCorner corner;
	PlayerRole role;*/
	int[] greenZone;
	int[] redZone;

	private int blocksCollected = 0;
	private int maxBlocks = 2;
	
	private FunctionType function = FunctionType.LOCALIZE;

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
		
		new Map(/*role,*/ greenZone, redZone);
		new LCDInfo();

		us = new USGather(usFront);
		cg = new ColorGather(csLeft, csRight, csBlockReader);
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		nav = new Navigation2(robo);
		
		//need to construct localization with transmission.startingCorner
		loc = new Localization(us, cg, StartCorner.BOTTOM_LEFT, nav);
		
		//corrector = new OdometryCorrection(cg, WHEEL_RADIUS, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST);
		new Odometer(robo , corrector);

		id = new Identify(cg, us, nav);

		collection = new CollectionSystem(cageMotor, nav);

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
/*		BluetoothConnection conn = new BluetoothConnection();
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
		Button.waitForAnyPress();*/
	}

	// Initiates the localization of the robot
	private void localize() {
		cageMotor.rotate(-330);
		loc.localize();
		function = FunctionType.SEARCH; // Once finished localizing robot goes
										// immediately to search mode.
	}

	// Search method (performs scans)
	private void search(double fromAngle, double toAngle, int direction) {
		
		if(!firstScan) {
			Odometer.getPosition(pos);
			scanStart = pos[2];
			scanAngle = (int)pos[2] + 357;
			direction = 1;
		}
		
		//0 as to turn to with smallest angle before starting the scan
		nav.turnTo(fromAngle, 0);
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
		//after first execution of search change search to angles
		if(firstScan) firstScan = false;
	}

	// Handles navigating to a point (allows the scanner to continue in case an
	// unexpected obstacle appears (i.e. the other player)
	private void navigateToBlock() {
		//Odometer.runCorrection(true);
		while (!nav.isDone()) {
			// Keep the ultrasonic sensor scanning to detect obstacles
			// If an obstacle appears (probably the other player)
			// nav.stop();
		}

		function = FunctionType.IDENTIFY;
	}

	// Identifies a specific block
	private void identify() {
		collection.rotateCage(60);
		
		// if the block is blue collect it
		
		if (id.isBlue()) {
			function = FunctionType.COLLECT;
		}
		
		// else the robot has backed up and does a search
		else {
			Map.getCurrentBlock().investigate();
			Map.updateWaypoint(false);
			
			if (Map.hasNewWaypoint()) {
				function = FunctionType.BLOCK_NAVIGATE;
			}
			else{
				function = FunctionType.POINT_NAVIGATE;
			}
		}
	}

	// Collects said block
	private void collect() {
		if(blocksCollected == 0){
			collection.lowerCage();
			collection.openCage();
			
			nav.moveStraight(20);
			while(!nav.isDone()){
				try { Thread.sleep(400); } 
				catch (InterruptedException e) { }
			}
			
			collection.closeCage();
			collection.raiseCage();
		}
		else{
			nav.moveStraight(20);
			while(!nav.isDone()){
				try { Thread.sleep(400); } 
				catch (InterruptedException e) { }
			}
			
			collection.collect();
		}
		
		blocksCollected++;
		
		if(blocksCollected >= maxBlocks)
			function = FunctionType.END_NAVIGATE;
		else
			function = FunctionType.SEARCH;
	}

	// Handles the navigation to the end
	private void navigateToEnd() {

	}

	// Releases the entire stack (only done at the end of the match)
	private void release() {
		collection.release();
		function = FunctionType.IDLE;
	}

}
