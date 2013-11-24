package robot.sensors;

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
	private ColorSensor csLeft, csRight, csBlockReader;
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
	public ColorGather(ColorSensor csLeft, ColorSensor csRight, ColorSensor csBlockReader, OdometryCorrection corr) {
		this.csLeft = csLeft;
		this.csRight = csRight;
		this.csBlockReader = csBlockReader;
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
	 * Determines the int color value of object in front of the sensor
	 * @return int - value of the red light return
	 */
	public int getFilteredRed() {
		csBlockReader.setFloodlight(Color.RED);
		return csBlockReader.getNormalizedLightValue(); 
	}
	
	/**
	 * Determines the int color value of object in front of the sensor
	 * @return int - value of the blue light return
	 */
	public int getFilteredBlue() {
		csBlockReader.setFloodlight(Color.BLUE);
		return csBlockReader.getNormalizedLightValue();
	}
	
	/**
	 * Determines the int color value of object in front of the sensor
	 * @return int - value of the blue light return
	 */
	public int getFilteredGreen() {
		csBlockReader.setFloodlight(Color.GREEN);
		return csBlockReader.getNormalizedLightValue();
	}
	
	/**
	 * Determines if the light sensor in the front of the robot is detecting a Styrofoam block.
	 * @return boolean - if the block is blue
	 */	
	 public boolean isBlue() {
		
		int red = getFilteredRed();
		try { Thread.sleep(100); } catch (InterruptedException e) {}
		
		int blue = getFilteredBlue();
		try { Thread.sleep(100); }  catch (InterruptedException e) {}
		
		int green = getFilteredGreen();
		try { Thread.sleep(100); }  catch (InterruptedException e) {}
		
		csBlockReader.setFloodlight(false);
		
		int sumOfAbsDiff = Math.abs(red - green) + Math.abs(red - blue) + 
				Math.abs(green - red) + Math.abs(green - blue) + 
				Math.abs(blue - red) + Math.abs(blue - green);
				
/*		LCD.clear();
		LCD.drawString("r:" + red + " g:" + green + " b:" + blue, 0,0);
		LCD.drawString("r-g:" + (red - green) + " r-b:" + (red - blue), 0,1);
		LCD.drawString("g-r:" + (green - red) + " g-b:" + (green - blue), 0,2);
		LCD.drawString("b-r:" + (blue - red) + " b-g:" + (blue - green), 0,3);
		LCD.drawString("avgerage:" + (int)( (red + green + blue) / 3 ) ,0,4);
		LCD.drawString("sumOfAbs:" + sumOfAbsDiff ,0,5);*/
		try { Thread.sleep(500); }  catch (InterruptedException e) {}
		
		if(sumOfAbsDiff < WOODEN_BLOCK_THRESH)
			return true; 
		else
			return false; 
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
