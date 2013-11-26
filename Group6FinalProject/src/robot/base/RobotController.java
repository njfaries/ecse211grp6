package robot.base;

import robot.bluetooth.*;
import robot.collection.*;
import robot.localization.Localization;
import robot.mapping.*;
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
		IDLE, LOCALIZE, SEARCH, IDENTIFY, BLOCK_NAVIGATE, OPEN_NAVIGATE, COLLECT, END_NAVIGATE, RELEASE, RETURN
	};

	public enum RobotMode {
		STACKER, GARBAGE
	};

	private static double OPEN_DIST = 40;
	
	private static NXTRegulatedMotor leftMotor = new NXTRegulatedMotor(MotorPort.A);
	private static NXTRegulatedMotor rightMotor = new NXTRegulatedMotor(MotorPort.B);
	private static NXTRegulatedMotor cageMotor = new NXTRegulatedMotor(MotorPort.C);

	private static UltrasonicSensor usFront = new UltrasonicSensor(SensorPort.S4);
	private static UltrasonicSensor usTop = new UltrasonicSensor(SensorPort.S3);
	
	private static ColorSensor csLeft = new ColorSensor(SensorPort.S1);
	private static ColorSensor csRight = new ColorSensor(SensorPort.S2);

	private CollectionSystem collection;
	private Navigation2 nav;
	private TwoWheeledRobot robo;

	private USGather us;
	private ColorGather cg;

	private Localization loc;
	private Scanner sc;
	
	StartCorner corner = StartCorner.BOTTOM_LEFT;
	PlayerRole role = PlayerRole.BUILDER;
	int[] greenZone = new int[] {60,90,90,120};
	int[] redZone = new int[4];

	private int blocksCollected = 0;
	private int maxBlocks = 2;
	private double searchFrom = 0, searchTo = 90;
	
	private FunctionType function = FunctionType.SEARCH;

	private double[] pos = new double[3];

	public static void main(String[] args) {
		new RobotController();
	}

	/**
	 * The robot controller delegates the starting and ending of various
	 * subtasks like localization, searching and collection.
	 */
	public RobotController() {
		//receive();
		
		new Map2(role, greenZone, redZone);

		us = new USGather(usFront, usTop);
		cg = new ColorGather(csLeft, csRight, new OdometryCorrection());
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		nav = new Navigation2(robo);
		
		//need to construct localization with transmission.startingCorner
		loc = new Localization(us, cg, StartCorner.BOTTOM_LEFT, nav);
		sc = new Scanner(nav, us);
		
		//corrector = new OdometryCorrection(cg, WHEEL_RADIUS, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST);
		new Odometer(robo);

		//id = new Identify(cg, us, nav);

		collection = new CollectionSystem(cageMotor, nav);
		collection.rotateCage(-430);
		
		this.start();
	}

	// Runs all the control code (calling localization, navigation,
	// identification, etc)
	public void run() {
		boolean running = true;
		double[] endCenter = Map2.getEndCenter();
		
		while (running) {
			switch(function) {
				case IDLE:
					try { Thread.sleep(500); }
					catch(InterruptedException e){ }
					continue;
				case LOCALIZE:
					localize();
					
					searchFrom = 0;
					searchTo = 90;
					function = FunctionType.SEARCH;
					continue;
				case SEARCH:
					sc.scanRange(searchFrom, searchTo);
					function = FunctionType.OPEN_NAVIGATE;
					continue;
				case BLOCK_NAVIGATE: 
				case IDENTIFY:
				case OPEN_NAVIGATE:
					double t = sc.bestOpenAngle(searchFrom, searchTo, endCenter[0], endCenter[1]);
					//if -1 is return of best open angle, none open, scan again
					if(t == -1) { 
						function = FunctionType.SEARCH;
						break;
					}
					Odometer.getPosition(pos);
					double x = pos[0] + OPEN_DIST * Math.cos(Math.toRadians(t));
					double y = pos[1] + OPEN_DIST * Math.sin(Math.toRadians(t));
					
					
					//if green zone center is within a threshold go to it as it is assumed as open
					if(blocksCollected >= maxBlocks && Math.abs(pos[0] - endCenter[0]) < 15 
							&& Math.abs(pos[1] - endCenter[1]) < 15 ) {
						function = FunctionType.RELEASE;
						break;
					}
					else 
						nav.travelTo(x, y);
					while( !nav.isDone() ) {
						try { Thread.sleep(200); }
						catch(InterruptedException e){ }
					}
					Odometer.getPosition(pos);
					searchFrom = pos[2] - 45;
					searchTo = pos[2] + 45;
					
					function = FunctionType.SEARCH;
					continue;
				case END_NAVIGATE:
					
				case COLLECT:
					collect();
					continue;
				case RELEASE:
					endCenter = Map2.getEndCenter();
					nav.travelTo(endCenter[0], endCenter[1]);
					while( !nav.isDone() ) {
						try { Thread.sleep(200); }
						catch(InterruptedException e){ }
					}
					release();
					continue;
				case RETURN:
					returnToStart();
					running = false;
					continue;
			}
			
			try { Thread.sleep(50); } catch (InterruptedException e) { }
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

	// Initiates the localization of the robot
	private void localize() {
		cageMotor.rotate(-330);
		loc.localize();
	}

	// Identifies a specific block
	private void identify() {

	}

	// Collects said block
	private void collect() {
		if(blocksCollected == 0){
			collection.lowerCage();
			collection.openCage();
			
			nav.move();
			try { Thread.sleep(2000); } 
			catch (InterruptedException e) {}
			nav.stop();
			
			try { Thread.sleep(250); } 
			catch (InterruptedException e) {}
			
			collection.closeCage();
			collection.raiseCage();
		}
		else{
			nav.move();
			try {Thread.sleep(3000);} catch(InterruptedException e) {}
			nav.stop();
			
			nav.reverse();
			try {Thread.sleep(50);} catch(InterruptedException e) {}
			//while(us.getDistance() > 5);
			nav.stop();
			
			collection.lowerCage();
			collection.openCage();
			
			nav.move();
			try {Thread.sleep(500);}
			catch (InterruptedException e) {}
			nav.stop();
			
			collection.closeCage();
			collection.raiseCage();
		}
		
		blocksCollected++;
		
		function = FunctionType.OPEN_NAVIGATE;
	}

	// Releases the entire stack (only done at the end of the match)
	private void release() {
		collection.release();
		
		nav.reverse();
		try { Thread.sleep(2500); } 
		catch (InterruptedException e) { }
		nav.stop();
	}
	
	private void returnToStart(){
		
	}

}
