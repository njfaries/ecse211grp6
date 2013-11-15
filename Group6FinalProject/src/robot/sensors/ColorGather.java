package robot.sensors;

import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.LCD;
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
	private double currentColorLeft;
	private double currentColorRight;
	private ColorSensor csLeft, csRight, csBlockReader;
	
	private double[] lightReadingsLeft, lightReadingsRight, diffsLeft, diffsRight;
	private int readingNum;
	private boolean isOnLine;
	
	/**
	 * In order to gather color information. The ColorGather needs to have access to the three color sensors
	 * 
	 * @param csLeft - The left o
	 * @param csRight
	 * @param csBlockReader
	 */
	public ColorGather(ColorSensor csLeft, ColorSensor csRight, ColorSensor csBlockReader) {
		this.csLeft = csLeft;
		this.csRight = csRight;
		this.csBlockReader = csBlockReader;
		
		lightReadingsLeft = new double[7];
		lightReadingsRight = new double[7];
		diffsLeft = new double[7];
		diffsRight = new double[7];
		readingNum = -1;
		isOnLine = false;
		

		
		Timer timer = new Timer(50, this);
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
	 * Determines if the light sensor in the front of the robot is detecting a Styrofoam block.
	 * @return boolean - if the block is blue
	 */	
	 public boolean isBlue() {
		
		int red = getFilteredRed();
		try { Thread.sleep(10); } catch (InterruptedException e) {}
		
		int blue = getFilteredBlue();
		try { Thread.sleep(10); }  catch (InterruptedException e) {}
		
		csBlockReader.setFloodlight(false);
		
		if(red - blue < 50)
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
		
		if(sensor == 0)
			diffs = diffsLeft;
		else
			diffs = diffsRight;
				
		// finds the average of the past 7 differences
		double sumDiff = 0;
		for(double d : diffs){sumDiff += d;};
		sumDiff = sumDiff / 7;
		
		// going on/off a line determined by the average of the past 7 light differences
		if(!isOnLine && sumDiff < -LINE_THRESHOLD)
			isOnLine = true;
		else if(isOnLine && sumDiff > LINE_THRESHOLD)
			isOnLine = false;

		LCD.drawString(sumDiff + "", 0, 4);
		
		return isOnLine;
	}
}
