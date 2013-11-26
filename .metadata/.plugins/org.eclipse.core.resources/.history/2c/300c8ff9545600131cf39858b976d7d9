package robot.test;

import java.util.ArrayList;

import robot.base.DemoController.FunctionType;
import robot.base.RobotController.RobotMode;
import robot.bluetooth.PlayerRole;
import robot.collection.*;
import robot.mapping.Block;
import robot.mapping.Map;
import robot.mapping.Scan;
import robot.navigation.*;
import robot.sensors.*;
import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

/**
 * Contains the main method for the robot.
 * Initiates classes and passes them the necessary motors, sensors, and various constants.
 * Controls and and delegates tasks to various subroutines.
 * 
 * @author Andreas
 * @version 1.1.0
 * @since 2013-11-04
 */
public class ScnIdColTest extends Thread{	
	private NXTRegulatedMotor leftMotor = Motor.A;
	private NXTRegulatedMotor rightMotor = Motor.B;
	private NXTRegulatedMotor cageMotor = Motor.C;
	
	private UltrasonicSensor usFront = new UltrasonicSensor(SensorPort.S4);
		
	private static ColorSensor csLeft = new ColorSensor(SensorPort.S1);
	private static ColorSensor csRight = new ColorSensor(SensorPort.S2);
	private static ColorSensor csBlockReader = new ColorSensor(SensorPort.S3);
	
	private Navigation2 nav;
	private TwoWheeledRobot robo;
	
	private ColorGather cg;
	private USGather us;
	
	private Identify id;
	CollectionSystem collection; 
			
	private FunctionType function = FunctionType.SEARCH;
	private RobotMode mode = RobotMode.STACKER;
	
	double[] pos = new double[3];
	
	public static void main(String[] args) {
		new ScnIdColTest();
	}
	/**
	 * The scanning test tests the ability for the robot to scan its surroundings and find blocks.
	 * This involves running the Scan() while rotating using Navigation.turnTo(x,y)
	 */
	public ScnIdColTest(){
		new Map(PlayerRole.BUILDER, new int[]{-1,-1,0,0}, new int[]{60,60,90,90});
		
		collection = new CollectionSystem(cageMotor, nav);
		collection.rotateCage(-330);
		
		us = new USGather(usFront);
		cg = new ColorGather(csLeft, csRight, csBlockReader, new OdometryCorrection());
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		nav = new Navigation2(robo);
		new Odometer(robo);
		
		id = new Identify(cg, us, nav);
		
		//new LCDInfo();
		
		this.start();
	}
	
	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		while(true){
			LCD.clear();
			if(function == FunctionType.SEARCH)
				search(0,90);
			else if(function == FunctionType.BLOCK_NAVIGATE)
				navigateToBlock();	
			else if(function == FunctionType.END_NAVIGATE)
				navigateToEnd();
			else if (function == FunctionType.POINT_NAVIGATE)
				navigateToNextPoint();
			else if(function == FunctionType.IDENTIFY)
				identify();			
			else if(function == FunctionType.COLLECT)
				collect();	
			else if(function == FunctionType.RELEASE)
				release();	
			
			try{ Thread.sleep(500); }
			catch(InterruptedException e){ }
		}
	}
	// Search method (performs scans)
	private void search(double fromAngle, double toAngle){
		LCD.drawString("search", 0, 4);
		//0 as to turn to with smallest angle before starting the scan
		nav.turnTo(fromAngle, 0);
		while (!nav.isDone()) {
			try { Thread.sleep(400); } 
			catch (InterruptedException e) { }
		}
		nav.turnTo(toAngle, 1);

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
			function = FunctionType.IDLE;
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
		
		while (us.getRawDistance() - 14 > 20) {
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
	private void navigateToNextPoint() {
		LCD.drawString("navigate2", 0, 4);
		
		Odometer.getPosition(pos);
		nav.turnTo(45, 0);

		while (!nav.isDone()) {
			try { Thread.sleep(400); } 
			catch (InterruptedException e) { }
		}
		nav.stop();
		
		nav.move();
		try { Thread.sleep(2500); } 
		catch (InterruptedException e) { }
		nav.stop();
		
		function = FunctionType.SEARCH;
	}
	// Identifies a block
	private void identify(){		
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
	// Collects said block
	private void collect(){
		LCD.drawString("collect", 0,4);
		collection.lowerCage();
		collection.openCage();
		
		nav.move();
		try { Thread.sleep(2500); } 
		catch (InterruptedException e) { }
		nav.stop();
		
		try { Thread.sleep(250); } 
		catch (InterruptedException e) { }
		
		collection.rotateCage(-500);

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
