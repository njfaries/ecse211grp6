package robot.navigation;

import robot.sensors.ColorGather;

public class OdometryCorrection {
	private static double WHEEL_RADIUS;
	private static double SENSOR_DISTANCE;
	
	private ColorGather cg;
	
	private long frontTime, backTime;
	private boolean updateAvailable, newFrontData;
	private double newAngle;
	
	public OdometryCorrection(ColorGather cg, double wheelRad, double sensorDist){
		WHEEL_RADIUS = wheelRad;
		SENSOR_DISTANCE = sensorDist;
		this.cg = cg;
	}
	public void update(double angle, double x, double y, double speed){
		long currTime = System.currentTimeMillis();
		
		if(cg.isOnLine(0) && !newFrontData){
			frontTime = currTime;
			newFrontData = true;
		}
		
		if(cg.isOnLine(1) && newFrontData){
			backTime = currTime;
			newFrontData = false;
			
			getNewAngle(angle, speed, frontTime, backTime);
		}
	}
	private void getNewAngle(double oldAngle, double speed, long time1, long time2){
		double secondDiff = (time2 - time1) / 1000.0;
		double distDeg = secondDiff * speed;
		double dist = (2 * Math.PI * WHEEL_RADIUS) * (distDeg / 360.0);
		
		// gets the angle to the line (will always return < 90)
		double baseAngle = Math.toDegrees(Math.atan(dist / SENSOR_DISTANCE));
		
		// Loops to find the most appropriate angle
		for(int i=0; i < 4; i++){
			if((newAngle + (i * 90)) > oldAngle - 15 && (newAngle + (i * 90)) < oldAngle + 15)
				this.newAngle = baseAngle + (i * 90);
		}		
		
	}
	
	public boolean isUpdateAvailable(){
		return updateAvailable;
	}
	public double getNewTheta(){
		updateAvailable = false;
	
		return newAngle;
	}
}
