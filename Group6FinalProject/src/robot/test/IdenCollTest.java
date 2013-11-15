package robot.test;
import robot.base.LCDInfo;
import robot.base.DemoNoBlue.FunctionType;
import robot.base.RobotController.RobotMode;
import robot.collection.*;
import robot.mapping.Map;
import robot.navigation.*;
import robot.sensors.*;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

/**
 * Test class.
 * Uses mocked classes for the purpose of testing collection.
 * 
 * @author Andreas
 * @version 1.0
 */
public class IdenCollTest extends Thread{
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
	private Identify id;
	private Navigation2 nav;
	
	private static FunctionType function = FunctionType.IDENTIFY;
	
	int distanceIndex = 0;
	
	public static void main(String[] args) {
		new IdenCollTest();
	}
	public IdenCollTest(){		
		us = new USGather(usFront);
		cg = new ColorGather(csLeft, csRight, csBlockReader);
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		new Odometer(robo, null);
		nav = new Navigation2(robo);
		
		id = new Identify(cg, us, nav);
		
		collection = new CollectionSystem(clawMotor, nav);
		collection.rotateCage(-330);
		
		new LCDInfo();
		this.start();
	}
	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		while(true){
			LCD.drawString("                    ", 0,7);
		
			if(function == FunctionType.IDENTIFY)
				identify();			
			else if(function == FunctionType.COLLECT)
				collect();		
			
			try{
				Thread.sleep(50);
			}
			catch(InterruptedException e){
				
			}
		}
	}
	// Collects said block
	private void identify(){		
		// if the block is blue collect it
		
		if (id.isBlue()) {
			LCD.drawString("blue", 0,7);
			function = FunctionType.COLLECT;
		}
		
		// else the robot has backed up and does a search
		else {
			LCD.drawString("not blue", 0,7);
			function = FunctionType.IDLE;
		}	
	}
	// Collects said block
	private void collect(){
		LCD.drawString("collecting", 0,7);
		collection.lowerCage();
		collection.openCage();
		
		nav.move();
		try { Thread.sleep(2500); } 
		catch (InterruptedException e) { }
		nav.stop();
		
		try { Thread.sleep(250); } 
		catch (InterruptedException e) { }
		
		collection.closeCage();
		collection.raiseCage();

		function = FunctionType.IDLE;
	}
}

