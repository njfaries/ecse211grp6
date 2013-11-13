package robot.test;

import robot.base.RobotController.RobotMode;
import robot.collection.*;
import robot.mapping.Map;
import robot.mapping.Scan;
import robot.navigation.*;
import robot.sensors.*;
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
public class ScanningTest extends Thread{
	public enum FunctionType { IDLE, RECEIVE, LOCALIZE, SEARCH, IDENTIFY, NAVIGATE, COLLECT, RELEASE };
	
	private NXTRegulatedMotor leftMotor = Motor.A;
	private NXTRegulatedMotor rightMotor = Motor.B;
	private NXTRegulatedMotor cageMotor = Motor.C;
	
	private UltrasonicSensor usFront = new UltrasonicSensor(SensorPort.S4);
		
	private Navigation nav;
	private TwoWheeledRobot robo;
	
	private USGather us;
	
	private FunctionType function = FunctionType.SEARCH;
	private RobotMode mode = RobotMode.STACKER;
	
	public static void main(String[] args) {
		new ScanningTest();
	}
	/**
	 * The scanning test tests the ability for the robot to scan its surroundings and find blocks.
	 * This involves running the Scan() while rotating using Navigation.turnTo(x,y)
	 */
	public ScanningTest(){
		new Map(mode);
		
		CollectionSystem collect = new CollectionSystem(cageMotor, nav);
		collect.raiseCage();
		
		us = new USGather(usFront);
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		nav = new Navigation(robo);
		new Odometer(robo);
		
		//new LCDInfo();
		
		this.start();
	}
	
	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		while(true){
			if(function == FunctionType.SEARCH)
				search(325,125);
			
			try{ Thread.sleep(50); }
			catch(InterruptedException e){ }
		}
	}
	// Search method (performs scans)
	private void search(double fromAngle, double toAngle){
		nav.turnTo(fromAngle, 0);

		while(!nav.isDone()){
			try{Thread.sleep(400);} catch(InterruptedException e){ }
		}
		
		nav.turnTo(toAngle, 1);
		 
/*		while(!nav.isDone()){
			try{Thread.sleep(400);} catch(InterruptedException e){ }
		}*/
		
		new Scan(nav, us);

		while(!Scan.scanParsed()){
		 	try{Thread.sleep(400);} catch(InterruptedException e){ }
		}

		function = FunctionType.IDLE;
	}
	
}
