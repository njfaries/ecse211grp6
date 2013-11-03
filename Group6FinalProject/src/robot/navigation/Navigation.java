package robot.navigation;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.TimerListener;

/**
 * The navigation class that is responsible for moving the robot from point A to point B
 * @author Andreas
 *
 */
public class Navigation implements TimerListener {
	
	private NXTRegulatedMotor leftMotor;
	private NXTRegulatedMotor rightMotor;
	private Odometer odometer;
	//will need getters and setters
	private double destinationX;
	private double destinationY;
	private double destinationT;
	
	private double[] pos = new double[3];
	
	private final double WHEEL_RADIUS = 2.125;
	private final double WHEEL_BASE = 15.2;
	private final double DEST_RADIUS = 0.3;
	private final double MAX_HEADING_ERROR = 5.0;
	private final double MAX_TURN_TO_ERROR = 1.0;
	
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
	public Navigation(Odometer odometer, NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
	}
	
	public void timedOut() {
		if(done)
			return;
	}
	private void travel(){
		
	}
	private void turn(){
		
	}
	
	
	
	/**
	 * Initiates the traveling to a particular point
	 * 
	 * @param x - The destination x value
	 * @param y - The destination y value
	 */
	public void travelTo(double x, double y) {
		destinationX = x;
		destinationY = y;
		
		traveling = true;
		done = false;
	}
		
	/**
	 * Initiates the turning to a particular angle
	 * @param theta
	 */
	public void turnTo(double theta) {
		destinationT = theta;
		
		turning = true;
		done = false;
	}
	
	/**
	 * Stops all processes
	 */
	public void stop() {
		odometer.getPosition(pos);
		
		destinationX = pos[0];
		destinationY = pos[1];
		destinationT = pos[2];
		
		done = true;
		traveling = false;
		turning = false;
	}
}
