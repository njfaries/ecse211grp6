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
		Map.addBlock(50, 70);
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		new Odometer(robo);
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
		double[] pos = new double[3];
		
		LCD.drawString("go home", 0, 0);
		Map.buildEndWaypoints();

		double[] wp = new double[2];
		
		while(Map.hasNewWaypoint()){
			// Get new waypoint
			Map.getWaypoint(wp);
			
			LCD.drawInt(Map.getBlockCount(), 0, 1);
			LCD.drawInt(Map.getWaypointCount(), 0, 2);
			LCD.drawString((int)wp[0] + "|" + (int)wp[1], 0, 3);
			
			// Find new heading
			Odometer.getPosition(pos);
			double newHeading = Math.toDegrees(Math.atan2(wp[1] - pos[1], wp[0] - pos[0]));

			if(newHeading < 0)
				newHeading += 360;
			newHeading = newHeading % 360;
			
			// Turn
			nav.turnTo(newHeading, 0);
			while (!nav.isDone()) {
				try { Thread.sleep(400); } 
				catch (InterruptedException e) { }
			}
			nav.stop();
			
			// Find distance to travel
			double requiredDistance = Math.sqrt(Math.pow(wp[0] - pos[0],2) + Math.pow(wp[1] - pos[1],2));
			long startTime = System.currentTimeMillis();
			double distanceTravelled = 0;
			
			// Move distance
			nav.move();
			while (distanceTravelled < requiredDistance) {
				long elapsedTime = System.currentTimeMillis() - startTime;
				distanceTravelled = Odometer.getSpeed() * elapsedTime / 1000;

				try { Thread.sleep(50); } 
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

