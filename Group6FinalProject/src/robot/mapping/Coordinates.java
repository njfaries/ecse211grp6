package robot.mapping;
import java.lang.Math;
import java.util.Arrays;

import lejos.nxt.LCD;
import robot.base.Status;
import robot.navigation.Odometer;
/**
 * Construct that takes in the distance data about a block then finds the block's corners and line slopes to
 * generate a virtual representation of object in question.
 * 
 * @author Michael
 * @version 1.1.0
 * @since
 *
 */

public class Coordinates {
	//private final int SCOPE_SIZE = 5;
	//this will take A LOT of fine tuning

	//private final double LATCH_MIDDLE_SLOPE_THRESH = 0.3;
	private final double LATCH_SLOPE_THRESHOLD = 5;
	private static boolean scanParsed = true;
	
	private double[] xs, ys, slopes;
	private double[] objectXs, objectYs;
	private int leftLatchedIndex, rightLatchedIndex/*, middleLatchedIndex*/;

	private double bestFitSlope1;
	private double[] lineCenters = new double[4];
	
	private static Object lock = new Object();
	private double[] boundaryEquations = new double[4];
		
	//coordinates input in (r,theta) and are constructed to (x,y)
	public Coordinates(double[] xPoints, double[] yPoints) {
		this.xs = xPoints;
		this.ys = yPoints;
		
		double[] pos = new double[3];
		Odometer.getPosition(pos);
		for(int i=0; i<xs.length; i++){
			LCD.setPixel((int)(xs[i] - pos[0])/4 + 64, (int)(ys[i] - pos[1])/4 + 32, 1);
		}
				
		this.slopes = generateSlopes();	

		latchPoints();
		if(leftLatchedIndex < 0){
			scanParsed = true;
			LCD.drawString("no left",0,0);
			return;
		}
		if(rightLatchedIndex < 0){
			scanParsed = true;
			LCD.drawString("no right",0,1);
			return;
		}
/*		LCD.drawString((int)xs[leftLatchedIndex] + "|" + (int)ys[leftLatchedIndex],0,1);
		LCD.drawString((int)xs[rightLatchedIndex] + "|" + (int)ys[rightLatchedIndex],0,2);*/
		
		LCD.setPixel((int)(xs[leftLatchedIndex] - pos[0])/4 + 64, (int)(ys[leftLatchedIndex] - pos[1])/4 + 32, 1);
		LCD.setPixel((int)(xs[rightLatchedIndex] - pos[0])/4 + 64, (int)(ys[rightLatchedIndex] - pos[1])/4 + 32, 1);
		
		Status.setStatus((int)xs[leftLatchedIndex] + " " + (int)ys[leftLatchedIndex] + " " + (int)xs[rightLatchedIndex] + " " + (int)ys[rightLatchedIndex]);
				
		this.objectXs = Arrays.copyOfRange(xs, leftLatchedIndex, rightLatchedIndex + 1);
		this.objectYs = Arrays.copyOfRange(ys, leftLatchedIndex, rightLatchedIndex + 1);

		bestFitSlope1 = slopeLineOfBestFit(objectXs, objectYs);
		
		findMiddlePoints();
		
		generateBoundaryEquations();
		
		// If there is an object scanned
		if(leftLatchedIndex != -1){
			// Add this object to the map
			Map.addBlock(objectXs, objectYs);
		
			// Parse through the rest of the scan for another object
			if(rightLatchedIndex + 1 < objectXs.length){
				double[] newXs = Arrays.copyOfRange(xs, rightLatchedIndex + 1, xs.length);
				double[] newYs = Arrays.copyOfRange(xs, rightLatchedIndex + 1, ys.length);
				new Coordinates(newXs, newYs);
			}
			else{
				// If the amount of material left is too small: don't bother parse it
				synchronized(lock){
					scanParsed = true;
				}
			}
		}
		else{
			// If there was nothing found in the previous parse: don't bother try again
			synchronized(lock){
				scanParsed = true;
			}
		}
	}
	
	//generate slopes from x and y coordinates
	private double[] generateSlopes() {
		int slopesLen = xs.length;
		double[] slopes = new double[slopesLen - 1];
		
		for(int i = 0; i < slopesLen - 1; i++) {
			slopes[i] = (ys[i+1] - ys[i])/(xs[i+1] - xs[i]);
		}
		
		return slopes;
	}
	
	// Finds the latchable points
	// 1. Searches for the left latch (determines when a stead stope appears)
	// 2. Finds the point when this steady slope ends
	// 3. If the slope after this point is an opposite reciprocal of the current steady slope (i.e. 90d off),
	//	  then latch the point as a the middle (block at an angle)
	// 4. Otherwise latch the point as the end (block straight on)
	private void latchPoints(){
		leftLatchedIndex = -1;
		rightLatchedIndex = -1;
		
		// Find the start of a steady slope
		for(int i = 0; i < slopes.length - 1; i++) {
			if(Math.abs(slopes[i+1] / slopes[i]) > LATCH_SLOPE_THRESHOLD && Math.abs(slopes[i+1] / slopes[i]) < LATCH_SLOPE_THRESHOLD +1) {
				leftLatchedIndex = i;
				LCD.drawString(i + "|" + (int)xs[i] + "|" + (int)ys[i] + "|" +(int)slopes[i+1]*10 + "|" + (int)slopes[i]*10,0,0);
				break;
			}
		}
		if(leftLatchedIndex == -1)
			return;
		
		// Find the end of the steady slope
		for(int i = leftLatchedIndex+1; i < slopes.length - 1; i++) {
			if((Math.abs(slopes[i+1]/ slopes[i]) > LATCH_SLOPE_THRESHOLD + 1 || Math.abs(slopes[i+1]/ slopes[i]) < LATCH_SLOPE_THRESHOLD)) {
				rightLatchedIndex = i;
				LCD.drawString(i + "|" + (int)xs[i] + "|" + (int)ys[i] + "|" +(int)(slopes[i+1]*10) + "|" + (int)(slopes[i] * 10),0,1);
				break;
			}
		}
		if(rightLatchedIndex == -1)
			rightLatchedIndex = slopes.length;
	}
	
	// Method to find the slope of best fit for the set of values
	// courtesy of http://hotmath.com/hotmath_help/topics/line-of-best-fit.html
	private double slopeLineOfBestFit(double[] xValues, double[] yValues){
		double m = 0;
		double sumX = 0;
		double sumY = 0;
		double sumXY = 0;
		double sumXX = 0;
		double pointCount = xValues.length;
		
		for(double d: xValues){sumX += d;}
		for(double d: yValues){sumY += d;}
		for(int i=0; i < pointCount; i++){
			sumXY += (xValues[i] * yValues[i]);
		}
		for(int i=0; i < pointCount; i++){
			sumXX += (xValues[i] * xValues[i]);
		}
		
		m = (sumXY - (sumX * sumY) / pointCount) / (sumXX - (sumX * sumX) / pointCount);
		
		return m;
	}
	
	// Finds the middle point of each side of the object
	private void findMiddlePoints(){
		double sumX1 = 0, sumY1 = 0, sum1 = 0;
		
		for(int i = leftLatchedIndex; i < rightLatchedIndex; i++){
			sumX1 += xs[i];
			sumY1 += ys[i];
			sum1++;
		}
		
		lineCenters[0] = sumX1 / sum1;
		lineCenters[1] = sumY1 / sum1;
	}
		
	//method to generate boundary equations based on attributes initiated
	private void generateBoundaryEquations() {
		boundaryEquations[0] = bestFitSlope1;
		boundaryEquations[1] = findYIntercept(boundaryEquations[0], lineCenters[0], lineCenters[1]);
	}
	
	//helper to find y-intercept based on a slope and point
	private double findYIntercept(double m, double x, double y) {
		return y - m * x;
	}
	
	/**
	 * Returns the boundary equations for an object
	 * @return boundaryEquations
	 */
	public double[] getBoundaryEquations() {
		return boundaryEquations;
	}
	/**
	 * Returns all the points thought to make up a scanned object.
	 * @return double[][] points - double[x][y] filed with object points
	 */
	public double[][] getObjectPoints() {
		return new double[][]{objectXs, objectYs};
	}
	/**
	 * Returns whether or not the latest scan has been fully parsed
	 * @return boolean - scanParsed
	 */
	public static boolean scanParsed(){
		synchronized(lock){
			return scanParsed;
		}
	}
}
