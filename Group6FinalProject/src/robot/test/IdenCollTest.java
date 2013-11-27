package robot.test;
import robot.base.LCDInfo;
import robot.collection.*;
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
	private static UltrasonicSensor usTop = new UltrasonicSensor(SensorPort.S3);	
	
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
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		new Odometer(robo);
		nav = new Navigation2(robo);
		
		collection = new CollectionSystem(clawMotor, nav);
		collection.rotateCage(-330);
		us = new USGather(usFront, usTop);
		
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
		nav.move();
		while(!us.flagObstruction()){
			try { Thread.sleep(50); } 
			catch (InterruptedException e) {}
		}
		// While an obstruction is flagged and no block is seen, continue forward (may return instantly)
		while(us.getZType() == USGather.HeightCategory.FLOOR && us.flagObstruction()){
			try { Thread.sleep(50); } 
			catch (InterruptedException e) {}
		}
		nav.stop();
		
		function = FunctionType.COLLECT;
	}
	// Collects said block
	private void collect(){
		nav.reverse();
		try { Thread.sleep(500); } 
		catch (InterruptedException e) {}
		nav.stop();
		
		collection.lowerCage();
		collection.openCage();
		
		nav.move();
		try { Thread.sleep(3000); } 
		catch (InterruptedException e) {}
		nav.stop();
		
		try { Thread.sleep(250); } 
		catch (InterruptedException e) {}
		
		collection.closeCage();
		collection.raiseCage();
		
		function = FunctionType.IDLE;
	}
}

