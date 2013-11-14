package robot.navigation;

import lejos.nxt.LCD;
import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 * The navigation class that is responsible for moving the robot from point A to point B and/or rotate by an angle T
 * 
 * @author Andreas
 * @version 1.0.1
 * @since 2013-11-04
 */
public class Navigation implements TimerListener {
	private final double FORWARD_SPEED = 10;
	private final double ROTATION_SPEED = 25;
	private final double ANGLE_ERROR_THRESH = 1;
	private final double DIST_ERROR_THRESH = 1.5;
	
	private TwoWheeledRobot robo;
	//will need getters and setters
	private double destinationX, destinationY, destinationT;
	private int turnDirection;
	
	private double[] pos = new double[3];
		
	private boolean done = true;
	private boolean traveling = false;
	private boolean turning = false;
	
	private Object lock = new Object();
	
	/**
	 * Requires the odometer as an input along with the two motors for each wheel.
	 * 
	 * @param odometer - Odometery for the robot
	 * @param leftMotor - Motor for the left wheel
	 * @param rightMotor - Motor for the right wheel
	 */
	public Navigation(TwoWheeledRobot robo) {
		this.robo = robo;
		
		Timer timer = new Timer(20, this);
		timer.start();
	}
	
	@Override
	public void timedOut() {
		if(done)
			return;
		
		Odometer.getPosition(pos);
		
		// Calculates the position and heading differences
		double dX = destinationX - pos[0];
		double dY = destinationY - pos[1];
		double dT = destinationT - pos[2];
		if(destinationT == -1)
			dT = getAngle(dX, dY) - pos[2];
		
		// Sets the robot to turn (first time through the method)
		if(Math.abs(dT) > ANGLE_ERROR_THRESH && !traveling && !turning){
			LCD.drawString("TURN1", 0, 5);
			turnBy(dT);
			turning = true;
		} 
		// Called for correcting the angle while the robot is moving.
		else if(Math.abs(dT) > (5 * ANGLE_ERROR_THRESH) && (dX > DIST_ERROR_THRESH * 2 || dY > DIST_ERROR_THRESH * 2) && traveling && !turning){
			LCD.drawString("TURN2", 0, 5);
			stop();
			turnBy(dT);
			turning = true;
			traveling = false;
		}
		// If angle is within threshold and it is turning but not moving, stop turning
		else if(Math.abs(dT) < ANGLE_ERROR_THRESH && !traveling && turning ){
			LCD.drawString("STOP1", 0, 5);
			turnBy(0);
			turning = false;
			turnDirection = 0;
		} 
		
		// If the robot not moving but is far from a point, start moving forward (will be called after robot turns)
		if((dX > DIST_ERROR_THRESH || dY > DIST_ERROR_THRESH) && !traveling && !turning){
			LCD.drawString("MOVE2", 0, 5);
			travel();
			traveling = true;
		}
		// If the robot is moving, not turning, and is within a distance from it's destination, stop moving
		else if(dX < DIST_ERROR_THRESH && dY < DIST_ERROR_THRESH && traveling && !turning){
			LCD.drawString("STOP2", 0, 5);
			stop();
			traveling = false;
		}
		
		LCD.drawString("                          ", 0, 6);
		LCD.drawString("                          ", 0, 7);
		
		LCD.drawString(turning + " " + traveling, 0, 6);
		LCD.drawString((int)dT + " " + (int)dX + " " + (int)dY, 0, 7);
		
	}
	// Travel to a point (this is called after the robot is oriented so only forward movement is necessary)
	private void travel(){
		robo.setForwardSpeed(FORWARD_SPEED);
	}
	// Turn by a specific amount
	private void turnBy(double theta){	
		if(turnDirection == 1){
			robo.setRotationSpeed(ROTATION_SPEED);
		}
		else if(turnDirection == 2){
			robo.setRotationSpeed(-ROTATION_SPEED);
		}
		else{
			if((theta > 0 && theta <= 180) || (theta > -360 && theta < -180))
				turnDirection = 1;
			else if((theta > 180 && theta < 360) || (theta >= -180 && theta < 0))
				turnDirection = 2;
			else{
				robo.setRotationSpeed(0);
				return;
			}

			turnBy(theta);
		}
		
	}
	// Calculate an angle by observing the distances in X and Y needed to be travelled by
	private double getAngle(double dX, double dY){
		double angle = Math.toDegrees(Math.atan2(dY, dX));
		if(angle < 0)
			angle += 360;
		else if(angle >= 360)
			angle -= 360;
		
		return angle;
	}
	
	
	/**
	 * Initiates the traveling to a particular point
	 * @param x - The destination x value
	 * @param y - The destination y value
	 */
	public void travelTo(double x, double y) {
		LCD.drawString("Travel", 0,4);
		
		destinationX = x;
		destinationY = y;
		destinationT = -1;
		turnDirection = 0;
		
		traveling = false;
		turning = false;
		
		synchronized(lock){
			done = false;
		}
	}
		
	/**
	 * Initiates the turning to a particular angle
	 * @param theta - The angle to turn to
	 * @param direction - The direction to turn 0:don't care; 1:clockwise; 2:counter clockwise
	 */
	public void turnTo(double theta, int direction) {
		Odometer.getPosition(pos);
		destinationX = pos[0];
		destinationY = pos[1];
		destinationT = theta;
		turnDirection = direction;
		
		traveling = false;
		turning = false;
		
		synchronized(lock){
			done = false;
		}
	}
	
	/**
	 * Full stop of all movement
	 * @return void
	 */
	public void stop() {				
		done = true;
		traveling = false;
		turning = false;
		
		robo.setForwardSpeed(0);
		robo.setRotationSpeed(0);
	}
	
	/**
	 * Continues with the current path (to be used mainly if the robot has had to stop to the other player
	 * and wants to continue to the same waypoint)
	 * @return void
	 */
	public void cont(){
		this.travelTo(destinationX, destinationY);
	}
	
	/**
	 * method used in collection to move forward after the claw has been opened
	 * @param dist - the distance to move forward
	 * @return void
	 */
	public void moveStraight(double dist) {
		double[] pos = new double[3];
		Odometer.getPosition(pos);
		double x = pos[0] + dist * Math.sin(pos[2]);
		double y = pos[1] + dist * Math.cos(pos[2]);
		travelTo(x,y);
	}
	
	/**
	 * Reverses the robot without specifying when to stop
	 */
	public void reverse(){
		robo.setForwardSpeed(-FORWARD_SPEED);
	}
	/**
	 * Determines if the navigation is actually running (if true, implies it is not)
	 * @return boolean: done
	 */
	public boolean isDone(){
		synchronized(lock){
			return done;
		}
	}
}
