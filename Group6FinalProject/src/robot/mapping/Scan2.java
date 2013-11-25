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
		
		double[] leftEdge = null;	
		double[] rightEdge = null;
		boolean blockFound = false;
		double edgeDist = -1;
		double currDist = 0;
		double currAngle = 0;
		
		// Creates empty array lists that data is added to
		double[] pos = new double[3];
		Odometer.getPosition(pos);
		
		// Performs a clockwise scan at an angle set by the input parameters
		nav.turnTo(pos[2] + scanAngle, 1);
		// Turn the scan amount to find a block
		int confidence = 0;
		
		currDist = us.getFilteredData() / 2 + 7;
		if(currDist < DISTANCE_THRESHOLD && currDist > 1)
			confidence = 3;
		while(!nav.isDone()){
			currDist = us.getFilteredData() / 2 + 7;
			
			if(currDist < DISTANCE_THRESHOLD && currDist > 1 && confidence >= 5){
				Odometer.getPosition(pos);
				blockFound = true;
				currAngle = pos[2];
				leftEdge = new double[]{edgeDist, currAngle};
				
				double adjustment = 0;
				if(currDist < 20)
					adjustment = 40;
				else if(currDist < 30)
					adjustment = 27;
				else if(currDist < 40)
					adjustment = 16;
				else if(currDist < 50)
					adjustment = 5;
				
				double adjustedAngle = currAngle + (direction * adjustment);
				if(adjustedAngle < 0)
					adjustedAngle += 360;
				
				double xValue = pos[0] + currDist * Math.cos(Math.toRadians(adjustedAngle));
				double yValue = pos[1] + currDist * Math.sin(Math.toRadians(adjustedAngle));
				
				if(Map.checkPoint(xValue, yValue)){
					LCD.drawString("ALREADY CHECKED", 0,1);
					confidence = 0;
					continue;
				}
				nav.stop();
				
				return new double[]{currDist, adjustedAngle};
				
			}
			else if(currDist < DISTANCE_THRESHOLD && currDist > 1)
				confidence++;
			
			try { Thread.sleep(50); } catch (InterruptedException e) {}
		}
		nav.stop();
		
		return null;
			
		

/*		// If the search completed and did not pick up anything, return null
		// Otherwise, continue
		if(!blockFound)
			return null;
		
		// Turn clockwise to find the right falling edge
		nav.rotate(0);
		try { Thread.sleep(500); } catch (InterruptedException e) {}
		
		Odometer.getPosition(pos);
		double dist = us.getFilteredData();
		currAngle = pos[2];
		// Keep turning clockwise until the right falling edge is detected
		while(dist < edgeDist + 6 && dist > edgeDist - 6){
			Odometer.getPosition(pos);
			dist = us.getFilteredData();
			currAngle = pos[2];
			
			LCD.drawString(edgeDist + " " + dist + "              ", 0, 3);
			
			rightEdge = new double[]{edgeDist, currAngle};	
			try { Thread.sleep(50); } catch (InterruptedException e) {}
		}
		nav.stop();
		
		// Reverse to find the left falling edge
		nav.rotate(1);
		// Sleep to make sure the right corner isn't picked up a second time
		try { Thread.sleep(500); } catch (InterruptedException e) {}
		
		Odometer.getPosition(pos);
		dist = us.getFilteredData();
		currAngle = pos[2];
		// Keep turning Counter Clockwise to find the left falling edge
		while(dist < edgeDist + 6 && dist > edgeDist - 6){
			Odometer.getPosition(pos);
			dist = us.getFilteredData();
			currAngle = pos[2];
			
			leftEdge = new double[]{edgeDist, currAngle};
			try { Thread.sleep(50); } catch (InterruptedException e) {}
		}
		nav.stop();
		
		// Calculate the center of the block based on the rising/falling edges
		return new double[]{(leftEdge[0] + rightEdge[0]) / 2, findAverageTheta(leftEdge[1], rightEdge[1])};*/
	}
	private double findAverageTheta(double t1, double t2){
		LCD.drawString((int)t1 + " " + (int)t2, 0, 4);
		if(t1 > 270 && t2 < 90)
			t1 -= 360;
		else if(t1 < 90 && t2 > 270)
			t2 -= 360;
		
		return (t1 + t2) / 2;
	}
}
