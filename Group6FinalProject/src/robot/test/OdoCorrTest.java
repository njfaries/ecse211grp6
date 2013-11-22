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
 * Test class.
 * Uses mocked classes for the purpose of testing navigation.
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
		cg = new ColorGather(csLeft, csRight, csBlockReader);
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		corr = new OdometryCorrection(cg);
		new Odometer(robo, corr);
		nav = new Navigation2(robo);
		
		//new LCDInfo();
		
		this.start();
	}
	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		navigate();
		
		drawReadings();
		try{ Thread.sleep(1000); }
		catch(InterruptedException e){ }
	}
	// Handles navigating to a point (allows the scanner to continue in case an unexpected obstacle appears (i.e. the other player)
	private void navigate(){
		Odometer.getPosition(pos);
		double angle = wpIndex * 90;
		LCD.drawInt((int)angle,0,3);
		
		nav.turnTo(angle,0);
		while(!nav.isDone()){
			try{ Thread.sleep(100); }
			catch(InterruptedException e){ }
		}
		nav.stop();
		
		LCD.drawString("done Turn",0,4);
		drawReadings();
		
		nav.move();
		Odometer.runCorrection(true);
		int t=0;
		while(t < 20){
			Odometer.getPosition(pos);
			drawReadings();
			try{ Thread.sleep(200); }
			catch(InterruptedException e){ }
			t++;
		}
		nav.stop();
		Odometer.runCorrection(false);

		drawReadings();
		wpIndex++;
		if(wpIndex < waypoints.length)
			navigate();
	}
	private void drawReadings(){
		Odometer.getPosition(pos);
		LCD.drawString("X:                        ",0,0);
		LCD.drawString("Y:                        ",0,1);
		LCD.drawString("T                         ",0,2);
		
		LCD.drawString((int)pos[0] + "|" + waypoints[wpIndex][0],2,0);
		LCD.drawString((int)pos[1] + "|" + waypoints[wpIndex][1],2,1);
		LCD.drawInt((int)pos[2],2,2);
	}
}
