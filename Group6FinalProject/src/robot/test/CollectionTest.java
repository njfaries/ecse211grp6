package robot.test;
import robot.base.LCDInfo;
import robot.base.RobotController.RobotMode;
import robot.collection.*;
import robot.mapping.Map;
import robot.navigation.*;
import robot.sensors.*;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.ColorSensor;
import lejos.nxt.UltrasonicSensor;
import static org.mockito.Mockito.*;

/**
 * Test class.
 * Uses mocked classes for the purpose of testing collection.
 * 
 * @author Andreas
 * @version 1.0
 */
public class CollectionTest {
	public enum FunctionType { IDLE, RECEIVE, LOCALIZE, SEARCH, IDENTIFY, NAVIGATE, COLLECT, RELEASE };
	
	private static double WHEEL_RADIUS = 2.125, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST;
	
	private static NXTRegulatedMotor leftMotor;
	private static NXTRegulatedMotor rightMotor;
	private static NXTRegulatedMotor motor1;
	
	private static UltrasonicSensor usFront;
	private static ColorSensor csFront;
	private static ColorSensor csBack;
	private static ColorSensor csBlockReader;
	
	private static OdometryCorrection corrector;
	private static TwoWheeledRobot robo;
	private static USGather us;
	private static ColorGather cg;
	private static CollectionSystem collection;
	private static Navigation nav;
	
	private static FunctionType function = FunctionType.IDLE;
	
	int[] distances = new int[]{0,1,2,3};
	int distanceIndex = 0;
	
	public static void main(String[] args) {
		new CollectionTest();
	}
	public CollectionTest(){
		new Map(RobotMode.STACKER);
		
		usFront = mock(UltrasonicSensor.class);
		when(usFront.getDistance()).thenReturn(getNextDist());
		
		us = new USGather(usFront);
		cg = new ColorGather(csFront, csBack, csBlockReader);
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		corrector = new OdometryCorrection(cg, WHEEL_RADIUS, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST);
		new Odometer(robo, corrector);
		nav = new Navigation(robo);
		
		new LCDInfo();
	}
	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		while(true){
			if(function == FunctionType.COLLECT)
				collect();			
			
			try{
				Thread.sleep(50);
			}
			catch(InterruptedException e){
				
			}
		}
	}
	// Collects said block
	private void collect(){
		collection.collectBlock();
		
		while(!collection.isDone()){
			try{Thread.sleep(500);} catch(InterruptedException e){ }
		}
		
		function = FunctionType.IDLE;
	}
	
	public int getNextDist(){
		distanceIndex++;
		if(distanceIndex > 3)
			distanceIndex = 0;
		return distances[distanceIndex];
	}
}

