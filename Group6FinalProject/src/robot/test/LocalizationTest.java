package robot.test;

import robot.bluetooth.StartCorner;
import robot.localization.*;
import robot.navigation.*;
import robot.sensors.ColorGather;
import robot.sensors.USGather;
import lejos.nxt.Button;
import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

/**
 * Test class.  Uses mocked classes to test localization
 * 
 * @author Nathaniel
 * @version 1.0.0
 * @since 2013-11-09
 *
 */

public class LocalizationTest {
	
	private static NXTRegulatedMotor leftMotor = new NXTRegulatedMotor(MotorPort.A);
	private static NXTRegulatedMotor rightMotor = new NXTRegulatedMotor(MotorPort.B);
	private static UltrasonicSensor us = new UltrasonicSensor(SensorPort.S4);
	private static UltrasonicSensor usTop = new UltrasonicSensor(SensorPort.S3);
	
	private static ColorSensor csLeft = new ColorSensor(SensorPort.S1);
	private static ColorSensor csRight = new ColorSensor(SensorPort.S2);
	
	
	private static TwoWheeledRobot robo;
	private static Localization loc;
	private double[] pos = new double[3];
	
	public static void main(String args[]) {
		Motor.C.rotate(-330);
		int buttonChoice;
		LCD.drawString("Running...", 0, 0);
		buttonChoice = Button.waitForAnyPress();
		while (buttonChoice != Button.ID_ENTER){}
		LCD.drawString("Localizing...", 0, 1);
		new LocalizationTest();
		LCD.drawString("Done", 0, 2);
		buttonChoice = Button.waitForAnyPress();
		while (buttonChoice != Button.ID_ESCAPE) {}
	}
	
	public LocalizationTest() {
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		new Odometer(robo);
		Navigation2 nav = new Navigation2(robo);
		ColorGather cg = new ColorGather(csLeft, csRight, new OdometryCorrection());
		USGather usg = new USGather(us, usTop);
		
		loc = new Localization(usg, cg, StartCorner.BOTTOM_LEFT, nav);
		
		
		//new LCDInfo();
		//loc.usLocalization();
		loc.localize();
		
		Odometer.getPosition(pos);
		LCD.drawString("x:" + (int)pos[0] + "y:" + (int)pos[1],0,0);
		LCD.drawString("t:" + pos[2],0,1);
		//this.start();
	}
}
