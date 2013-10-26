package robot.navigation;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.TimerListener;

public class Navigation implements TimerListener {
	
	private NXTRegulatedMotor leftMotor;
	private NXTRegulatedMotor rightMotor;
	private Odometer odometer;
	//will need getters and setters
	private double movingToX;
	private double movingToY;
	
	private final double WHEEL_RADIUS = 2.125;
	private final double WHEEL_BASE = 15.2;
	private final double DEST_RADIUS = 0.3;
	private final double MAX_HEADING_ERROR = 5.0;
	private final double MAX_TURN_TO_ERROR = 1.0;
	
	public Navigation(Odometer odometer, NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor) {
			this.leftMotor = leftMotor;
			this.rightMotor = rightMotor;
			this.odometer = odometer;
	}
	
	public void timedOut() {
	}
		
	//executes in while loop fashion outside of timedOut
	public void travelTo() {
		
	}
		
	//helper method that returns the calculated required theta in degrees
	public double requiredHeading(double x, double y) {
		return 4.5;
	}
		
	//turnTo telling the motors to turn to the correct heading, input in degrees
	public void turnTo(double theta) {
		
	}
	
	//helper method to stop motors when last destination is reached
	public void stopMotors() {
		
	}
}
