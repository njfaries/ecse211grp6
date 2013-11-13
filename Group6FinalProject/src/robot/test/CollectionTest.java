package robot.test;
import robot.base.LCDInfo;
import robot.base.RobotController.RobotMode;
import robot.collection.*;
import robot.mapping.Map;
import robot.navigation.*;
import robot.sensors.*;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.ColorSensor;
import lejos.nxt.UltrasonicSensor;

/**
 * Test class.
 * Uses mocked classes for the purpose of testing collection.
 * 
 * @author Andreas
 * @version 1.0
 */
public class CollectionTest {
	public enum FunctionType { IDLE, RECEIVE, LOCALIZE, SEARCH, IDENTIFY, NAVIGATE, COLLECT, RELEASE };
		
	private NXTRegulatedMotor leftMotor = Motor.A;
	private NXTRegulatedMotor rightMotor = Motor.B;
	private NXTRegulatedMotor clawMotor = Motor.C;
	
	private UltrasonicSensor usFront;
	private ColorSensor csFront;
	private ColorSensor csBack;
	private ColorSensor csBlockReader;
	
	private TwoWheeledRobot robo;
	private USGather us;
	private ColorGather cg;
	private CollectionSystem collection;
	private Navigation nav;
	
	private static FunctionType function = FunctionType.IDLE;
	
	int[] distances = new int[]{0,1,2,3};
	int distanceIndex = 0;
	
	public static void main(String[] args) {
		new CollectionTest();
	}
	public CollectionTest(){
		new Map(RobotMode.STACKER);
		
		us = new USGather(usFront);
		cg = new ColorGather(csFront, csBack, csBlockReader);
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		new Odometer(robo);
		nav = new Navigation(robo);
		
		collection = new CollectionSystem(clawMotor, nav);
		
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

