package robot.navigation;

import lejos.nxt.NXTRegulatedMotor;

/**
 * The two wheeled robot class. Deals with movement of the wheels and measuring their displacement.
 * 
 * @author Andreas
 * @version 1.0.0
 * @since 2013-11-03
 */
public class TwoWheeledRobot {
	public static final double DEFAULT_LEFT_RADIUS = 2.125;
	public static final double DEFAULT_RIGHT_RADIUS = 2.125;
	public static final double DEFAULT_WIDTH = 22.0;
	public static final int MOTOR_SPEED = 169;
	private NXTRegulatedMotor leftMotor, rightMotor;
	private double leftRadius, rightRadius, width;
	private double forwardSpeed = 0, rotationSpeed = 0;
	
	private Object lock = new Object();
	
	public TwoWheeledRobot(NXTRegulatedMotor leftMotor,
						   NXTRegulatedMotor rightMotor,
						   double width,
						   double leftRadius,
						   double rightRadius) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;
	}
	/**
	 * Initiates the two wheeled robot with the left and right wheel motors.
	 * 
	 * @param leftMotor - left wheel motor
	 * @param rightMotor - right wheel motor
	 */
	public TwoWheeledRobot(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor) {
		this(leftMotor, rightMotor, DEFAULT_WIDTH, DEFAULT_LEFT_RADIUS, DEFAULT_RIGHT_RADIUS);
	}
	public TwoWheeledRobot(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor, double width) {
		this(leftMotor, rightMotor, width, DEFAULT_LEFT_RADIUS, DEFAULT_RIGHT_RADIUS);
	}
	
	// accessors
	public double getDisplacement() {
		return (leftMotor.getTachoCount() * leftRadius +
				rightMotor.getTachoCount() * rightRadius) *
				Math.PI / 360.0;
	}
	
	public double getHeading() {
		return (leftMotor.getTachoCount() * leftRadius -
				rightMotor.getTachoCount() * rightRadius) / width;
	}
	/**
	 * Get the displacement and heading of the robot and returns through the parameter.
	 * 
	 * @param data - Updates this array with displacement and heading data 0:disp 1:head
	 */
	public void getDisplacementAndHeading(double [] data) {
		int leftTacho, rightTacho;
		
		leftTacho = leftMotor.getTachoCount();
		rightTacho = rightMotor.getTachoCount();
		
		data[0] = (leftTacho * leftRadius + rightTacho * rightRadius) *	Math.PI / 360.0;
		data[1] = (leftTacho * leftRadius - rightTacho * rightRadius) / width;
	}
	// Get the speeds of the right and left wheels.
	public double getLeftWheelSpeed(){
		return leftMotor.getRotationSpeed();
	}
	public double getRightWheelSpeed(){
		return rightMotor.getRotationSpeed();
	}
	// mutators
	public void turn(int direction){
		synchronized(lock){
			leftMotor.setSpeed(MOTOR_SPEED);
			rightMotor.setSpeed(MOTOR_SPEED);
			
			if(direction == 0){
				leftMotor.forward();
				rightMotor.backward();
			}
			else{
				leftMotor.backward();
				rightMotor.forward();
			}
		}
	}
	public void goForward(){
		synchronized(lock){
			leftMotor.setSpeed(MOTOR_SPEED);
			rightMotor.setSpeed(MOTOR_SPEED);
			
			rightMotor.forward();
			leftMotor.forward();
		}
	}
	public void goBackward(){
		synchronized(lock){
			leftMotor.setSpeed(MOTOR_SPEED);
			rightMotor.setSpeed(MOTOR_SPEED);
			
			
			rightMotor.backward();
			leftMotor.backward();
		}
	}
	public void stopMotor(){
		synchronized(lock){
			rightMotor.stop();
			leftMotor.stop();
		}
	}
}
