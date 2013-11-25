package robot.test;

import robot.base.LCDInfo;
import robot.navigation.*;
import robot.sensors.ColorGather;
import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;

/**
 * Test class for ododmetery correction
 * Place in the vicinity of 45, 45 and let it run over the first line an a slightly off angle.
 * 
 * @author Andreas
 * @version 1.1.0
 * @since 2013-11-09
 */
public class OdoCorrTest extends Thread{	
	
	private static ColorSensor csLeft = new ColorSensor(SensorPort.S1);
	private static ColorSensor csRight = new ColorSensor(SensorPort.S2);
	private static ColorSensor csBlockReader = new ColorSensor(SensorPort.S3);
	
	private NXTRegulatedMotor leftMotor = Motor.A;
	private NXTRegulatedMotor rightMotor = Motor.B;
	
	private TwoWheeledRobot robo;
	Navigation2 nav;
	OdometryCorrection corr;
	private ColorGather cg;
	
	 // This controls which points the navigation travels to
	double[][] waypoints = new double[][]{{105,45},{105,105},{45,105},{45,45}};
	double[] pos = new double[3];
	int wpIndex = 0;
	
	public static void main(String[] args) {
		new OdoCorrTest();
	}
	public OdoCorrTest(){
		corr = new OdometryCorrection();
		cg = new ColorGather(csLeft, csRight, csBlockReader, corr);
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		
		new Odometer(robo);
		nav = new Navigation2(robo);
		
		//new LCDInfo();
		
		this.start();
	}
	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		navigate();
		
		drawReadings();
		
		while(true){
			try{ Thread.sleep(2000); }
			catch(InterruptedException e){ }
		}
	}
	// Handles navigating to a point (allows the scanner to continue in case an unexpected obstacle appears (i.e. the other player)
	private void navigate(){
		// GOTO 135,45
		nav.move();
		try{ Thread.sleep(6000); }
		catch(InterruptedException e){ }
		drawReadings();
		nav.stop();
		
		Odometer.getPosition(pos);
		
		nav.turnTo(90,0);
		while(!nav.isDone()){
			try{ Thread.sleep(500); }
			catch(InterruptedException e){ }
		}
		drawReadings();
		nav.stop();
		
		// GOTO 135, 135
		nav.move();
		try{ Thread.sleep(6000); }
		catch(InterruptedException e){ }
		drawReadings();
		nav.stop();
		
		Odometer.getPosition(pos);
		
		nav.turnTo(180,0);
		while(!nav.isDone()){
			try{ Thread.sleep(500); }
			catch(InterruptedException e){ }
		}
		drawReadings();
		nav.stop();
		
		// GOTO 45,135
		nav.move();
		try{ Thread.sleep(6000); }
		catch(InterruptedException e){ }
		drawReadings();
		nav.stop();
		
		Odometer.getPosition(pos);
		
		nav.turnTo(270,0);
		while(!nav.isDone()){
			try{ Thread.sleep(500); }
			catch(InterruptedException e){ }
		}
		drawReadings();
		nav.stop();
		
		// GOTO 45,45
		nav.move();
		try{ Thread.sleep(6000); }
		catch(InterruptedException e){ }
		drawReadings();
		nav.stop();
		
	}
	private void drawReadings(){
		Odometer.getPosition(pos);
		LCD.drawString("X:                        ",0,0);
		LCD.drawString("Y:                        ",0,1);
		LCD.drawString("T                         ",0,2);
		
		LCD.drawInt((int)pos[0],2,0);
		LCD.drawInt((int)pos[1],2,1);
		LCD.drawInt((int)pos[2],2,2);
	}
}

