package robot.test;
import robot.base.LCDInfo;
import robot.collection.*;
import robot.navigation.*;
import robot.sensors.*;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

/**
 * Test class.
 * Uses mocked classes for the purpose of testing collection.
 * 
 * @author Andreas, Nathaniel
 * @version 1.2
 */
public class CollectionTest extends Thread{
	public enum FunctionType { IDLE, RECEIVE, LOCALIZE, SEARCH, IDENTIFY, NAVIGATE, COLLECT, RELEASE };
		
	private NXTRegulatedMotor leftMotor = Motor.A;
	private NXTRegulatedMotor rightMotor = Motor.B;
	private NXTRegulatedMotor clawMotor = Motor.C;
	
	private static UltrasonicSensor usFront = new UltrasonicSensor(SensorPort.S4);
	
	private static ColorSensor csLeft = new ColorSensor(SensorPort.S1);
	private static ColorSensor csRight = new ColorSensor(SensorPort.S2);
	private static ColorSensor csBlockReader = new ColorSensor(SensorPort.S3);
	
	private TwoWheeledRobot robo;
	private USGather us;
	private ColorGather cg;
	private CollectionSystem collection;
	private Navigation2 nav;
	
	private static FunctionType function = FunctionType.COLLECT;
	
	int distanceIndex = 0;
	int blockCount = 1;
	
	public static void main(String[] args) {
		new CollectionTest();
	}
	public CollectionTest(){		
		us = new USGather(usFront);
		cg = new ColorGather(csLeft, csRight, csBlockReader);
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		new Odometer(robo, null);
		nav = new Navigation2(robo);
		
		collection = new CollectionSystem(clawMotor, nav);
		collection.rotateCage(-330);
		
		new LCDInfo();
		this.start();
	}
	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		while(true){
			if(function == FunctionType.COLLECT)
				if (blockCount == 0)
					collectFirstBlock();
				else
					collect();
			
			try{
				Thread.sleep(50);
			}
			catch(InterruptedException e){
				
			}
		}
	}
	// Collects said block
	private void collectFirstBlock(){
		collection.lowerCage();
		collection.openCage();
		
		nav.move();
		try { Thread.sleep(2000); } 
		catch (InterruptedException e) {}
		nav.stop();
		
		try { Thread.sleep(250); } 
		catch (InterruptedException e) {}
		
		collection.closeCage();
		collection.raiseCage();

		function = FunctionType.IDLE;
	}
	
	private void collect() {
		alignBlock();
		collection.lowerCage();
		collection.openCage();
		nav.move();
		try {Thread.sleep(500);}
		catch (InterruptedException e) {}
		nav.stop();
		collection.closeCage();
		collection.raiseCage();
		
		function = FunctionType.IDLE;
	}
	
	private void alignBlock() {
		nav.move();
		try {Thread.sleep(3000);} catch(InterruptedException e) {}
		nav.stop();
	}
}

