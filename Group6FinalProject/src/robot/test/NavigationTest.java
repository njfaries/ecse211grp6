package robot.test;
import robot.base.LCDInfo;
import robot.base.RobotController.FunctionType;
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
 * Uses mocked classes for the purpose of testing navigation.
 * 
 * @author Andreas
 * @version 1.0
 */
public class NavigationTest {
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
	private static Navigation nav;
	
	private static FunctionType function = FunctionType.NAVIGATE;
	
	int[] distances = new int[]{0,1,2,3};
	int distanceIndex = 0;
	
	public static void main(String[] args) {
		new NavigationTest();
	}
	public NavigationTest(){

		usFront = mock(UltrasonicSensor.class);
		when(usFront.getDistance()).thenReturn(getNextDist());
		
		us = new USGather(usFront);
		cg = new ColorGather(csFront, csBack, csBlockReader);
		map = new Map();
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		corrector = new OdometryCorrection(cg, WHEEL_RADIUS, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST);
		odo = new Odometer(robo, corrector);
		nav = new Navigation(robo);
		
		new LCDInfo();
	}
	public int getNextDist(){
		distanceIndex++;
		if(distanceIndex > 3)
			distanceIndex = 0;
		return distances[distanceIndex];
	}

	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		while(true){
			
			if(function == FunctionType.SEARCH)
				search();
			if(function == FunctionType.NAVIGATE)
				navigate();
			
			
			try{
				Thread.sleep(50);
			}
			catch(InterruptedException e){
				
			}
		}
	}
	// Search method (performs scans)
	private void search(){
		// Perform scan
		
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
			// Keep the ultrasonic sensor updating to detect obstacles
			// If an obstacle appears (probably the other player)
			// nav.stop();
		}
		
		function = FunctionType.IDLE;
	}
}

