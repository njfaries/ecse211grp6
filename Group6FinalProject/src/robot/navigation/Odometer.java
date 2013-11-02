package robot.navigation;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class Odometer implements TimerListener {
	private NXTRegulatedMotor leftMotor;
	private NXTRegulatedMotor rightMotor;
	private OdometryCorrection corrector;
	
	// robot position
	private static double x, y, theta;
	//wheel radius in cm
	private double wheelRadius = 2.125;
	//distance from robot center to wheel center in cm
	private double wheelBase = 15.2;
	//initializing needed tacho values
	private int tachoL, tachoR, changeInTachoL, changeInTachoR;
	private int lastTachoL = 0;
	private int lastTachoR = 0;
	//initializing displacement value
	private double changeInDisplacement = 0.0; ////// or set to read tacho value?????
	private double changeInTheta = 90.0 * (Math.PI / 180);

	//Current forward speed of the robot
	private double speed;
	
	// lock object for mutual exclusion
	private static Object lock;

	// default constructor
	public Odometer(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor, OdometryCorrection corrector) {
		x = 0.0;
		y = 0.0;
		
		theta = Math.PI / 180;
		lock = new Object();
		
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.corrector = corrector;
		
		Timer timer = new Timer(25, this);
		timer.start();
	}

	// run method (required for Thread)
	public void timedOut() {
		// put (some of) your odometer code here
		
		//update tachos
		updateTacho();
		
		synchronized (lock) {			
			//changing theta back to radians for calculation
			theta = Math.toRadians(theta);
			
			//calculate the change in theta
			calculateChangeInTheta();
			
			//finding new x and y coordinates
			calculateChangeInDisplacement();
			x = x + changeInDisplacement * Math.cos(theta + changeInTheta/2);
			y = y + changeInDisplacement * Math.sin(theta + changeInTheta/2);
			
			//calculating the new theta
			theta = theta - changeInTheta;
			//changing to degrees for display
			theta = Math.toDegrees(theta);
			
			double lSpeed = leftMotor.getRotationSpeed();
			double rSpeed = rightMotor.getRotationSpeed();
			if(lSpeed == rSpeed)
				corrector.update(lSpeed, theta, x, y);
			
			if(corrector.isUpdateAvailable())
				theta = corrector.getNewTheta();
		}
	}
	
	//using formula to find change in theta
	public void calculateChangeInTheta() {
		changeInTheta = (changeInTachoL*wheelRadius - changeInTachoR*wheelRadius)/wheelBase;
		changeInTheta = changeInTheta * (Math.PI / 180);
	}
	
	//using formula to find change in displacement
	public void calculateChangeInDisplacement() {
		changeInDisplacement = (changeInTachoL*wheelRadius + changeInTachoR*wheelRadius)/2;
		changeInDisplacement = changeInDisplacement * (Math.PI / 180);
	}
	
	//update the Tacho value and find changeInTacho
	public void updateTacho() {
		
		//reading new tacho values
		tachoL = leftMotor.getTachoCount();
		tachoR = rightMotor.getTachoCount();
		
		//finding difference between old and new tacho readings
		changeInTachoL = tachoL - lastTachoL;
		changeInTachoR = tachoR - lastTachoR;
		
		//reseting lastTacho values for next iteration
		lastTachoL = tachoL;
		lastTachoR = tachoR;
		
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	public static double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public static double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public static double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
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

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
}
