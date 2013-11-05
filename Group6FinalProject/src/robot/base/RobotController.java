package robot.base;
import robot.collection.*;
import robot.navigation.*;
import robot.sensors.*;

import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.ColorSensor;
import lejos.nxt.UltrasonicSensor;

/**
 * Contains the main method for the robot.
 * Initiates classes and passes them the necessary motors, sensors, and various constants.
 * 
 * @author Andreas
 * @version 1.1.0
 * @since 2013-11-04
 */
public class RobotController extends Thread{
	private static double WHEEL_RADIUS = 2.125, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST;
	
	private NXTRegulatedMotor leftMotor;
	private NXTRegulatedMotor rightMotor;
	private NXTRegulatedMotor motor1;
	private NXTRegulatedMotor motor2;
	private NXTRegulatedMotor motor3;
	private NXTRegulatedMotor motor4;
	
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
	
	public static void main(String[] args) {
		new RobotController();
	}
	public RobotController(){
		new Map();
		new LCDInfo();
		
		us = new USGather(usFront);
		cg = new ColorGather(csFront, csBack, csBlockReader);
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		nav = new Navigation(robo);
		corrector = new OdometryCorrection(cg, WHEEL_RADIUS, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST);
		odo = new Odometer(robo, corrector);
		
		collection = new CollectionSystem(motor1, motor2, motor3, motor4);
		
		this.start();
	}
	
	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		while(true){
			
			
			try{
				Thread.sleep(10);
			}
			catch(InterruptedException e){
				
			}
		}
	}
	private void localize(){
		
	}
	private void search(){
		if(Map.hasNewWaypoint()){
			double[] wp = new double[]{0,0};
			Map.getWaypoint(wp);
			nav.travelTo(wp[0], wp[1]);
		}
	}
	private void navigate(){
		while(!nav.isDone()){
			// Keep the ultrasonic sensor scanning to detect obstacles
			// If an obstacle appears (probably the other player)
			// nav.stop();
		}
	}
	private void identify(){
		
	}
	
}
