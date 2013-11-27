package robot.navigation;

import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 * The navigation class that is responsible for moving the robot from point A to point B and/or rotate by an angle T
 * 
 * @author Andreas
 * @version 1.0.1
 * @since 2013-11-04
 */
public class Navigation2 implements TimerListener {
	private final double FORWARD_SPEED = 10;
	private final double ROTATION_SPEED = 40;
	private final double TURNING_ANGLE_ERROR_THRESH = 2;
	private final double MOVING_ANGLE_ERROR_THRESH = 5;
	private final double DIST_ERROR_THRESH = 2;
	
	private TwoWheeledRobot robo;
	//will need getters and setters
	private double destinationX, destinationY, destinationT, startX, startY;
	private double angleError = TURNING_ANGLE_ERROR_THRESH;
	
	private int turnDirection;
	
	private double[] pos = new double[3];
		
	private boolean done = true;
	private boolean traveling = false;
	private boolean turning = false;
	private boolean onlyTurn = false;
	private Object lock = new Object();
	
	/**
	 * Requires the odometer as an input along with the two motors for each wheel.
	 * 
	 * @param odometer - Odometery for the robot
	 * @param leftMotor - Motor for the left wheel
	 * @param rightMotor - Motor for the right wheel
	 */
	public Navigation2(TwoWheeledRobot robo) {
		this.robo = robo;
		
		Timer timer = new Timer(25, this);
		timer.start();
	}
	
	@Override
	public void timedOut() {
		if(done){
			turnDirection = 0;
			//robo.stopMotor();
			return;
		}
		else{
			robo.goForward();
		}
		
		Odometer.getPosition(pos);
		
		// Calculates the position and heading differences
		double dX = destinationX - pos[0];
		double dY = destinationY - pos[1];
		double dT = destinationT - pos[2];
		if(destinationT == -1)
			dT = getAngle() - pos[2];
		
		if(Math.abs(dT) > angleError)
			turning = true;
		else{
			turning = false;
			turnDirection = 0;
		}
		// Checks if traveling is necessary
		if(Math.abs(dX) > DIST_ERROR_THRESH || Math.abs(dY) > DIST_ERROR_THRESH){
			turnDirection = 0;
			traveling = true;
		}
		else{
			if(!onlyTurn)
				turning = false;
			traveling = false;
		}
		
		// Movement methods called here
		if(turning){
			turnBy(dT);
		}
		else if(traveling){
			travel();
		}
		else{
			done = true;
		}
		
	}
	// Travel to a point (this is called after the robot is oriented so only forward movement is necessary)
	private void travel(){
		robo.setSpeeds(FORWARD_SPEED, 0);
		robo.goForward();
	}
	// Turn by a specific amount
	private void turnBy(double theta){
		if(turnDirection == 1){
			robo.setSpeeds(0, ROTATION_SPEED);
			robo.turn(0);
		}
		else if(turnDirection == 2){
			robo.setSpeeds(0, -ROTATION_SPEED);
			robo.turn(1);
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
	private double getAngle(){
		double angle = Math.toDegrees(Math.atan2((destinationY - startY), (destinationX - startX)));

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
		robo.setSpeeds(0,0);
		robo.goForward();
		
		Odometer.getPosition(pos);
		startX = pos[0];
		startY = pos[1];
		
		destinationX = x;
		destinationY = y;
		destinationT = -1;
		
		traveling = true;
		onlyTurn = false;
		
		synchronized(lock){
			done = false;
		}
		
		angleError = MOVING_ANGLE_ERROR_THRESH;
	}
		
	/**
	 * Initiates the turning to a particular angle
	 * @param theta - The angle to turn to
	 * @param direction - The direction to turn 0:don't care; 1:clockwise; 2:counter clockwise
	 */
	public void turnTo(double theta, int direction) {
		robo.setSpeeds(0,0);
		robo.goForward();
		
		Odometer.getPosition(pos);
		startX = pos[0];
		startY = pos[1];
		destinationX = pos[0];
		destinationY = pos[1];
		destinationT = theta;
		turnDirection = direction;
		
		turning = true;
		onlyTurn = true;
		
		synchronized(lock){
			done = false;
		}
		
		angleError = TURNING_ANGLE_ERROR_THRESH;
	}
	
	/**
	 * Full stop of all movement
	 * @return void
	 */
	public void stop() {				
		done = true;
		robo.setSpeeds(0,0);
		robo.stopMotor();
		
		traveling = false;
		turning = false;                                               
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
		synchronized(lock){
			robo.setSpeeds(FORWARD_SPEED, 0);
			robo.goForward();
		}
	}
	public void reverse(){
		synchronized(lock){
			robo.setSpeeds(-FORWARD_SPEED, 0);
			robo.goBackward();
		}
		
	}
	/**
	 * CW - 0
	 * CCW - 1
	 * @param direction
	 */
	public void rotate(int direction, double speed){
		double rotationSpeed = ROTATION_SPEED;
		if(speed > 0)
			rotationSpeed = speed;
		synchronized(lock){
			if(direction == 0)
				robo.setSpeeds(0, rotationSpeed);
			else
				robo.setSpeeds(0, -rotationSpeed);
		
			robo.turn(direction);
		}
		
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
