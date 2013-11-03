package robot.sensors;
import java.util.Arrays;

import robot.navigation.Odometer;
import lejos.util.TimerListener;
import lejos.nxt.UltrasonicSensor;

public class USGather implements TimerListener {
	
	private int distance;
	private boolean filter;
	
	private final int SEN_TO_CENTER = 7;
	private final static int FILTER_OUT = 5;
	private final int SLEEP_TIME = 10;
	private final int WALL_ERROR = 20;
	private UltrasonicSensor us;
	
	public USGather(UltrasonicSensor us) {
		this.us = us;
	}
	
	public void testCoord() {
		
	}
	
	public void timedOut() {
		
	}
	
	public void updateReadValues(double d) {

	}
	
	//method to scan an array of value at a range and update an array of places to travel to 
	public void scan(int range) {
		
	}
	
	public boolean flagObstruction() {
		return true;
	}
	
	//getFilteredData will return an int after filtering out 255 values
	public int getFilteredData() {
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
		return filterWall(distance + SEN_TO_CENTER);
	}
	
	//method to test and filter out wall readings and filtering out found blocks
	public int filterWall(int reading) {
		double x = Odometer.getX();
		double y = Odometer.getY();
		double theta = Odometer.getTheta();
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
}
