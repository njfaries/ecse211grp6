package robot.navigation;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 * Keeps up-to-date information about the location and heading of the robot.
 * 
 * @version 1.0
 * @author Andreas
 *
 */
public class Odometer implements TimerListener {
	
	public static final int DEFAULT_PERIOD = 20;
	
	private TwoWheeledRobot robo;
	private Navigation nav;
	private OdometryCorrection corrector;
	
	// position data
	private static double x, y, theta;
	private double [] oldDH, dDH;
	
	
	
	// lock object for mutual exclusion
	private static Object lock;

	/**
	 * Odometer constructor. Allows for the keeping of up-to-date information about the robot's current postion and heading
	 * 
	 * @param leftMotor - The motor for the left wheel
	 * @param rightMotor - The motor for the right wheel
	 * @param corrector - The Odometry Correction being used
	 */
	public Odometer(TwoWheeledRobot robo, OdometryCorrection corrector) {
		x = 0.0;
		y = 0.0;
		
		theta = Math.PI / 180;
		lock = new Object();
		
		this.corrector = corrector;
		this.robo = robo;
		
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
			
			x += dDH[0] * Math.sin(Math.toRadians(theta));
			y += dDH[0] * Math.cos(Math.toRadians(theta));
			
			// Determines if the robot is moving straight. If so, attempt to do odometry correction
			// note* only updates if robot has recently crossed a gridline.
			double[] data = new double[]{x,y,theta};
			double lSpeed = robo.getLeftWheelSpeed();
			double rSpeed = robo.getRightWheelSpeed();
			if(lSpeed == rSpeed)
				corrector.update(data, Math.abs(lSpeed));
			x = data[0];
			y = data[1];
			theta = data[2];
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

	public TwoWheeledRobot getTwoWheeledRobot() {
		return robo;
	}
	
	public Navigation getNavigator() {
		return this.nav;
	}
	
	// Setters
	/**
	 * Updates the position and heading of the robot with the values contained in the 'position' parameter.
	 * Only updates the value if the corresponding index in 'update' is true.
	 * 
	 * @param position - values containing the updated position and heading x:0 y:1 t:2
	 * @param update - values of whether or not to update the respective value x:0 y:1 t:2
	 */
	public void setPosition(double[] position, boolean[] update) {
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
	public static double fixDegAngle(double angle) {		
		if (angle < 0.0)
			angle = 360.0 + (angle % 360.0);
		
		return angle % 360.0;
	}
	
	public static double minimumAngleFromTo(double a, double b) {
		double d = fixDegAngle(b - a);
		
		if (d < 180.0)
			return d;
		else
			return d - 360.0;
	}
}
