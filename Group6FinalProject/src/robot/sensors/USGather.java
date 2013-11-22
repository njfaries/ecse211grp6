package robot.sensors;
import java.util.Arrays;

import robot.navigation.Odometer;
import lejos.util.Timer;
import lejos.util.TimerListener;
import lejos.nxt.UltrasonicSensor;

/**
 * Gathers information from the Ultrasonic sensor and filters out bad values.
 * 
 * @author Michael
 * @version 1.0.0
 * @since 
 */
public class USGather implements TimerListener {
	
	private int distance;
	private int rawDistance;
	private boolean filter;
	
	private final int FLAG_THRESH = 34;
	private final int SEN_TO_CENTER = 8;
	private final static int FILTER_OUT = 5;
	private final int SLEEP_TIME = 10;
	private final int WALL_ERROR = 20;
	private double[] pos = new double[3];
	private UltrasonicSensor us;
	
	private Object lock = new Object();
	
	/**
	 * Takes in the ultrasonic sensor from which to gather data.
	 * @param us - The ultrasonic sensor gathering the information.
	 */
	public USGather(UltrasonicSensor us) {
		this.us = us;
		
		Timer timer = new Timer(25, this);
		timer.start();
	}
	
	public void testCoord() {
		
	}
	
	public void timedOut() {
		synchronized(lock){
			distance = getFilteredData();
		}
	}
	
	public void updateReadValues(double d) {

	}
	
	/**
	 * Scans an array of values at a range and update an array of navigation options
	 *  
	 * @param range - The max distance at which to detect an object.
	 */
	public void scan(int range) {
		
	}
	
	//method to check for obstruction block when navigating to waypoint
	public boolean flagObstruction() {
		if(getRawDistance() < FLAG_THRESH) {
			return true;
		}
		else {
			return false;
		}
	}
	
	//getFilteredData will return an int after filtering out 255 values
	private int getFilteredData() {
		filter = true;
		int filterControl = 0;
			
			while(filter) {
				
				// do a ping
				us.ping();
				// wait for the ping to complete
				try { Thread.sleep(SLEEP_TIME); } catch (InterruptedException e) {}
	
				distance = us.getDistance();
				
				// implemented a filter to filter out 255 values
				if(distance == 255 && (filterControl < FILTER_OUT) ) { 
					filterControl++; 
				}
				else {
					filter = false;
				}
			
			}	
		rawDistance = distance + 2 * SEN_TO_CENTER;
		return filterWall(distance + 2 * SEN_TO_CENTER);
	}
	
	//method to test and filter out wall readings and filtering out found blocks
	private int filterWall(int reading) {
		Odometer.getPosition(pos);
		double x = pos[0];
		double y = pos[1];
		double theta = pos[2];
		
		//find the x and y coordinate at distance being read
		double readX = x + reading*Math.cos(Math.toRadians(theta));
		double readY = y + reading*Math.sin(Math.toRadians(theta));
		//checking if coordinate is a wall value
		if(readY < WALL_ERROR || readY > 240 - WALL_ERROR || readX < WALL_ERROR || readX > 240 - WALL_ERROR) { 
			reading = 200; 
		}
		//checking if coordinate is a block found ???

		return reading;
	}
	
	/**
	 * Returns the current distance as recorded by the Ultrasonic sensor
	 * @return double: distance
	 */
	public double getDistance(){
		synchronized(lock){
			// Should return the distance from the center of the robot
			return distance;
		}
	}
	public double getRawDistance(){
		synchronized(lock){
			// Should return the distance from the center of the robot
			return rawDistance;
		}
	}
}
