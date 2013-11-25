package robot.test;

import robot.collection.*;
import robot.mapping.Scan2;
import robot.navigation.Navigation2;
import robot.navigation.Odometer;
import robot.navigation.TwoWheeledRobot;
import robot.sensors.*;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

/**
 * Contains the main method for the robot.
 * Initiates a scan of type Scan2 to find and approach the first available block
 * 
 * @author Andreas
 * @version 1.0.0
 * @since 2013-11-24
 */
public class Scanning2Test extends Thread{
	private NXTRegulatedMotor leftMotor = Motor.A;
	private NXTRegulatedMotor rightMotor = Motor.B;
	private NXTRegulatedMotor cageMotor = Motor.C;
	
	private UltrasonicSensor usFront = new UltrasonicSensor(SensorPort.S4);
	
	private Navigation2 nav;
	private TwoWheeledRobot robo;
	
	private USGather us;
	private Scan2 scan;
	
	private double[] pos = new double[3];
	
	public static void main(String[] args) {
		new Scanning2Test();
	}
	/**
	 * The scanning test tests the ability for the robot to scan its surroundings and find blocks.
	 * This involves running the Scan() while rotating using Navigation.turnTo(x,y)
	 */
	public Scanning2Test(){
		//new Map(PlayerRole.BUILDER, new int[]{150,150,180,180}, new int[]{-1,-1,0,0});
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		new Odometer(robo);
		nav = new Navigation2(robo);
		
		us = new USGather(usFront);
		
		CollectionSystem collect = new CollectionSystem(cageMotor, nav);
		collect.rotateCage(-300);
		
		scan = new Scan2(nav, us);
		
		this.start();
	}
	
	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		while(true){
			search(0,90);
			
			try{ Thread.sleep(500); }
			catch(InterruptedException e){ }
		}		
	}
	// Search method (performs scans)
	private void search(double fromAngle, double toAngle){
		LCD.drawString("Searching",0,0);
		
		nav.turnTo(fromAngle, 0);
		while(!nav.isDone()){
			try{Thread.sleep(400);} catch(InterruptedException e){ }
			
		}
		nav.stop();
		
		double[] newBlock = scan.findBlock(toAngle, 1);
		
		if(newBlock == null)
			return;
		
		Odometer.getPosition(pos);
		double angleHeading = Math.toDegrees(Math.atan2(newBlock[1] - pos[1], newBlock[0] - pos[0]));
		
		nav.turnTo(angleHeading, 0);
		while(!nav.isDone()){
			try{Thread.sleep(400);} catch(InterruptedException e){ }
			
		}
		nav.stop();
		
		nav.move();
		while(!us.flagObstruction()){
			try{Thread.sleep(20);} catch(InterruptedException e){ }
		}
		nav.stop();
	}
	
}
