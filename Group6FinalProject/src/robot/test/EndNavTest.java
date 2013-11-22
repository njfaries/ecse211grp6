package robot.test;

import robot.bluetooth.PlayerRole;
import robot.mapping.Map;
import robot.navigation.*;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

/**
 * Test class.
 * Uses mocked classes for the purpose of testing navigation. 
 * 
 * @author Andreas
 * @version 1.1.0
 * @since 2013-11-09
 */
public class EndNavTest extends Thread{	
	
	private NXTRegulatedMotor leftMotor = Motor.A;
	private NXTRegulatedMotor rightMotor = Motor.B;
	
	private TwoWheeledRobot robo;
	Navigation2 nav;
	
	 // This controls which points the navigation travels to
	int[] endZone = new int[]{120,90,150,120};
	int wpIndex = 0;
	
	public static void main(String[] args) {
		new EndNavTest();
	}
	public EndNavTest(){
		new Map(PlayerRole.BUILDER, new int[]{0,0,0,0}, endZone);
		Map.addBlock(new double[]{50,50,50,50}, new double[]{70,70,70,70});
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		new Odometer(robo, null);
		nav = new Navigation2(robo);
		
		//new LCDInfo();
		
		this.start();
	}
	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		navigateToEnd();
		
		try { Thread.sleep(1000); } 
		catch (InterruptedException e) { }
	}
	// Handles the navigation to the end
	private void navigateToEnd() {		
		LCD.drawString("go home", 0, 0);
		Map.buildEndWaypoints();

		double[] wp = new double[2];
		
		while(Map.hasNewWaypoint()){
			Map.getWaypoint(wp);
			
			LCD.drawInt(Map.getBlockCount(), 0, 1);
			LCD.drawInt(Map.getWaypointCount(), 0, 2);
			LCD.drawString((int)wp[0] + "|" + (int)wp[1], 0, 3);
			
			nav.travelTo(wp[0], wp[1]);
			
			while (!nav.isDone()) {
				try { Thread.sleep(400); } 
				catch (InterruptedException e) { }
			}
			
			nav.stop();
			Map.waypointReached();
		}
		LCD.drawInt(Map.getBlockCount(), 0, 1);
		LCD.drawInt(Map.getWaypointCount(), 0, 2);
		LCD.drawString((int)wp[0] + "|" + (int)wp[1], 0, 3);
	}
}

