package robot.mapping;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;

import robot.navigation.Navigation;
import robot.navigation.Odometer;
import robot.sensors.USGather;

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
	private final double LATCH_SLOPE_THRESHOLD = 0.5;
	private static boolean scanParsed = true;
	
	private double[] xs, ys, slopes;
	private double[] objectXs, objectYs;
	private int leftLatchedIndex, rightLatchedIndex, middleLatchedIndex;

	private double bestFitSlope1, bestFitSlope2;
	private double[] lineCenters = new double[4];
	private double centerX, centerY;
	
	private static Object lock = new Object();
	private double[] boundaryEquations = new double[4];
	
//--------------------------------------------------------------------------------------------
	/**
	 * Starts a scan that takes in odometry information and ultrasonic information then, after the scan is complete,
	 * create a new Coordinates object with the collected data;
	 * @param odo - The odometer class being used
	 * @param us - The USGather class being used
	 */
	public static void scan(Navigation nav, USGather us){	
		scanParsed = false;
		// Creates empty array lists that data is added to
		double[] pos = new double[3];
		ArrayList<Double> xPoints = new ArrayList<Double>();
		ArrayList<Double> yPoints = new ArrayList<Double>();
		
		// keeps taking in data until the robot stops turning (i.e. the scan completes)
		while(!nav.isDone()){
			try{Thread.sleep(100);}catch(InterruptedException e){}
			
			Odometer.getPosition(pos);
			double dist = us.getDistance();
			double angle = pos[3];
			
			// Convert relative (r,t) to absolute (x,y)
			double xVal = pos[0] + dist * Math.sin(angle);
			double yVal = pos[1] + dist * Math.cos(angle);
			
			// Filters out any points outside the map rangle
			if(xVal > 10 && xVal < Map.xMax - 10 && yVal > 10 && yVal < Map.yMax - 10){
				xPoints.add(xVal);
				yPoints.add(yVal);
			}
		}
		
		// Convert the arrayLists to arrays
		double[] xArray = new double[xPoints.size()];
		double[] yArray = new double[yPoints.size()];
		
		for(int i=0; i < xPoints.size(); i++){
			xArray[i] = xPoints.get(i);
			yArray[i] = yPoints.get(i);
		}
		
		// Creates a new set of coordinates with the scan data
		new Coordinates(xArray, yArray);
	}

//--------------------------------------------------------------------------------------------
	
	//coordinates input in (r,theta) and are constructed to (x,y)
	public Coordinates(double[] xPoints, double[] yPoints) {
		this.xs = xPoints;
		this.ys = yPoints;
		
		this.slopes = generateSlopes();	

		latchPoints();
		
		this.objectXs = Arrays.copyOfRange(xs, leftLatchedIndex, rightLatchedIndex + 1);
		this.objectYs = Arrays.copyOfRange(ys, leftLatchedIndex, rightLatchedIndex + 1);
		
		// If there is not middle point (i.e. face on to the block), only take 1 LoBF
		if(middleLatchedIndex == -1)
			this.bestFitSlope1 = slopeLineOfBestFit(objectXs, objectYs);
		// Else if the block is at an angle (i.e. middle point detected), take 2 LoBF for each side
		else
			this.bestFitSlope1 = slopeLineOfBestFit(Arrays.copyOfRange(xs, leftLatchedIndex, middleLatchedIndex + 1), Arrays.copyOfRange(ys, leftLatchedIndex, middleLatchedIndex + 1));

		this.bestFitSlope2 = - 1 / (bestFitSlope1);
		
		findMiddlePoints();
		
		generateBoundaryEquations();
		
		generateObjectCenter();
		
		// If there is an object scanned
		if(leftLatchedIndex != -1){
			// Add this object to the map
			Map.addBlock(this);
		
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
		middleLatchedIndex = -1;
		
		// Find the start of a seady slope
		for(int i = 0; i < slopes.length - 1; i++) {
			if(Math.abs(slopes[i+1] - slopes[i]) <= LATCH_SLOPE_THRESHOLD) {
				leftLatchedIndex = i;
				break;
			}
		}
		
		// Find the end of the steady slope
		for(int i = leftLatchedIndex; i < slopes.length - 1; i++) {
			if(Math.abs(slopes[i+1] - slopes[i]) > LATCH_SLOPE_THRESHOLD) {
				rightLatchedIndex = i + 1;
				break;
			}
		}
		// End of steady slope could either be the end or a corner
		middleLatchedIndex = rightLatchedIndex;
		
		// Determine if there is a middle index to be found (determine if an opposite reciprocal slope appears)
		for(int i = rightLatchedIndex; i < slopes.length - 3; i++) {
			if(Math.abs((-1)/slopes[i+1] - slopes[i]) <= LATCH_SLOPE_THRESHOLD) {
				middleLatchedIndex = i + 1;
				break;
			}
			else if(Math.abs((-1)/slopes[i+2] - slopes[i]) <= LATCH_SLOPE_THRESHOLD){
				middleLatchedIndex = i + 2;
				break;
			}
			else if(Math.abs((-1)/slopes[i+3] - slopes[i]) <= LATCH_SLOPE_THRESHOLD){
				middleLatchedIndex = i + 3;
				break;
			}
		}
		
		// If there is a middle, re-calculate the right index
		if(middleLatchedIndex != rightLatchedIndex){
			for(int i = middleLatchedIndex; i < slopes.length - 1; i++) {
				if(Math.abs(slopes[i+1] - slopes[i]) > LATCH_SLOPE_THRESHOLD) {
					rightLatchedIndex = i + 1;
					break;
				}
			}
		}
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
		double sumX1 = 0, sumY1 = 0, sumX2 = 0, sumY2 = 0, sum1 = 0, sum2 = 0;
		
		for(int i = leftLatchedIndex; i < rightLatchedIndex; i++){
			if(i <= middleLatchedIndex){
				sumX1 += xs[i];
				sumY1 += ys[i];
				sum1++;
			}
			if(i >= middleLatchedIndex){
				sumX2 += xs[i];
				sumY2 += ys[i];
				sum2++;
			}
		}
		
		lineCenters[0] = sumX1 / sum1;
		lineCenters[1] = sumY1 / sum1;
		lineCenters[2] = sumX2 / sum2;
		lineCenters[3] = sumY2 / sum2;
	}
		
	//method to generate boundary equations based on attributes initiated
	private void generateBoundaryEquations() {
		boundaryEquations[0] = bestFitSlope1;
		boundaryEquations[1] = findYIntercept(boundaryEquations[0], lineCenters[0], lineCenters[1]);
		// If not second side found, this will just be the inverse reciprocal of slope1
		boundaryEquations[2] = bestFitSlope2; 
		//middleX2 and middleY2 will be the point of the rightLatchedIndex of not second side is found
		boundaryEquations[3] = findYIntercept(boundaryEquations[2], lineCenters[2], lineCenters[3]); 
	}
	
	//helper to find y-intercept based on a slope and point
	private double findYIntercept(double m, double x, double y) {
		return y - m * x;
	}

	// Finds the objects center from boundary equations generated
	private void generateObjectCenter(){
		// Values for linear system of equations
		double  m1 = boundaryEquations[0], 
				b1 = boundaryEquations[1], 
				m2 = boundaryEquations[2], 
				b2 = boundaryEquations[3];
		
		// generate matrices for (-m1)X + (1)Y = (b1)b and (-m2)X + (1)Y = (b2)b
		// CRAMERS RULE!!
		double detA = (-m1 *  1) - (-m2 *  1);
		double detX = (-m1 * b1) - (-m2 * b2);
		double detY = ( b1 *  1) - ( b2 *  1);
		
		centerX = detX / detA;
		centerY = detY / detA;
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
	 * Returns the center of all the sides detected. This will allow the Map to determine the possible center
	 * of the object in question, allowing it to set proper bounds.
	 * @param middles
	 */
	public double[] getSideMiddles(){
		return lineCenters;
	}
	/**
	 * Returns the point (double[]) of the center of the object
	 * @return double[] - objectCenter
	 */
	public double[] getObjectCenter(){
		return new double[]{centerX, centerY};
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
