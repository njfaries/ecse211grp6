package robot.sensors;
import java.lang.Math;

/**
 * Construct that takes in the distance data about a block then finds the block's corners and line slopes to
 * generate a virtual representation of object in question.
 * 
 * @author Michael
 * @version 1.0.0
 * @since
 *
 */
public class Coordinates {
	//private final int SCOPE_SIZE = 5;
	//this will take A LOT of fine tuning
	private final double LATCH_MIDDLE_SLOPE_THRESH = 0.3;
	private final double OUTSIDE_LATCH_THRESH = 0.5;
	private boolean latchMiddle = false;
	private double[] xs;
	private double[] ys;
	private double[] slopes;
	private int leftLatchedIndex;
	private int rightLatchedIndex;
	private int middleLatchedIndex;
	// {m1,b1,m2,b2,m3,b3,m4,b4}
	private double[] boundaryEquations;
	private double scannedAtPositionX;
	private double scannedAtPositionY;
	
	//coordinates input in (r,theta) and are constructed to (x,y)
	public Coordinates(double[] radii, double[] thetas, double x, double y) {
		this.scannedAtPositionX = x;
		this.scannedAtPositionY = y;
		this.xs = radii;
		this.ys = thetas;
		convertToXY();
		this.slopes = generateSlopes();	
		this.leftLatchedIndex = findLeftIndex();
		this.rightLatchedIndex = findRightIndex();
		this.middleLatchedIndex = findMiddleIndex();
		this.boundaryEquations = generateBoundaryEquations();
	}
	
	//convert to (x,y)
	private void convertToXY() {
		double curDist = 0;
		double curRadians = 0;
		for(int i = 0; i < xs.length; i++) {
			curDist = xs[i];
			curRadians = Math.toRadians(ys[i]);
			//need to add odo.getX() and odo.getY() HERE!!!!
			//also take into account robots odo.getTheta() in constructor!!!!
			xs[i] = scannedAtPositionX + curDist * Math.cos(curRadians);
			ys[i] = scannedAtPositionY + curDist * Math.sin(curRadians);
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
	
	//method to find left corner block index
	private int findLeftIndex() {
		for(int i = 0; i < slopes.length - 1; i++) {
			if(Math.abs(slopes[i] - slopes[i+1]) > OUTSIDE_LATCH_THRESH) {
				return i+1;
			}
		}
		//shouldn't get here
		return 0;
	}
	
	//method to find right corner block index
	private int findRightIndex() {
		for(int i = slopes.length - 1; i > 0; i--) {
			if(Math.abs(slopes[i] - slopes[i-1]) > OUTSIDE_LATCH_THRESH) {
				return i - 1;
			}
		}
		//shouldn't get here
		return slopes.length;
	}
	
	//method to find middle corner block index
	private int findMiddleIndex() {
		for(int i = leftLatchedIndex; i < rightLatchedIndex - 1; i++) {
			if(Math.abs(slopes[i+1] - slopes[i]) > LATCH_MIDDLE_SLOPE_THRESH) {
				latchMiddle = true;
				return i;
			}
		}
		//shouldn't get here
		latchMiddle = false;
		return leftLatchedIndex - rightLatchedIndex;
	}
	
	//method to generate boundary equations based on attributes initiated
	private double[] generateBoundaryEquations() {
		//generate two line if latchMiddle is true and one if not
		if(latchMiddle == true) {
			double[] es = new double[4];
			es[0] = findSlope(xs[leftLatchedIndex], ys[leftLatchedIndex], xs[middleLatchedIndex], ys[middleLatchedIndex]);
			es[1] = findYIntercept(es[0], xs[leftLatchedIndex], ys[leftLatchedIndex]);
			es[2] = findSlope(xs[middleLatchedIndex], ys[middleLatchedIndex], xs[rightLatchedIndex], ys[rightLatchedIndex]);
			es[3] = findYIntercept(es[2], xs[rightLatchedIndex], ys[rightLatchedIndex]);
			//need to generate two more equations based on the two found
			return es;
		}
		else {
			//if distance between two points is less than thresh we are looking at short side
		}
		//tmp for compile
		double[] tmp = {5,5,5,5};
		return tmp;
	}
	
	//helper to find fourth vertex

	
	//helper to return a distance between two points
	private double longSide(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x2-x1)*(x2-x1) + (y2 - y1)*(y2 - y1));
	}
	
	//helper to find slope based on four points
	private double findSlope(double x1, double y1, double x2, double y2) {
		return (y2 - y1)/(x2 - x1);
	}
	
	//helper to find y-intercept based on a slope and point
	private double findYIntercept(double m, double x, double y) {
		return y - m*x;
	}

	/**
	 * Returns the boundary equations for an object
	 * @return boundaryEquations
	 */
	public double[] getBoundaryEquations() {
		return boundaryEquations;
	}
	/**
	 * Returns an array of the X values of an object
	 * @return double[] xs
	 */
	public double[] getXs() {
		return xs;
	}
	/**
	 * Returns an array of the Y values of an object
	 * @return double[] ys
	 */
	public double[] getYs() {
		return ys;
	}
	
}
