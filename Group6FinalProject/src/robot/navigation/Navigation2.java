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
	private final double ROTATION_SPEED = 35;
	private final double ANGLE_ERROR_THRESH = 2;
	private final double DISTANCE_THRESH = 3;
	
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
	public Navigation2(TwoWheeledRobot robo) {
		this.robo = robo;
		
		Timer timer = new Timer(25, this);
		timer.start();
	}
	
	@Override
	public void timedOut() {
		if(done){
			turning = false;
			traveling = false;
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
		double distance = Math.sqrt(dX * dX + dY * dY);
		
		if(Math.abs(dT) < ANGLE_ERROR_THRESH){
			turning = false;
			turnDirection = 0;
		}
		
		// Checks if traveling is necessary
		if(distance < DISTANCE_THRESH)
			traveling = false;
		
		// Movement methods called here
		if(turning)
			turnBy(dT);
		else if(traveling)
			move();
		else
			stop();
		
	}
	
	// Turn by a specific amount
	private void turnBy(double theta){
		if(turnDirection == 1){
			robo.turn(0);
		}
		else if(turnDirection == 2){
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
	
	/**
	 * Initiates the traveling to a particular point
	 * @param x - The destination x value
	 * @param y - The destination y value
	 */
	public void travelTo(double x, double y) {
		Odometer.getPosition(pos);		
		destinationX = x;
		destinationY = y;
		
		double heading = Math.toDegrees(Math.atan2(pos[1] - y, pos[0] - x));
		double headingError = pos[2] - heading;
		if(headingError < 360)
			headingError += 360;
		headingError = headingError % 360;
		
		if(headingError > ANGLE_ERROR_THRESH){
			destinationT = heading;
			turnDirection = 0;
			turning = true;
		}
		
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
		synchronized(lock){
			done = true;
			robo.stopMotor();
			
			traveling = false;
			turning = false;
		}
                                           
	}
	
	public void move(){
		synchronized(lock){
			robo.goForward();
		}
	}
	public void reverse(){
		synchronized(lock){
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

