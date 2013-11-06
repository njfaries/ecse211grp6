package robot.test;
import robot.base.LCDInfo;
import robot.collection.*;
import robot.navigation.*;
import robot.sensors.*;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

import static org.mockito.Mockito.*;

/**
 * Test class.
 * Uses mocked sensors and motors
 * 
 * @author Andreas
 * @version 1.0
 */
public class TestController {
	private static double WHEEL_RADIUS = 2.125, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST;
	
	private static NXTRegulatedMotor leftMotor;
	private static NXTRegulatedMotor rightMotor;
	private static NXTRegulatedMotor motor1;
	
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
	
	int[] distances = new int[]{0,1,2,3};
	int distanceIndex = 0;
	
	public static void main(String[] args) {
		new TestController();
	}
	public TestController(){

		usFront = mock(UltrasonicSensor.class);
		when(usFront.getDistance()).thenReturn(getNextDist());
		
		us = new USGather(usFront);
		cg = new ColorGather(csFront, csBack, csBlockReader);
		map = new Map();
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		corrector = new OdometryCorrection(cg, WHEEL_RADIUS, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST);
		odo = new Odometer(robo, corrector);
		
		new LCDInfo();
	}
	public int getNextDist(){
		distanceIndex++;
		if(distanceIndex > 3)
			distanceIndex = 0;
		return distances[distanceIndex];
	}
}

