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
 * @version 1.0
 */
public class RobotController {
	private static double WHEEL_RADIUS = 2.125, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST;
	
	private static NXTRegulatedMotor leftMotor;
	private static NXTRegulatedMotor rightMotor;
	private static NXTRegulatedMotor motor1;
	private static NXTRegulatedMotor motor2;
	private static NXTRegulatedMotor motor3;
	private static NXTRegulatedMotor motor4;
	private static UltrasonicSensor usFront;
	private static ColorSensor csFront;
	private static ColorSensor csBack;
	private static ColorSensor csBlockReader;
	
	private static OdometryCorrection corrector;
	private static Odometer odo;
	private static Map map;
	private static TwoWheeledRobot robo;
	private static USGather us;
	private static ColorGather cg;
	private static CollectionSystem collection;
	
	
	public static void main(String[] args) {
		new RobotController();
	}
	public RobotController(){		
		us = new USGather(usFront);
		cg = new ColorGather(csFront, csBack, csBlockReader);
		map = new Map();
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		corrector = new OdometryCorrection(cg, WHEEL_RADIUS, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST);
		odo = new Odometer(robo, corrector);
		
		new LCDInfo(odo);
		collection = new CollectionSystem(motor1, motor2, motor3, motor4);
	}
	
}
