package robot.test;
import robot.base.*;
import robot.localization.*;
import robot.navigation.*;
import lejos.nxt.ColorSensor;
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
	
	private static NXTRegulatedMotor leftMotor;
	private static NXTRegulatedMotor rightMotor;
	private static UltrasonicSensor us = new UltrasonicSensor(SensorPort.S1);
	private static ColorSensor csLeft = new ColorSensor(SensorPort.S2);
	private static ColorSensor csRight = new ColorSensor(SensorPort.S3);
	private static ColorSensor csBlock = new ColorSensor(SensorPort.S4);
	
	private static TwoWheeledRobot robo;
	private static Localization loc;
	
	public static void main(String args[]) {
		new LocalizationTest();
	}
	
	public LocalizationTest() {
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		loc = new Localization(us, csLeft, csRight, csBlock, Localization.Corner.ONE, robo);
		
		new LCDInfo();
	}
	
	public void run() {
		localize();
	}
	
	public void localize() {
		loc.start();
		
	}

}
