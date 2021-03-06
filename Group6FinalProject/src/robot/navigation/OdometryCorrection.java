package robot.navigation;

import robot.sensors.ColorGather;

/**
 * Calculates the current position and heading of the robot whenever it crosses a gridline. Requires both light sensors
 * to cross the gridline before being able to calculate values.
 * 
 * @author Andreas
 * @version 1.0
 */
public class OdometryCorrection {
	private static double WHEEL_RADIUS;
	private static double SENSOR_WIDTH;
	private static double SENSOR_DISTANCE;
	
	private ColorGather cg;
	
	private long leftTime, rightTime;
	private boolean updateX, updateY, updateT, newLeftData, newRightData;
	private double newX, newY, newT;
	
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
	public OdometryCorrection(ColorGather colorGather, double wheelRad, double sensorWidth, double sensorDist){
		WHEEL_RADIUS = wheelRad;
		SENSOR_WIDTH = sensorWidth;
		SENSOR_DISTANCE = sensorDist;
		
		this.cg = colorGather;
		updateX = false;
		updateY = false;
		updateT = false;
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
	public void update(double[] data, double speed){
		long currTime = System.currentTimeMillis();
		
		if(cg.isOnLine(0) && !newLeftData){
			leftTime = currTime;
			newLeftData = true;
			
			if(newRightData){
				getNewAngle(data[2], speed, leftTime, rightTime);
				if(updateT)
					getNewPosition(data[0], data[1]);
			}
		}
		if(cg.isOnLine(1) && !newRightData){
			rightTime = currTime;
			newRightData = true;
			
			if(newLeftData){
				getNewAngle(data[2], speed, rightTime, leftTime);
				if(updateT)
					getNewPosition(data[0], data[1]);
			}
		}
		if(!cg.isOnLine(0) && !cg.isOnLine(1) && newRightData && newLeftData){
			newRightData = false;
			newLeftData = false;
		}
		
		if(updateX)
			data[0] = this.newX;
		if(updateY)
			data[1] = this.newY;
		if(updateT)
			data[2] = this.newT;
	}
	
	// Gets the new angle based on the time between the crossing of both sensors, 
	// the current speed, and the current approximate heading
	private void getNewAngle(double oldAngle, double speed, long time1, long time2){
		double secondDiff = time2 - time1 / 1000.0;
		double distDeg = secondDiff * speed;
		double dist = (2 * Math.PI * WHEEL_RADIUS) * (distDeg / 360.0);
		
		// gets the angle to the line (will always return < 90)
		double baseAngle = Math.toDegrees(Math.atan(dist / SENSOR_WIDTH));
		
		newT = -1;
		// Loops to find the most appropriate angle
		for(int i=0; i < 4; i++){
			if((baseAngle + (i * 90)) > oldAngle - 15 && (baseAngle + (i * 90)) < oldAngle + 15)
				this.newT = baseAngle + (i * 90);
		}		
		
		if(newT >= 0)
			updateT = true;
		else
			updateT = false;
	}
	
	// Approximates the current position by determining if the robot is close to any particular
	// gridline at recorded time of crossing. If two candidates exist, the position is not updated
	private void getNewPosition(double oldX, double oldY){
		double adjustedX = oldX + SENSOR_DISTANCE * Math.sin(Math.toDegrees(newT));
		double adjustedY = oldY + SENSOR_DISTANCE * Math.cos(Math.toDegrees(newT));
		
		double lineDistX = adjustedX % 30;
		if(lineDistX > 15)
			lineDistX -= 30;
		
		double lineDistY = adjustedY % 30;
		if(lineDistY > 15)
			lineDistY -= 30;
		
		if((lineDistX > 2 && lineDistX < 2) && (lineDistY > 2 && lineDistY < 2)){
			newX = oldX;
			updateX = false;
			
			newY = oldY;
			updateY = false;
		}
		else if(lineDistX > 2 && lineDistX < 2){
			newX = adjustedX - lineDistX;
			updateX = true;
		}
		else if(lineDistY > 2 && lineDistY < 2){
			newY = adjustedY - lineDistY;
			updateY = true;
		}
	}
}
