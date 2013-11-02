package robot.base;
import robot.collection.*;
import robot.navigation.*;
import robot.sensors.*;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.ColorSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.LCD;
import lejos.util.TimerListener;
import lejos.nxt.SensorPort;

//could delegate to multiple controller classes instead...

public class RobotController {
	private static double WHEEL_RADIUS = 2.1, ODO_CORRECT_SENS_DIST;
	
	private static NXTRegulatedMotor leftMotor;
	private static NXTRegulatedMotor rightMotor;
	private static NXTRegulatedMotor frontSenMotor;
	private static NXTRegulatedMotor backSenMotor;
	private static NXTRegulatedMotor motor1;
	private static NXTRegulatedMotor motor2;
	private static NXTRegulatedMotor motor3;
	private static NXTRegulatedMotor motor4;
	private static UltrasonicSensor usFront;
	private static UltrasonicSensor usBack;
	private static ColorSensor csFront;
	private static ColorSensor csBack;
	private static ColorSensor csBlockReader;
	
	private static OdometryCorrection corrector;
	private static Odometer odo;
	private static Map map;
	private static LCDInfo lcd;
	private static SensorMotor _frontSenMotor;
	private static SensorMotor _backSenMotor;
	private static Navigation nav;
	private static USGather us;
	private static ColorGather cg;
	private static CollectionSystem collection;
	
	
	public static void main(String[] args) {
		new RobotController();
	}
	public RobotController(){
		_frontSenMotor = new SensorMotor(frontSenMotor);
		_backSenMotor = new SensorMotor(backSenMotor);
		
		us = new USGather(usFront, usBack, _frontSenMotor, _backSenMotor);
		cg = new ColorGather(csFront, csBack, csBlockReader);
		
		corrector = new OdometryCorrection(cg, WHEEL_RADIUS, ODO_CORRECT_SENS_DIST);
		odo = new Odometer(leftMotor, rightMotor, corrector);
		
		lcd = new LCDInfo(odo);
		nav = new Navigation(odo, leftMotor, rightMotor);
		collection = new CollectionSystem(motor1, motor2, motor3, motor4);
	}
	
}
