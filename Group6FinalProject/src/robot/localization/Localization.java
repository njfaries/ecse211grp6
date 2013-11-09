package robot.localization;
import robot.sensors.ColorGather;
import robot.sensors.USGather;
import robot.navigation.*;
import lejos.nxt.*;

/**
 * The localization class responsible for getting the precise position of the robot.
 * 
 * @author Nathaniel
 * @version 1.0.0
 * @since 2013-11-09
 *
 */

public class Localization extends Thread {
	public enum Corner { ONE, TWO, THREE, FOUR }; 	//To determine values for angles.
	private final int US_OFFSET = 7;				//Measured value (distance from centre)
	private final double LS_OFFSET_ANGLE = 29.74488;//Measured value (degrees from central axis)
	private final double LS_OFFSET_DIST = 7;		//Measured value (distance from centre of rotation)
	private final int WALL_DISTANCE = 50; 			//Arbitrary value.  Not tested
	private final int ROTATION_SPEED = 100; 		//Needs to be tested
	private Navigation nav;	
	private USGather usGather;
	private ColorGather colorGather;
	private Corner corner;
	private int angleAdjustment;
	
	
	public Localization(UltrasonicSensor us, ColorSensor csLeft, ColorSensor csRight, ColorSensor csBlock, Corner corner, TwoWheeledRobot robo) {
		this.corner = corner;
		usGather = new USGather(us);
		colorGather = new ColorGather(csLeft, csRight, csBlock);
		nav = new Navigation(robo);
	}
	
	public void run() {
		switch(corner) {
		case ONE: 	angleAdjustment = 0;
		case TWO: 	angleAdjustment = 270;
		case THREE: angleAdjustment = 180;
		case FOUR: 	angleAdjustment = 90;
		default: 	angleAdjustment = 0;
		}
		usLocalization();
		nav.travelTo(10, 10);
		lightLocalization();
		
	}
	
	/**
	 * The method responsible for handling the ultrasonic part of localization.  It will
	 * set the heading in the odometer.
	 */	
	
	private void usLocalization() {
		double angleA = 0; double angleB = 0;
		double[] array = new double[3];			//Just to make getPosition happy...
		boolean direction = true;
		while (usGather.getDistance() < (WALL_DISTANCE - US_OFFSET)) {				//If starts facing a wall it will rotate until
			rotate(direction);														//it is no longer facing wall.
		}
		while(true) {	//while still localizing, may put bool variable here later.
			rotate(direction);
			if (usGather.getDistance() < (WALL_DISTANCE - US_OFFSET)) {			//Once the rising edge is detected...
				Motor.A.stop();														//stop the motors and get the theta value
				Motor.B.stop();														//from the odometer
				Odometer.getPosition(array); 										//Need to be able to get theta more easily...
				if (angleA == 0) {													//If this is the first time it's stopped (angleA hasn't
					angleA = array[2];												//been changed) then set angleA and reverse the direction.
					direction = false;												//The sleep is to ensure that the same edge is not picked
					try { Thread.sleep(200); } catch (InterruptedException e) {}	//up again.
				} else {
					angleB = array[2];												//Sets the second angle,	
					array[2] = calculateUS(angleA, angleB) + angleAdjustment;			//Calculates the true angle and adjusts for corner.
					Odometer.setPosition(array, new boolean [] {false, false, true});	//Sets the theta in the odometer, but NOT x and y.
					nav.turnTo(angleAdjustment, 0);
					return;
				}
			}
		}
	}
	
	/**
	 * The method responsible for handling the light sensor part of localization.
	 * It will set the x and y of the odometer.
	 */
	
	private void lightLocalization() {
		boolean direction = true;
		int counter = 0;
		double[] angles = new double[4];
		double[] array = new double[3];
		while(true) {
			rotate(direction);
			if (colorGather.isOnLine(0)) {						//0 = left sensor on robot
				Odometer.getPosition(array);
				angles[counter] = array[2] - LS_OFFSET_ANGLE;	//store the current angle and
				counter++;										//increment the counter.
			}
			if (counter == 4) {									//After four lines have been scanned
				Motor.A.stop();									//stop the motors and calculate the	
				Motor.B.stop();									//new odometer values.
				Odometer.setPosition(calculateLS(angles), new boolean[] {true, true, true});
				return;
			}
		}
	}
	
	/**
	 * This method is responsible for calculating the actual heading of the robot.
	 * 
	 * @param angleA - The first angle read by the robot in ultrasonic localization
	 * @param angleB - The second angle read by the robot in ultrasonic localization
	 * @return realAngle - The calculated angle.
	 */
	
	private double calculateUS(double angleA, double angleB) {
		double realAngle;
		double[] position = new double[3];
		Odometer.getPosition(position);
		if(angleA>angleB){
			//as in the rising edge, the angle in which the back wall is detected become angleB now (in falling edge, it is angleA), we invert the condition for calculation
			
			realAngle = position[2] + (Math.PI / 4 - ((angleA + angleB) / 2)) + Math.PI;	//Also will have a small number to make final correction. 	
		}																			
		else {
			realAngle = position[2] + (Math.PI * 1.25 - ((angleA + angleB) / 2)) + Math.PI; //See above.
		}
		return realAngle;
	}
	
	private double[] calculateLS(double[] angles) {
		double x, y;
		double[] array = new double[3];
		//these equations deal with any wraparound from going from 359 to 0 degrees
		if (angles[2] < angles[0]) 
			angles[2] += (Math.PI * 2);
		if (angles[3] < angles[1])
			angles[3] += (Math.PI * 2);
		//equations to calculate the actual x and y position of the robot
		double angleX=Math.abs(angles[2]-angles[0]);
		double angleY=Math.abs(angles[3]-angles[1]);
		y = -LS_OFFSET_DIST * Math.cos(angleX / 2);
		x = -LS_OFFSET_DIST * Math.cos(angleY / 2);
		
		//calculates actual heading of robot.
		double deltaTheta = (Math.PI / 2) + (angleY / 2) - (angles[3] - Math.PI);
		array[0] = x; array[1] = y;	array[2] = deltaTheta;
		return array;
	}
	
	/**
	 * The method responsible for rotating the robot to scan the walls.
	 * It uses a boolean to determine the direction.
	 * @param direction - If true, rotates robot clockwise.  If false, rotates counterclockwise.
	 */
	private void rotate(boolean direction) {
		Motor.A.setSpeed(ROTATION_SPEED);
		Motor.B.setSpeed(ROTATION_SPEED);
		//if the direction is true, rotate to clockwise
		if (direction) {
			Motor.A.forward();
			Motor.B.backward();
		} else {
			//if the direction is false, rotate to counterclockwise
			Motor.A.backward();
			Motor.B.forward();
		}
	}
}
