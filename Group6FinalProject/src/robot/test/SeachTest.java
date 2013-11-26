package robot.test;

import robot.bluetooth.BluetoothConnection;
import robot.bluetooth.PlayerRole;
import robot.bluetooth.StartCorner;
import robot.bluetooth.Transmission;
import robot.collection.*;
import robot.localization.Localization;
import robot.mapping.Map;
import robot.mapping.Scan;
import robot.mapping.Scan2;
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
 * @version 1.0
 * @since 2013-11-04
 */
public class SearchTest extends Thread {
	public enum FunctionType {
		IDLE, RECEIVE, LOCALIZE, SEARCH, POINT_NAVIGATE, IDENTIFY, COLLECT, END_NAVIGATE, RELEASE
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
	
	private CollectionSystem collection;
	private Identify id;
	private Scan2 scan;
	
	private FunctionType function = FunctionType.SEARCH;
	
	private int blocksCollected = 0;
	private int maxBlocks = 2;
	private long startTime = 0;
	private long elapsedTime = 0;
	private final int INTIAL_CAGE_ROTATION = -435; 
	private double searchFrom = 0, searchTo = 90;
	
	private double[] pos = new double[3];
	
	StartCorner corner = StartCorner.BOTTOM_LEFT;
	PlayerRole role = PlayerRole.BUILDER;
	int[] greenZone = new int[] {60,90,90,120};
	int[] redZone = new int[4];
	
	public static void main(String[] args) {
		new SearchTest();
	}

	/**
	 * The robot controller delegates the starting and ending of various
	 * subtasks like localization, searching and collection.
	 */
	public SearchTest() {
		startTime = System.currentTimeMillis();
		
		//receive();
		
		new Map(role,  redZone, greenZone);
		//new LCDInfo();

		us = new USGather(usFront);
		cg = new ColorGather(csLeft, csRight, csBlockReader, new OdometryCorrection());
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		nav = new Navigation2(robo);
		
		scan = new Scan2(nav, us);
		//need to construct localization with transmission.startingCorner
		corner = StartCorner.BOTTOM_LEFT;
		loc = new Localization(us, cg, corner, nav);
		
		new Odometer(robo);

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
			if(elapsedTime > gameTime - 60000 && function != FunctionType.END_NAVIGATE)
				 function = FunctionType.END_NAVIGATE;
				
			//LCD.clear();
			if (function == FunctionType.LOCALIZE)
				localize();
			else if (function == FunctionType.SEARCH)
				search(searchFrom, searchTo, 1);
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
		function = FunctionType.SEARCH; // Once finished localizing robot goes
										// immediately to search mode.
	}

	// Search method (performs scans)
	private void search(double fromAngle, double toAngle, int direction){
		LCD.clear();
		LCD.drawString("Searching",0,0);
		
		nav.turnTo(fromAngle, 0);
		while(!nav.isDone()){
			try{Thread.sleep(400);} catch(InterruptedException e){ }
			
		}
		nav.stop();
		
		double[] newBlock = scan.findBlock(toAngle, direction);
		
		if(newBlock == null){
			function = FunctionType.POINT_NAVIGATE;
			return;
		}
		
		double currBlockDistance = newBlock[0];
		
		LCD.drawString("r:" + (int)newBlock[0] + ", t:" + (int)newBlock[1], 0, 1);
		
		Odometer.getPosition(pos);
		
		nav.turnTo(newBlock[1], 0);
		while(!nav.isDone()){
			try{Thread.sleep(400);} catch(InterruptedException e){ }
			
		}
		nav.stop();
		
		//-------------Approach block -----------------------------------
		
		nav.move();
		long startTime = System.currentTimeMillis();
		while(!us.flagObstruction()){
			long elapsedTime = System.currentTimeMillis() - startTime;
			double distanceTravelled = Odometer.getSpeed() * elapsedTime / 1000;
			LCD.drawString("b:" + (int)currBlockDistance + " d:" + (int)distanceTravelled, 0,2);
			
			if(distanceTravelled > currBlockDistance + 5){
				nav.stop();
				Odometer.getPosition(pos);
				searchFrom = pos[2] - 45;
				searchTo = pos[2] + 45;
				function = FunctionType.SEARCH;
				return;
			}
			try{Thread.sleep(30);} catch(InterruptedException e){ }
		}
		nav.stop();
		
		function = FunctionType.IDENTIFY;
	}
	
	// CURRENTLY UNUSED
	private void findNextBlock(){
		Odometer.getPosition(pos);
		
		double turnAngle = pos[2] + 50;
		if(turnAngle > 360)
			turnAngle -= 360;
		
		nav.turnTo(turnAngle, 0);
		while (!nav.isDone()) {
			try { Thread.sleep(400); } 
			catch (InterruptedException e) { }
		}
		nav.stop();
		
		searchFrom = pos[2];
		searchTo = pos[2] + 180;
		function = FunctionType.SEARCH;
	}
	// Handles navigating to a block (allows the scanner to continue in case an
	// unexpected obstacle appears (i.e. the other player)
	// CURRENTLY UNUSED
	private void navigateToBlock() {
		LCD.drawString("block Nav", 0, 0);
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
		//nav.move();
		nav.travelTo(wp[0], wp[1]);
		while (!us.flagObstruction()) {
			//if the navigation is done no block has been found
			if(nav.isDone()) {
				//move back and define function
				nav.reverse();
				try{ Thread.sleep(2000); }
				catch(InterruptedException e){ }
				nav.stop();
				
				nav.rotate(1, 0);
				while(!us.flagObstruction()){
					try{ Thread.sleep(100); }
					catch(InterruptedException e){ }
				}
				try{ Thread.sleep(500); }
				catch(InterruptedException e){ }
				nav.stop();
				
				return;
			}
			try { Thread.sleep(20); } 
			catch (InterruptedException e) { }
		}
		
		nav.stop();
		
		Map.waypointReached();
		
/*		if(Map.hasNewWaypoint())
			function = FunctionType.BLOCK_NAVIGATE;
		else
			function = FunctionType.IDENTIFY;*/
	}

	// Handles the navigation to the end
	private void navigateToEnd() {		
		LCD.drawString("go home", 0, 0);
		Map.buildEndWaypoints();
		
		double[] wp = new double[2];
		Map.getWaypoint(wp);
		
		Odometer.getPosition(pos);
		double newHeading = Math.toDegrees(Math.atan2(wp[1] - pos[1], wp[0] - pos[0]));

		if(newHeading < 0)
			newHeading += 360;
		newHeading = newHeading % 360;
		
		nav.turnTo(newHeading, 0);
		while (!nav.isDone()) {
			try { Thread.sleep(400); } 
			catch (InterruptedException e) { }
		}
		nav.stop();
		
		double requiredDistance = Math.sqrt(Math.pow(wp[0] - pos[0],2) + Math.pow(wp[1] - pos[1],2));
		long startTime = System.currentTimeMillis();
		double distanceTravelled = 0;
		
		nav.move();
		while (distanceTravelled < requiredDistance) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			distanceTravelled = Odometer.getSpeed() * elapsedTime / 1000;

			try { Thread.sleep(50); } 
			catch (InterruptedException e) { }
		}
		nav.stop();
		
		Map.waypointReached();
		
		function = FunctionType.RELEASE;
	}
	
	// Handles navigating to a point (allows the scanner to continue in case an
	// unexpected obstacle appears (i.e. the other player)
	private void navigateToNextPoint() {
		LCD.clear();
		LCD.drawString("next Point", 0, 0);
		
		Odometer.getPosition(pos);
		double[] endZone = Map.getEndCenter();
		
		// Calculate Heading
		double heading = Math.toDegrees(Math.atan2(endZone[1] - pos[1], endZone[0] - pos[0]));
		if(heading < 0)
			heading += 360;
		heading = heading % 360;
		LCD.drawString("ex:" + (int)endZone[0] + " ey:" + (int)endZone[1] + " h:" + heading, 0,1);
		
		// Find offsets
		double xOffset = 45 * Math.cos(Math.toRadians(heading));
		double yOffset = 45 * Math.sin(Math.toRadians(heading));
		
		Map.buildNextPointWaypoints(pos[0] + xOffset, pos[1] + yOffset);
		
		while(Map.hasNewWaypoint()){
			double[] wp = new double[2];
			
			Map.getWaypoint(wp);
			
			LCD.drawString((int)wp[0] + "|" + (int)wp[1],0,2);
			Odometer.getPosition(pos);
			double newHeading = Math.toDegrees(Math.atan2(wp[1] - pos[1], wp[0] - pos[0]));
			if(newHeading < 0)
				newHeading += 360;
			newHeading = newHeading % 360;
			
			nav.turnTo(newHeading, 0);
			while (!nav.isDone()) {
				try { Thread.sleep(400); } 
				catch (InterruptedException e) { }
			}
			nav.stop();
			
			double requiredDistance = Math.sqrt(Math.pow(wp[0] - pos[0],2) + Math.pow(wp[1] - pos[1],2));
			long startTime = System.currentTimeMillis();
			double distanceTravelled = 0;
			
			nav.move();
			while (distanceTravelled < requiredDistance) {
				long elapsedTime = System.currentTimeMillis() - startTime;
				distanceTravelled = Odometer.getSpeed() * elapsedTime / 1000;

				try { Thread.sleep(50); } 
				catch (InterruptedException e) { }
			}
			nav.stop();
			
			Map.waypointReached();
		}

		Odometer.getPosition(pos);
		searchFrom = pos[2] - 90;
		searchTo = pos[2] + 90;
		function = FunctionType.SEARCH;
	}
	
	// Identifies a specific block
	private void identify() {
		LCD.drawString("identify", 0, 0);
		
		// if the block is blue collect it
		if (id.isBlue()) {
			LCD.drawString("blue", 0,7);
			function = FunctionType.COLLECT;
		}
		
		// else the robot has backed up and does a search
		else {
			LCD.drawString("not blue", 0,7);
			Odometer.getPosition(pos);
			Map.addBlock(us.getFilteredData() / 2, pos[2]);
			
			searchFrom = pos[2];
			searchTo = pos[2] + 180;
			function = FunctionType.SEARCH;
/*			Map.blockChecked(false);
			Map.buildNextBlockWaypoints();
			
			if(Map.hasNewWaypoint())
				function = FunctionType.BLOCK_NAVIGATE;
			else
				function = FunctionType.POINT_NAVIGATE;*/
		}	
	}

	// Collects navigated-to block
	private void collect() {
		LCD.drawString("collect", 0,0);
		collection.lowerCage();
		collection.openCage();
		
		nav.move();
		try { Thread.sleep(3500); } 
		catch (InterruptedException e) { }
		nav.stop();
		
		try { Thread.sleep(250); } 
		catch (InterruptedException e) { }
		
		collection.closeCage();
		collection.raiseCage();

		function = FunctionType.IDLE;
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

	public void alignBlock() {
		nav.move();
		try {Thread.sleep(3000);} catch(InterruptedException e) {}
		nav.stop();
		nav.reverse();
		try {Thread.sleep(50);} catch(InterruptedException e) {}
		//while(us.getDistance() > 5);
		nav.stop();
	}

}
