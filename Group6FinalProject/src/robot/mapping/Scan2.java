package robot.mapping;

import lejos.nxt.LCD;
import robot.navigation.Navigation2;
import robot.navigation.Odometer;
import robot.sensors.USGather;

/**
 * This class scans a range and returns an approximate center point for the first block detected
 * @author Andreas
 * @version 1.0
 * @since 2013-11-24
 */
public class Scan2 {
	private static final double DISTANCE_THRESHOLD = 50;
	private Navigation2 nav;
	private USGather us;
	
	public Scan2(Navigation2 nav, USGather us){
		this.nav = nav;
		this.us = us;
	}
	/**
	 * Finds and returns an array with the approximate center value of the first block detected.
	 * If no block detected in the scan, returns null
	 * @param scanAngle - The angle to scan to
	 * @param direction - The direction to scan (CW:1,CCW:-1)
	 */
	public double[] findBlock(double scanAngle, int direction){
		LCD.drawString("scanning", 0, 5);
		
		double currDist = 0;
		double currAngle = 0;
		
		// Creates empty array lists that data is added to
		double[] pos = new double[3];
		Odometer.getPosition(pos);
		
		// Performs a clockwise scan at an angle set by the input parameters
		nav.turnTo(pos[2] + scanAngle, 1);
		// Turn the scan amount to find a block
		int confidence = 0;
		
		currDist = us.getR() / 2;
		if(currDist < DISTANCE_THRESHOLD && currDist > 1)
			confidence = 5;
		while(!nav.isDone()){
			currDist = us.getR() / 2;
			
			if(currDist < DISTANCE_THRESHOLD && currDist > 1 && confidence >= 8){
				Odometer.getPosition(pos);
				//blockFound = true;
				currAngle = pos[2];
				//leftEdge = new double[]{edgeDist, currAngle};
				
				double adjustment = 20;
/*				if(currDist < 20)
					adjustment = 30;
				else if(currDist < 40)
					adjustment = 15;*/
				
				double adjustedAngle = currAngle + (direction * adjustment);
				if(adjustedAngle < 0)
					adjustedAngle += 360;
				
				double xValue = pos[0] + (currDist+5) * Math.cos(Math.toRadians(adjustedAngle));
				double yValue = pos[1] + (currDist+5) * Math.sin(Math.toRadians(adjustedAngle));
				
				if(Map.checkPoint(xValue, yValue)){
					LCD.setPixel((int)(xValue - pos[0]) + 64, (int)(yValue - pos[1]) + 32, 1);
					LCD.drawString("ALREADY CHECKED", 0,1);
					confidence = 0;
					continue;
				}
				else{
					LCD.drawString("                   ", 0,1);
				}
				nav.stop();
				
				confidence = 0;
				return new double[]{currDist, adjustedAngle};
				
			}
			else if(currDist < DISTANCE_THRESHOLD && currDist > 1)
				confidence++;
			
			try { Thread.sleep(50); } catch (InterruptedException e) {}
		}
		nav.stop();
		
		return null;
	}
}
