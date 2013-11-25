package robot.navigation;
import robot.sensors.ColorGather;
import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 * Keeps up-to-date information about the location and heading of the robot.
 * 
 * @author Andreas
 * @version 2.0.0
 * @since 2013-11-03
 */
public class Odometer implements TimerListener {
	
	public static final int DEFAULT_PERIOD = 20;
	
	private static TwoWheeledRobot robo;
	
	// position data
	private static double x, y, theta;
	private double[] oldDH = new double[2], dDH = new double[2];
	
	// lock object for mutual exclusion
	private static Object lock;

	/**
	 * Odometer constructor. Allows for the keeping of up-to-date information about the robot's current postion and heading
	 * 
	 * @param leftMotor - The motor for the left wheel
	 * @param rightMotor - The motor for the right wheel
	 * @param corrector - The Odometry Correction being used
	 */
	public Odometer(TwoWheeledRobot twoWheelRobo) {
		x = 30.0;
		y = 30.0;
		
		theta = 0;
		lock = new Object();
		
		robo = twoWheelRobo;
				
		Timer timer = new Timer(DEFAULT_PERIOD, this);
		timer.start();
	}

	@Override
	public void timedOut() {
		robo.getDisplacementAndHeading(dDH);
		dDH[0] -= oldDH[0];
		dDH[1] -= oldDH[1];
		
		// update the position in a critical region
		synchronized (lock) {
			theta += dDH[1];
			theta = fixDegAngle(theta);
			
			x += dDH[0] * Math.cos(Math.toRadians(theta));
			y += dDH[0] * Math.sin(Math.toRadians(theta));
		}
		
		oldDH[0] += dDH[0];
		oldDH[1] += dDH[1];	
	}

	// Getters 
	/**
	 * Updates an array of the position and heading of the robot with the up-to-date information.
	 * Updates the parameter.
	 * @param position - Array of position and heading of the robot x:0 y:1 t:2
	 */
	public static void getPosition(double[] position) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			position[0] = x;
			position[1] = y;
			position[2] = theta;
		}
	}
	/**
	 * Returns the TwoWheeledRobot class used by the Odometer and Navigation classes
	 * @return TwoWheeledRobot robo
	 */
	public TwoWheeledRobot getTwoWheeledRobot() {
		return robo;
	}
	
	// Setters
	/**
	 * Updates the position and heading of the robot with the values contained in the 'position' parameter.
	 * Only updates the value if the corresponding index in 'update' is true.
	 * 
	 * @param position - values containing the updated position and heading x:0 y:1 t:2
	 * @param update - values of whether or not to update the respective value x:0 y:1 t:2
	 */
	public static void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}
	
	// static 'helper' methods
	private double fixDegAngle(double angle) {		
		if (angle < 0.0)
			angle = 360.0 + (angle % 360.0);
		
		return angle % 360.0;
	}
	
	public static double requiredHeading(double xf, double yf) {
		//helper method that returns the calculated required theta in degrees
		double t = 0;
		//finding change in x and y coords
		synchronized(lock) {
			double deltaX = xf - x;
			double deltaY = yf - y;
			t = Math.toDegrees(Math.atan2(deltaY,deltaX));
		}
		
		//return the change in theta
		return t;
	}	
	public static double getLeftWheelSpeed(){
		return robo.getLeftWheelSpeed();
	}
	public static double getRightWheelSpeed(){
		return robo.getRightWheelSpeed();
	}
}
