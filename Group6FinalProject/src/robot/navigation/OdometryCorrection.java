package robot.navigation;

import robot.sensors.ColorGather;

public class OdometryCorrection {
	private static double WHEEL_RADIUS;
	private static double SENSOR_WIDTH;
	private static double SENSOR_DISTANCE;
	
	private ColorGather cg;
	
	private long leftTime, rightTime;
	private boolean updateX, updateY, updateT, newLeftData, newRightData;
	private double newX, newY, newT;
	
	public OdometryCorrection(ColorGather cg, double wheelRad, double sensorWidth, double sensorDist){
		WHEEL_RADIUS = wheelRad;
		SENSOR_WIDTH = sensorWidth;
		SENSOR_DISTANCE = sensorDist;
		
		this.cg = cg;
		updateX = false;
		updateY = false;
		updateT = false;
		
	}
	/**
	 * Updates the odometry correction class. Allows for the calculation of more accurate
	 * values for the robot's position and heading when the robot crosses a gridline.
	 * Passes x, y, and theta back to the caller.
	 * 
	 * @param theta - angle at which the robot is facing (value to be corrected)
	 * @param x - current recorded x value (to be corrected)
	 * @param y - current recorded y value (to be corrected)
	 * @param speed - forward unsigned speed of the robot (to be used in calculations)
	 */
	public void update(double theta, double x, double y, double speed){
		long currTime = System.currentTimeMillis();
		
		if(cg.isOnLine(0) && !newLeftData){
			leftTime = currTime;
			newLeftData = true;
			
			if(newRightData){
				getNewAngle(theta, speed, leftTime, rightTime);
				if(updateT)
					getNewPosition(x, y);
			}
		}
		if(cg.isOnLine(1) && !newRightData){
			rightTime = currTime;
			newRightData = true;
			
			if(newLeftData){
				getNewAngle(theta, speed, rightTime, leftTime);
				if(updateT)
					getNewPosition(x, y);
			}
		}
		if(!cg.isOnLine(0) && !cg.isOnLine(1) && newRightData && newLeftData){
			newRightData = false;
			newLeftData = false;
		}
		
		if(updateX)
			x = this.newX;
		if(updateY)
			y = this.newY;
		if(updateT)
			theta = this.newT;
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
	
	private void getNewPosition(double oldX, double oldY){
		double adjustedX = oldX + SENSOR_DISTANCE * Math.sin(Math.toDegrees(newT));
		double adjustedY = oldY + SENSOR_DISTANCE * Math.cos(Math.toDegrees(newT));
		
		double lineDistX = adjustedX % 30;
		if(lineDistX > 15)
			lineDistX -= 30;
		
		double lineDistY = adjustedY % 30;
		if(lineDistY > 15)
			lineDistY -= 30;
		
		if(lineDistX > 2 && lineDistX < 2){
			newX = adjustedX - lineDistX;
			updateX = true;
		}
		if(lineDistY > 2 && lineDistY < 2){
			newY = adjustedY - lineDistY;
			updateY = true;
		}
	}
}
