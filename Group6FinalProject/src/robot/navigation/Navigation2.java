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
public class Navigation2 extends Navigation implements TimerListener {
	private final double FORWARD_SPEED = 10;
	private final double ROTATION_SPEED = 35;
	private final double ANGLE_ERROR_THRESH = 2;
	private final double DIST_ERROR_THRESH = 2;
	
	private TwoWheeledRobot robo;
	//will need getters and setters
	private double destinationX, destinationY, destinationT;
	private int turnDirection;
	
	private double[] pos = new double[3];
		
	private boolean done = true;
	private boolean traveling = false;
	private boolean turning = false;
	
	
	/**
	 * Requires the odometer as an input along with the two motors for each wheel.
	 * 
	 * @param odometer - Odometery for the robot
	 * @param leftMotor - Motor for the left wheel
	 * @param rightMotor - Motor for the right wheel
	 */
	public Navigation2(TwoWheeledRobot robo) {
		super(robo);
		this.robo = robo;
		
		Timer timer = new Timer(25, this);
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
		
		// Checks if rotation is necessary
		if(Math.abs(dT) > ANGLE_ERROR_THRESH)
			turning = true;
/*		else if(Math.abs(dT) > ANGLE_ERROR_THRESH && (Math.abs(dX) > DIST_ERROR_THRESH*2 || Math.abs(dY) > DIST_ERROR_THRESH*2))
			turning = true;*/
		else{
			turning = false;
			turnDirection = 0;
		}
		// Checks if traveling is necessary
		if(Math.abs(dX) > DIST_ERROR_THRESH || Math.abs(dY) > DIST_ERROR_THRESH)
			traveling = true;
		else
			traveling = false;
		
		// Movement methods called here
		if(turning){
			Odometer.runCorrection(false);
			turnBy(dT);
		}
		else if(traveling){
			Odometer.runCorrection(true);
			travel();
		}
		else{
			Odometer.runCorrection(false);
			stop();
		}
		
		LCD.drawString("                                          ", 0, 5);
		LCD.drawString(done + "|" + turning + "|" + traveling, 0, 5);
		LCD.drawString("                                          ", 0, 6);
		LCD.drawString((int)dX + "|" + (int)dY + "|" +(int)dT, 0, 6);
	}
	// Travel to a point (this is called after the robot is oriented so only forward movement is necessary)
	private void travel(){
		robo.setRotationSpeed(0);
		robo.setForwardSpeed(FORWARD_SPEED);
	}
	// Turn by a specific amount
	private void turnBy(double theta){
		robo.setForwardSpeed(0);
		
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
			else
				return;

			turnBy(theta);
		}
		
	}
	// Calculate an angle by observing the distances in X and Y needed to be travelled by
	private double getAngle(double dX, double dY){
		double angle = Math.toDegrees(Math.atan2(dY, dX));

		if (angle < 0.0)
			angle = 360.0 + (angle % 360.0);
		
		return angle % 360.0;
	}
	
	
	/**
	 * Initiates the traveling to a particular point
	 * @param x - The destination x value
	 * @param y - The destination y value
	 */
	public void travelTo(double x, double y) {
		destinationX = x;
		destinationY = y;
		destinationT = -1;
		
		traveling = true;
		done = false;
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
		
		turning = true;
		done = false;
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
		double x = pos[0] + dist * Math.cos(pos[2]);
		double y = pos[1] + dist * Math.sin(pos[2]);
		travelTo(x,y);
	}
	
	public void move(){
		robo.setForwardSpeed(FORWARD_SPEED);
		robo.goForward();
	}
	public void reverse(){
		robo.setForwardSpeed(-FORWARD_SPEED);
		robo.goBackward();
	}
	
	/**
	 * Determines if the navigation is actually running (if true, implies it is not)
	 * @return boolean: done
	 */
	public boolean isDone(){
		return done;
	}
}

