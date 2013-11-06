package robot.sensors;

import lejos.nxt.ColorSensor;
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
	
	private double currentColorFront;
	private double currentColorBack;
	private double currentColorBlock;
	private ColorSensor csLeft, csRight, csBlockReader;
	
	private double[] lightReadingsBlock, lightReadingsFront, lightReadingsBack, diffsFront, diffsBack;
	private int readingNum;
	private boolean isOnLine;
	
	/**
	 * In order to gather color information. The ColorGather needs to have access to the three color sensors
	 * 
	 * @param csFront - The front o
	 * @param csBack
	 * @param csBlockReader
	 */
	public ColorGather(ColorSensor csLeft, ColorSensor csRight, ColorSensor csBlockReader) {
		this.csLeft = csLeft;
		this.csRight = csRight;
		this.csBlockReader = csBlockReader;
		
		lightReadingsBlock = new double[7];
		lightReadingsFront = new double[7];
		lightReadingsBack = new double[7];
		diffsFront = new double[7];
		diffsBack = new double[7];
		readingNum = -1;
		isOnLine = false;
	}
	
	@Override
	public void timedOut() {
		readingNum++; // Increments the array index
		if(readingNum >= 6) // Loops the array index if necessary
			readingNum = 0;		
		
		// Gets light readings;
		currentColorBlock = csBlockReader.getRawLightValue();
		currentColorFront = csLeft.getRawLightValue();
		currentColorBack = csRight.getRawLightValue();
				
		// Puts them in an array
		lightReadingsBlock[readingNum] = currentColorBlock;
		lightReadingsFront[readingNum] = currentColorFront;
		lightReadingsBack[readingNum] = currentColorBack;
		
		// finds the differences in the light readings
		if(readingNum > 0){
			diffsFront[readingNum] = currentColorFront - lightReadingsFront[readingNum - 1];
			diffsBack[readingNum] = currentColorBack - lightReadingsBack[readingNum - 1];
		}
		else{
			diffsFront[readingNum] = currentColorFront - lightReadingsFront[6];
			diffsBack[readingNum] = currentColorBack - lightReadingsBack[6];
		}		
	}
	
	/**
	 * Determines if the light sensor in the front of the robot is detecting a Styrofoam block.
	 * @return
	 */
	public boolean isBlockBlue() {
		return false;
	}
	
	/**
	 * Determines if a current sensor is currently over a line. The input parameter determines which sensor is checked.
	 * 
	 * @param sensor front:0 back:1
	 * @return boolean onLine
	 */
	public boolean isOnLine(int sensor) {
		double[] diffs = null;
		
		if(sensor == 0)
			diffs = diffsFront;
		else
			diffs = diffsBack;
				
		// finds the average of the past 7 differences
		double sumDiff = 0;
		for(double d : diffs){sumDiff += d;};
		sumDiff = sumDiff / 7;
		
		// going on/off a line determined by the average of the past 7 light differences
		if(!isOnLine && sumDiff < -10)
			isOnLine = true;
		else if(isOnLine && sumDiff > 10)
			isOnLine = false;

		return isOnLine;
	}
}