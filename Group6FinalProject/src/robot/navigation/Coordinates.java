package robot.navigation;

import java.lang.Math.*;
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
	
	//coordinates input in (r,theta) and are construted to (x,y)
	public Coordinates(double[] radii, double[] thetas) {
		this.xs = radii;
		this.ys = thetas;
		convertToXY();
		this.slopes = generateSlopes();	
		this.leftLatchedIndex = findLeftIndex();
		this.rightLatchedIndex = findRightIndex();
		this.middleLatchedIndex = findMiddleIndex();
		this.boundaryEquations = generateBoundaryEquations();
		
	}
	
	//method to print all coordintes
	public void printCoords() {
		for(int i = 0; i < xs.length; i++) {
			System.out.println("(" + (int)xs[i] + "," + (int)ys[i] + ")");
		} 
	}
	
	//method to print all slopes
	public void printSlopes() {
		for(int i = 0; i < slopes.length; i++) {
			System.out.println("slope at " + i + " = " + slopes[i]);
		} 
	}
	
	//method to print all boundary equation values
	public void printBoundaryEquations() {
		for(int i = 0; i < boundaryEquations.length; i++) {
			System.out.println("boundaryEquations[" + i + "] = " + boundaryEquations[i]);
		} 
	}
	
	//convert to (x,y)
	public void convertToXY() {
		double curDist = 0;
		double curRadians = 0;
		for(int i = 0; i < xs.length; i++) {
			curDist = xs[i];
			curRadians = Math.toRadians(ys[i]);
			//need to add odo.getX() and odo.getY() HERE!!!!
			//also take into account robots odo.getTheta() in constructor!!!!
			xs[i] = curDist*Math.cos(curRadians);
			ys[i] = curDist*Math.sin(curRadians);
		}
	}
	
	//generate slopes from x and y coordinates
	public double[] generateSlopes() {
		int slopesLen = xs.length;
		double[] slopes = new double[slopesLen - 1];
		for(int i = 0; i < slopesLen - 1; i++) {
			slopes[i] = (ys[i+1] - ys[i])/(xs[i+1] - xs[i]);
		}
		return slopes;
	}
	
	//method to find left corner block index
	public int findLeftIndex() {
		for(int i = 0; i < slopes.length - 1; i++) {
			if(Math.abs(slopes[i] - slopes[i+1]) > OUTSIDE_LATCH_THRESH) {
				System.out.println("leftLatchedIndex = " + (i+1));
				return i+1;
			}
		}
		//shouldn't get here
		return 0;
	}
	
	//method to find right corner block index
	public int findRightIndex() {
		for(int i = slopes.length - 1; i > 0; i--) {
			if(Math.abs(slopes[i] - slopes[i-1]) > OUTSIDE_LATCH_THRESH) {
				System.out.println("rightLatchedIndex = " + (i -1));
				return i - 1;
			}
		}
		//shouldn't get here
		return slopes.length;
	}
	
	//method to find middle corner block index
	public int findMiddleIndex() {
		for(int i = leftLatchedIndex; i < rightLatchedIndex; i++) {
			if(Math.abs(slopes[i+1] - slopes[i]) > LATCH_MIDDLE_SLOPE_THRESH) {
				latchMiddle = true;
				System.out.println("latchMiddle = " + latchMiddle);
				System.out.println("middleLatchedIndex = " + i);
				return i;
			}
		}
		//shouldn't get here
		latchMiddle = false;
		return leftLatchedIndex - rightLatchedIndex;
	}
	
	//method to generate boundary equations based on attributes initiated
	public double[] generateBoundaryEquations() {
		//generate two line if latchMiddle is true and one if not
		if(latchMiddle == true) {
			double[] es = new double[4];
			es[0] = findSlope(xs[leftLatchedIndex], ys[leftLatchedIndex], xs[middleLatchedIndex], ys[middleLatchedIndex]);
			es[1] = findYIntercept(es[0], xs[leftLatchedIndex], ys[leftLatchedIndex]);
			es[2] = findSlope(xs[middleLatchedIndex], ys[middleLatchedIndex], xs[rightLatchedIndex], ys[rightLatchedIndex]);
			es[3] = findYIntercept(es[2], xs[rightLatchedIndex], ys[rightLatchedIndex]);
			return es;
		}
		else {
			//if distance between two points is less than thresh we are looking at short side
		}
		//tmp for compile
		double[] tmp = {5,5,5,5};
		return tmp;
	}
	
	//helper to return a distance between two points
	public double longSide(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x2-x1)*(x2-x1) + (y2 - y1)*(y2 - y1));
	}
	
	//helper to find slope based on four points
	public double findSlope(double x1, double y1, double x2, double y2) {
		return (y2 - y1)/(x2 - x1);
	}
	
	//helper to find y-intercept based on a slope and point
	public double findYIntercept(double m, double x, double y) {
		return y - m*x;
	}

	
}