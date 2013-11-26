package robot.sensors;

import robot.navigation.Odometer;
import robot.navigation.OdometryCorrection;
import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 * Takes in data from the three color sensor on the robot. One on the front. Two on the bottom.
 * Also determines whether or not a sensor is on a gridline.
 * 
 * @version 2.0.0
 * @author Andreas
 * @since 
 */
public class ColorGather implements TimerListener {
	private static final double LINE_THRESHOLD = 5;
	private final int WOODEN_BLOCK_THRESH = 30;
	private double currentColorLeft;
	private double currentColorRight;
	private ColorSensor csLeft, csRight;
	private OdometryCorrection corr;
	private static Timer timer;
	
	private double[] lightReadingsLeft, lightReadingsRight, diffsLeft, diffsRight;
	private int readingNum;
	private boolean leftOnLine, rightOnLine;
	private static boolean doCorrection;
	
	/**
	 * In order to gather color information. The ColorGather needs to have access to the three color sensors
	 * 
	 * @param csLeft - The left o
	 * @param csRight
	 * @param csBlockReader
	 */
	public ColorGather(ColorSensor csLeft, ColorSensor csRight, OdometryCorrection corr) {
		this.csLeft = csLeft;
		this.csRight = csRight;
		this.corr = corr;
		
		lightReadingsLeft = new double[7];
		lightReadingsRight = new double[7];
		diffsLeft = new double[7];
		diffsRight = new double[7];
		readingNum = -1;
		leftOnLine = false;
		rightOnLine = false;
		doCorrection = false;

		
		timer = new Timer(100, this);
		timer.start();
	}
	
	@Override
	public void timedOut() {
		csLeft.setFloodlight(Color.RED);
		csRight.setFloodlight(Color.RED);
		
		// Gets light readings;
		currentColorLeft = csLeft.getRawLightValue();
		currentColorRight = csRight.getRawLightValue();
		
		if(currentColorLeft < 0 || currentColorRight < 0)
			return;
		
		readingNum++; // Increments the array index
		if(readingNum > 6) // Loops the array index if necessary
			readingNum = 0;		
				
		// Puts them in an array
		lightReadingsLeft[readingNum] = currentColorLeft;
		lightReadingsRight[readingNum] = currentColorRight;
		
		// finds the differences in the light readings
		if(readingNum > 0){
			diffsLeft[readingNum] = currentColorLeft - lightReadingsLeft[readingNum - 1];
			diffsRight[readingNum] = currentColorRight - lightReadingsRight[readingNum - 1];
		}
		else{
			diffsLeft[readingNum] = currentColorLeft - lightReadingsLeft[6];
			diffsRight[readingNum] = currentColorRight - lightReadingsRight[6];
		}
		
		
/*		if(Odometer.getLeftWheelSpeed() == Odometer.getRightWheelSpeed())
			doCorrection();
		else
			stopCorrection();*/
		
		if(!doCorrection)
			return;
		
		isOnLine(0);
		isOnLine(1);
		
		if(leftOnLine){
			long time = System.currentTimeMillis();
			corr.update(0, time);
		}
		if(rightOnLine){
			long time = System.currentTimeMillis();
			corr.update(1, time);
		}
		if(!leftOnLine && !rightOnLine){
			long time = System.currentTimeMillis();
			corr.update(-1, time);
		}
	}
	
	/**
	 * Determines if a current sensor is currently over a line. The input parameter determines which sensor is checked.
	 * 
	 * @param sensor left:0 right:1
	 * @return boolean onLine
	 */
	public boolean isOnLine(int sensor) {
		double[] diffs = null;
		boolean online = false;
		
		if(sensor == 0){
			diffs = diffsLeft;
			online = leftOnLine;
		}
		else{
			diffs = diffsRight;
			online = rightOnLine;
		}
				
		// finds the average of the past 7 differences
		double sumDiff = 0;
		for(double d : diffs){sumDiff += d;};
		sumDiff = sumDiff / 7;
		
		// going on/off a line determined by the average of the past 7 light differences
		if(!online && sumDiff < -LINE_THRESHOLD)
			online = true;
		else if(online && sumDiff > LINE_THRESHOLD)
			online = false;

		//LCD.drawString(sumDiff + "", 0, 4);
		
		if(sensor == 0)
			leftOnLine = online;
		else
			rightOnLine = online;
		
		return online;
	}
	
	/**
	 * Sets environment for localization
	 */
	public static void doLocalization(){
		timer.setDelay(20);
	}
	/**
	 * Sets environment for correction
	 */
	public static void doCorrection(){
		doCorrection = true;
		timer.setDelay(20);
	}
	/**
	 * Resets envrionment
	 */
	public static void stopCorrection(){
		if(doCorrection){
			doCorrection = false;
			timer.setDelay(100);
		}
	}
}
