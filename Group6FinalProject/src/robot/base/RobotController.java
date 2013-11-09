package robot.base;
import robot.collection.*;
import robot.localization.Localization;
import robot.mapping.Coordinates;
import robot.mapping.Map;
import robot.navigation.*;
import robot.sensors.*;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.ColorSensor;
import lejos.nxt.UltrasonicSensor;
import src.bluetooth.*;

/**
 * Contains the main method for the robot.
 * Initiates classes and passes them the necessary motors, sensors, and various constants.
 * Controls and and delegates tasks to various subroutines.
 * 
 * @author Andreas, Nathaniel
 * @version 1.2.0
 * @since 2013-11-04
 */
public class RobotController extends Thread{
	public enum FunctionType { IDLE, RECEIVE, LOCALIZE, SEARCH, IDENTIFY, NAVIGATE, COLLECT, RELEASE };
	public enum RobotMode {STACKER, GARBAGE};
	private static double WHEEL_RADIUS = 2.125, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST;
	
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
	
	private BluetoothConnection bt;
	private Transmission transmission;
	
	private FunctionType function = FunctionType.IDLE;
	private RobotMode mode = null;
	
	
	public static void main(String[] args) {
		new RobotController();
	}
	/**
	 * The robot controller delegates the starting and ending of various subtasks like localization,
	 * searching and collection.
	 */
	public RobotController(){
		new Map(mode);
		new LCDInfo();
		
		us = new USGather(usFront);
		cg = new ColorGather(csFront, csBack, csBlockReader);
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		nav = new Navigation(robo);
		corrector = new OdometryCorrection(cg, WHEEL_RADIUS, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST);
		odo = new Odometer(robo, corrector);
		
		collection = new CollectionSystem(clawMotor, nav);
		
		receive();
		
		this.start();
	}
	
	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		while(true){
			if(function == FunctionType.LOCALIZE)
				localize();
			else if(function == FunctionType.SEARCH)
				search(0, 90);
			else if(function == FunctionType.NAVIGATE)
				navigate();
			else if(function == FunctionType.IDENTIFY)
				identify();
			else if(function == FunctionType.COLLECT)
				collect();
			else if(function == FunctionType.RELEASE)
				release();
			
			
			try{
				Thread.sleep(50);
			}
			catch(InterruptedException e){
				
			}
		}
	}
	// Receives instruction via bluetooth
	private void receive(){
		bt = new BluetoothConnection();
		transmission = bt.getTransmission();
		mode = (transmission.role.equals(PlayerRole.BUILDER)) ? RobotMode.STACKER : RobotMode.GARBAGE;
	}
	// Initiates the localization of the robot
	private void localize(){
		loc = new Localization(usFront, csBack, csBack, csBack, transmission.startingCorner, robo);
		loc.localize();
	}
	// Search method (performs scans)
	private void search(double fromAngle, double toAngle){
		 nav.turnTo(fromAngle, 0);
		 while(!nav.isDone()){
		 	try{Thread.sleep(400);} catch(InterruptedException e){ }
		 }
		 nav.turnTo(toAngle, 0);
		 
		 Coordinates.scan(nav, us);

		 while(Coordinates.scanParsed()){
			 	try{Thread.sleep(400);} catch(InterruptedException e){ }
			 }
		 
		if(Map.hasNewWaypoint()){
			double[] wp = new double[]{0,0};
			Map.getWaypoint(wp);
			nav.travelTo(wp[0], wp[1]);
			
			function = FunctionType.NAVIGATE;
		}
	}
	// Handles navigating to a point (allows the scanner to continue in case an unexpected obstacle appears (i.e. the other player)
	private void navigate(){
		while(!nav.isDone()){
			// Keep the ultrasonic sensor scanning to detect obstacles
			// If an obstacle appears (probably the other player)
			// nav.stop();
		}
	}
	// Identifies a specific block
	private void identify(){
		
	}
	// Collects said block
	private void collect(){
		collection.collectBlock();
		
		while(!collection.isDone()){
			try{Thread.sleep(500);} catch(InterruptedException e){ }
		}
		
		function = FunctionType.SEARCH;
	}
	// Releases the entire stack (only done at the end of the match)
	private void release(){
		collection.releaseStack();
		
		while(!collection.isDone()){
			try{Thread.sleep(500);} catch(InterruptedException e){ }
		}
	}
	
}
