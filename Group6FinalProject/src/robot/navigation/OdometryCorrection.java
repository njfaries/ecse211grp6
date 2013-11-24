package robot.navigation;

import lejos.nxt.LCD;
import robot.sensors.ColorGather;

/**
 * Calculates the current position and heading of the robot whenever it crosses a gridline. Requires both light sensors
 * to cross the gridline before being able to calculate values.
 * 
 * @author Andreas
 * @version 1.0
 */
public class OdometryCorrection {
	private static double WHEEL_RADIUS = 2.125;
	private static double WHEEL_SPEED = 269;
	private static double SENSOR_WIDTH = 7.9;
	private static double SENSOR_DISTANCE = 7.5;
	private static double ANGLE_THRESHOLD = 40;
	
	private long leftTime, rightTime;
	private boolean updateX, updateY, updateT, newLeftData, newRightData;
	private double newX, newY, newT, dist;
	
	/**
	 * In order to correct the position and heading, the OdometryCorrection class must use information gathered from
	 * the ColorGather class in order to determine when it has crossed a line. It also requires constants like the
	 * radius of the wheels, the distance between the two sensors and the distance from the sensors to robot's axis 
	 * of rotation
	 * 
	 * @param colorGather - The ColorGather class being used
	 * @param wheelRad - The radius of the wheels (constant)
	 * @param sensorWidth - The distance between the center of the two light sensor (constant)
	 * @param sensorDist - The distance from the two light sensors to the robot's axis of rotation
	 */
	public OdometryCorrection(){
		updateX = false;
		updateY = false;
		updateT = false;
		
		newLeftData = false;
		newRightData = false;
	}
	
	/**
	 * Provides updated postion and heading data. Updates the 'data' array passed with 
	 * (if possible to calculate) new data. Allows for the calculation of more accurate
	 * values for the robot's position and heading when the robot crosses a gridline.
	 * Passes x, y, and theta back to the caller.
	 * 
	 * @param data - The array containing current x, y, and theta values. This array will be updated with corrected values if available.
	 * @param speed - Forward unsigned speed of the robot (to be used in calculations)
	 */
	public void update(int sensor, long time){
		double pos[] = new double[3];
		// Check if the left sensor is over a line and was not over a line on the previous iteration
		// Record the time that the sensor crosses
		if(sensor == 0 && !newLeftData){
			leftTime = time;
			newLeftData = true;
		}
		// Repeat for right sensor
		if(sensor == 1 && !newRightData){
			rightTime = time;
			newRightData = true;
		}
		
		// If neither sensor is on the line line and there is recorded data for both sensors. Reset
		if(sensor == -1 && newRightData && newLeftData){
			newRightData = false;
			newLeftData = false;
		}
		// Otherwise if there is recorded data for both sensors and one is over the line 
		else if(newRightData && newLeftData){
			Odometer.getPosition(pos);
			LCD.drawString("both on line",0,7);
			getNewAngle(pos[2], rightTime, leftTime);
		}
		else if(sensor == -1)
			return;
		
		// If there is an update to to, get the new position
		if(updateT){	
			// Will set updateX, updateY, updateT to false in the case of a risky update
			//LCD.drawString("update postion        ",0,7);
			//getNewPosition(pos[0], pos[1]);	
		}
		else
			return;
		
		Odometer.setPosition(new double[]{newX, newY, newT}, new boolean[]{updateX, updateY, updateT});
		updateX = false;
		updateY = false;
		updateT = false;
	}
	
	// Gets the new angle based on the time between the crossing of both sensors, 
	// the current speed, and the current approximate heading
	private void getNewAngle(double oldAngle, long time1, long time2){
		dist = 0;
		double timeDiff = Math.abs(time2 - time1);
		double distDeg = timeDiff * WHEEL_SPEED / 1000.0;
		this.dist = (2 * Math.PI * WHEEL_RADIUS) * (distDeg / 360.0);
		
		// gets the angle to the line (will always return < 90)
		double baseAngle = Math.toDegrees(Math.atan2(dist, SENSOR_WIDTH));
		
		newT = -1;
		// Loops to find the most appropriate angle
		for(int i=0; i < 4; i++){
			if((baseAngle + (i * 90)) > oldAngle - ANGLE_THRESHOLD && (baseAngle + (i * 90)) < oldAngle + ANGLE_THRESHOLD)
				this.newT = baseAngle + (i * 90);
		}		
		LCD.drawString((int)timeDiff + " " + (int)dist + " " + (int)baseAngle + "                    ", 0, 7);
		
		if(newT >= 0)
			updateT = true;
		else
			updateT = false;
		
	}
	
	// Approximates the current position by determining if the robot is close to any particular
	// gridline at recorded time of crossing. If two candidates exist, the position is not updated
	private void getNewPosition(double oldX, double oldY){		
		double adjustedX = oldX + (dist + SENSOR_DISTANCE) * Math.cos(Math.toRadians(newT));
		double adjustedY = oldY + (dist + SENSOR_DISTANCE) * Math.sin(Math.toRadians(newT));
		
		// Find if the error could be corrected to an x line
		double lineErrorX = adjustedX % 30.48;
		if(lineErrorX < 5 || lineErrorX > 25){
			double lineDistX = Math.round(adjustedX / 30.48);
			newX = (lineDistX * 30.48) + (Math.abs(dist) + SENSOR_DISTANCE) * Math.cos(Math.toDegrees(newT));
			updateX = true;
			LCD.drawString((int)newX + "", 0, 5);
		}
		
		// Find if the error can be corrected with respect to a y line
		double lineErrorY = adjustedY % 30.48;
		if(lineErrorY < 5 || lineErrorY > 25){
			double lineDistY = Math.round(adjustedY / 30.48);
			newY = (lineDistY * 30.48) + (Math.abs(dist) + SENSOR_DISTANCE) * Math.sin(Math.toDegrees(newT));
			updateY = true;
			LCD.drawString((int)newY + "", 0, 6);
		}
		
		LCD.drawString((int)lineErrorX + "|" + (int)lineErrorY,0,4);
		// don't risk a bad update (will be true if the robot is close to multiple gridlines)
		if(updateX && updateY){
			updateX = false;
			updateY = false;
			updateT = false;
		}
	}
}
