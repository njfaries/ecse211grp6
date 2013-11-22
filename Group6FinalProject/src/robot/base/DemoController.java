package robot.base;

import robot.bluetooth.BluetoothConnection;
import robot.bluetooth.PlayerRole;
import robot.bluetooth.StartCorner;
import robot.bluetooth.Transmission;
import robot.collection.*;
import robot.localization.Localization;
import robot.mapping.Map;
import robot.mapping.Scan;
import robot.navigation.*;
import robot.sensors.*;
import robot.test.IdenCollTest.FunctionType;
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
public class DemoController extends Thread {
	public enum FunctionType {
		IDLE, RECEIVE, LOCALIZE, INITIAL_SEARCH, SEARCH, BLOCK_NAVIGATE, POINT_NAVIGATE, IDENTIFY, COLLECT, END_NAVIGATE, RELEASE
	};

	private static long gameTime = 300000;

	private static NXTRegulatedMotor leftMotor = new NXTRegulatedMotor(MotorPort.A);
	private static NXTRegulatedMotor rightMotor = new NXTRegulatedMotor(MotorPort.B);
	private static NXTRegulatedMotor cageMotor = new NXTRegulatedMotor(MotorPort.C);

	private static UltrasonicSensor usFront = new UltrasonicSensor(SensorPort.S4);

	private static ColorSensor csLeft = new ColorSensor(SensorPort.S1);
	private static ColorSensor csRight = new ColorSensor(SensorPort.S2);
	private static ColorSensor csBlockReader = new ColorSensor(SensorPort.S3);

	private USGather us;
	private ColorGather cg;
	
	private Localization loc;
	
	private Navigation2 nav;
	private TwoWheeledRobot robo;
	private OdometryCorrection corrector;
	
	private CollectionSystem collection;
	private Identify id;
	
	private FunctionType function = FunctionType.INITIAL_SEARCH;
	
	private int blocksCollected = 0;
	private int maxBlocks = 2;
	private long startTime = 0;
	private long elapsedTime = 0;
	private final int INTIAL_CAGE_ROTATION = -450;

	private double[] pos = new double[3];
	
	StartCorner corner = StartCorner.BOTTOM_LEFT;
	PlayerRole role = PlayerRole.BUILDER;
	int[] greenZone = new int[4];
	int[] redZone = new int[4];
	
	public static void main(String[] args) {
		new DemoController();
	}

	/**
	 * The robot controller delegates the starting and ending of various
	 * subtasks like localization, searching and collection.
	 */
	public DemoController() {
		startTime = System.currentTimeMillis();
		
		//receive();
		
		new Map(role,  redZone, greenZone);
		//new LCDInfo();

		us = new USGather(usFront);
		cg = new ColorGather(csLeft, csRight, csBlockReader);
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		nav = new Navigation2(robo);
		
		//need to construct localization with transmission.startingCorner
		corner = StartCorner.BOTTOM_LEFT;
		loc = new Localization(us, cg, corner, nav);
		
		corrector = new OdometryCorrection(cg);
		new Odometer(robo , null);

		id = new Identify(cg, us, nav);

		collection = new CollectionSystem(cageMotor, nav);
		collection.rotateCage(INTIAL_CAGE_ROTATION);
		this.start();
	}

	// Runs all the control code (calling localization, navigation,
	// identification, etc)
	public void run() {
		
		while (true) {
			elapsedTime = System.currentTimeMillis() - startTime;
			if(elapsedTime > gameTime - 30000 && function != FunctionType.END_NAVIGATE)
				 function = FunctionType.END_NAVIGATE;
				
			LCD.clear();
			if (function == FunctionType.LOCALIZE)
				localize();
			else if (function == FunctionType.INITIAL_SEARCH)
				search(0, 90, 1);
			else if (function == FunctionType.SEARCH){
				Odometer.getPosition(pos);
				search(pos[2], pos[2] + 359, 1);
			}
			else if (function == FunctionType.BLOCK_NAVIGATE)
				navigateToBlock();
			else if (function == FunctionType.POINT_NAVIGATE)
				navigateToNextPoint();
			else if (function == FunctionType.END_NAVIGATE)
				navigateToEnd();
			else if (function == FunctionType.IDENTIFY)
				identify();
			else if (function == FunctionType.COLLECT)
				collect();
			else if (function == FunctionType.RELEASE)
				release();

			try { Thread.sleep(50); } 
			catch (InterruptedException e) { }
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
	}

	// Initiates the localization of the robot 
	private void localize() {
		LCD.drawString("Localize", 0, 4);
		loc.localize();
		function = FunctionType.INITIAL_SEARCH; // Once finished localizing robot goes
										// immediately to search mode.
	}

	// Search method (performs scans)
	private void search(double fromAngle, double toAngle, int direction) {
		LCD.drawString("search", 0, 4);
		//0 as to turn to with smallest angle before starting the scan
		nav.turnTo(fromAngle, 0);
		while (!nav.isDone()) {
			try { Thread.sleep(400); } 
			catch (InterruptedException e) { }
		}
		nav.turnTo(toAngle, direction);

		new Scan(nav, us);
		
		while (!Scan.scanParsed()) {
			try { Thread.sleep(400); } 
			catch (InterruptedException e) { }
		}
		LCD.drawString("search done", 0, 4);
		
		Map.cleanBlocks();
		Map.buildNextBlockWaypoints();
		
		if (Map.hasNewWaypoint()) {
			function = FunctionType.BLOCK_NAVIGATE;
		}
		else{
			function = FunctionType.POINT_NAVIGATE;
		}
	}

	// Handles navigating to a block (allows the scanner to continue in case an
	// unexpected obstacle appears (i.e. the other player)
	private void navigateToBlock() {
		LCD.drawString("nav1 start", 0, 4);
		double[] wp = new double[2];
		Map.getWaypoint(wp);
		
		Odometer.getPosition(pos);
		double angle = Math.toDegrees(Math.atan2(wp[1] - pos[1], wp[0] - pos[0]));
		nav.turnTo(angle, 0);

		while (!nav.isDone()) {
			try { Thread.sleep(400); } 
			catch (InterruptedException e) { }
		}
		
		nav.stop();
		
		LCD.clear();
		LCD.drawString("nav1.0 end", 0, 4);
		
		nav.move();
		nav.travelTo(wp[0], wp[1]);
		while (!us.flagObstruction()) {
			//if the navigation is done no block has been found
			if(nav.isDone()) {
				//move back and search again
				nav.reverse();
				try{ Thread.sleep(1000); }
				catch(InterruptedException e){ }
				nav.stop();
				function = FunctionType.SEARCH;
				return;
			}
			try { Thread.sleep(20); } 
			catch (InterruptedException e) { }
		}
		
		nav.stop();
		
		LCD.clear();
		LCD.drawString("nav1.1 end", 0, 4);
		
		Map.waypointReached();
		
		LCD.clear();
		LCD.drawString("nav1.2 end", 0, 4);
		
		if(Map.hasNewWaypoint())
			navigateToBlock();
		else
			function = FunctionType.IDENTIFY;
		
		LCD.clear();
		LCD.drawString("nav1.3 end", 0, 4);
	}

	// Handles the navigation to the end
	private void navigateToEnd() {		
		LCD.drawString("go home", 0, 4);
		Map.buildEndWaypoints();
		
		double[] wp = new double[2];
		Map.getWaypoint(wp);
		
		nav.travelTo(wp[0], wp[1]);
		while (!nav.isDone()) {
			try { Thread.sleep(400); } 
			catch (InterruptedException e) { }
		}
		nav.stop();
		
		function = FunctionType.RELEASE;
	}
	
	// Handles navigating to a point (allows the scanner to continue in case an
	// unexpected obstacle appears (i.e. the other player)
	private void navigateToNextPoint() {
		LCD.drawString("navigate2", 0, 4);
		Odometer.getPosition(pos);
		Map.buildNextPointWaypoints(pos[0] + 62, pos[1] + 62);
		
		while(Map.hasNewWaypoint()){
			double[] wp = new double[2];
			Map.getWaypoint(wp);
			
			nav.travelTo(wp[0], wp[1]);
			while (!nav.isDone()) {
				try { Thread.sleep(400); } 
				catch (InterruptedException e) { }
			}
			nav.stop();
			
			Map.waypointReached();
		}

		function = FunctionType.SEARCH;
	}
	
	// Identifies a specific block
	private void identify() {
		LCD.drawString("identify", 0, 4);
		
		// if the block is blue collect it
		if (id.isBlue()) {
			LCD.drawString("blue", 0,7);
			Map.blockChecked(true);
			function = FunctionType.COLLECT;
		}
		
		// else the robot has backed up and does a search
		else {
			LCD.drawString("not blue", 0,7);
			Map.blockChecked(false);
			Map.buildNextBlockWaypoints();
			
			if(Map.hasNewWaypoint())
				function = FunctionType.BLOCK_NAVIGATE;
			else
				function = FunctionType.POINT_NAVIGATE;
		}	
	}

	// Collects navigated-to block
	private void collect() {
		LCD.drawString("collect", 0,4);
		collection.lowerCage();
		collection.openCage();
		
		nav.move();
		try { Thread.sleep(2500); } 
		catch (InterruptedException e) { }
		nav.stop();
		
		try { Thread.sleep(250); } 
		catch (InterruptedException e) { }
		
		collection.closeCage();
		collection.raiseCage();

		function = FunctionType.END_NAVIGATE;
	}
	// Releases the entire stack (only done at the end of the match)
	private void release() {
		collection.release();
		
		nav.reverse();
		try { Thread.sleep(2500); } 
		catch (InterruptedException e) { }
		nav.stop();
		
		function = FunctionType.IDLE;
	}

}