import java.util.ArrayList;
import java.lang.Math;

public class NonLinearDataFilter {
	
	//constants
	private final double SUM_THRESHOLD = 5.0;
	private final int HALF_SCOPE_SIZE = 3;
	private final double SAFE_DIST = 10.0;
	
	//inputs
	private double[] xs, ys, rs, ts;
	private double scanLocX, scanLocY, safeDist;
	
	//generated: waypoint xs, way point ys, point to check for blue block: (validXs, validYs)
	private double[] wxs, wys, sumErrors, validXs, validYs;
	private int[] validLineIndexes;
	
	//NOTE: safeDist is the distance we want the waypoint to be perpendicularly back from the block
	//if only processing data it safeDist  = 0
	public NonLinearDataFilter(double[] rs, double[] ts, double[] xs, double[] ys, double scanLocX, double scanLocY, double safeDist) {
		
		//constructing inputs
		this.xs = xs;
		this.ys = ys;
		this.rs = rs;
		this.ts = ts;
		this.safeDist = safeDist;
		this.scanLocX = scanLocX;
		this.scanLocY = scanLocY;
		
		//constructing generated
		this.wxs = new double[ts.length - 2*HALF_SCOPE_SIZE];
		this.wys = new double[ts.length - 2*HALF_SCOPE_SIZE];
		this.sumErrors = new double[wxs.length];
		generateWP();
		generateSumErrors();
		generateValidWP();
		
	}
	
	//returns an array of valid x waypoints
	public double[] getValidXWP() {
		double[] xs = new double[validXs.length];
		xs = validXs;
		return xs;
	}
	
	//returns an array of valid y waypoints
	public double[] getValidYWP() {
		double[] ys = new double[validYs.length];
		ys = validYs;
		return ys;
	}
	
	//generates valid linear waypoints
	public void generateValidWP() {
		int numValidWP = 0;
		//check number of valid lines for array creation, could have used arraylist...
		for(int i = 0; i < sumErrors.length; i++) {
			if(sumErrors[i] < SUM_THRESHOLD) {
				numValidWP++;
			}
		}
		//System.out.println("numValidWP: " + numValidWP);
		this.validXs = new double[numValidWP];
		this.validYs = new double[numValidWP];
		int validIndex = 0;
		for(int i = 0; i < sumErrors.length; i++) {
			if(sumErrors[i] < SUM_THRESHOLD) {
				validXs[validIndex] = wxs[i];
				validYs[validIndex] = wys[i];
				//System.out.println( "validXs[" + validIndex + "]: " + (int)validXs[validIndex] +
				//	 ", validYs[" + validIndex + "]: " + (int)validYs[validIndex] );
				validIndex++;
			}
		}
	}
	
	//generate potential waypoints starting from the half scope size
	public void generateWP() {
		for(int i = 0; i < wxs.length; i++) {
			wxs[i] = scanLocX + (rs[i + HALF_SCOPE_SIZE] - safeDist)*Math.cos(Math.toRadians(ts[i + HALF_SCOPE_SIZE]));
			wys[i] = scanLocY + (rs[i + HALF_SCOPE_SIZE] - safeDist)*Math.sin(Math.toRadians(ts[i + HALF_SCOPE_SIZE]));
			//System.out.println( "rs[" + (i+3) + "]: " + (int)rs[i+3] + ", ts[" + (i+3) + "]: " + (int)ts[i+3] );
			//System.out.println( "wxs[" + i + "]: " + (int)wxs[i] + ", wys[" + i + "]: " + (int)wys[i] );
		}
	}
	
	//generate the sum of the line error at each index starting form the half scope size
	public void generateSumErrors() {
		for(int i = 0; i < sumErrors.length; i++) {
			sumErrors[i] = sumTrendError(xs[i], ys[i], xs[ i+ 2*HALF_SCOPE_SIZE], ys[i + 2*HALF_SCOPE_SIZE], i, i + 2*HALF_SCOPE_SIZE);
			//System.out.println( "sumErrors[" + i + "]: " + sumErrors[i] );
		}
	}
	
	//returns the sum of the trend line error, the sum of distances from points to generated line
	public double sumTrendError(double xi, double yi, double xf, double yf, int indexi, int indexf) {
		double mTrend = generateSlope(xi ,yi ,xf ,yf);
		double mCur;
		double curTrendAngle;
		double hypotenuse;
		double sum = 0;
		//System.out.println("Sum from index: " + indexi + " to index: " + indexf);
		//go through all points within scope index sum distance to generated line
		for(int i = indexi + 1; i < indexf; i++) {
			mCur = generateSlope(xi ,yi ,xs[i] ,ys[i]);
			curTrendAngle = acuteAngleBetweenLines(mCur,mTrend);
			hypotenuse = distanceBetweenPoints(xi, yi, xs[i], ys[i]);
			sum = sum + hypotenuse*Math.sin(curTrendAngle);
		}
		return Math.abs(sum);
	}

	//generates a slope from two points
	public double generateSlope(double xi, double yi, double xf, double yf) {
		return (yf - yi)/(xf - xi);
	}	

	//assuming atan2 returns in radians
	public double acuteAngleBetweenLines(double m1, double m2) {
		return Math.atan( (m1 - m2)/(1 + m2*m2) );
	}

	//method returns hypotenuse or distance between two points
	public double distanceBetweenPoints(double xi, double yi, double xf, double yf) {
		return Math.sqrt( (xf - xi)*(xf - xi) + (yf - yi)*(yf -yi) );
	}
	
}
