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

	private static Odometer odo;
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
	private static Map map;
	
	private static LCDInfo lcd = new LCDInfo(odo);
	private static SensorMotor _frontSenMotor = new SensorMotor(frontSenMotor);
	private static SensorMotor _backSenMotor = new SensorMotor(backSenMotor);
	private static Navigation nav = new Navigation(odo, leftMotor, rightMotor);
	private static USGather us = new USGather(odo, usFront, usBack, _frontSenMotor, _backSenMotor);
	private static ColorGather cg = new ColorGather(csFront, csBack, csBlockReader);
	private static CollectionSystem collection = new CollectionSystem(motor1, motor2, motor3, motor4);
	
	
	public static void main(String[] args) {
		
		
	}
	
}
