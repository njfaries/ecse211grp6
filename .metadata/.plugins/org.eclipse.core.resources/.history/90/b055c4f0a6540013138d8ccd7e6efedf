package robot.mapping;

import java.util.Arrays;

import lejos.nxt.LCD;
import robot.navigation.Odometer;

public class ScanAnalysis {
	private static final double DIST_THRESHOLD = 2;
	private static int iteration = 0;
	
	private double[] pos = new double[3];
	
	/**
	 * Analyzes the scan data. Called by the Scan class. Finds objects in the data and creates Block classes
	 * from this data by calling Map.addBlock(x[], y[])
	 * @param tValues - Array of theta values
	 * @param rValues - Corresponding radii
	 */
	public ScanAnalysis(double[] tValues, double[] rValues){
		if(tValues.length < 2)
			return;
		
		// Filters out values where the radius is constant (robot is seeing a max point but could be looking at a gap)
		// Also filters out values above 60
		double[] rValuesFiltered = rValues;
		for(int i = 1; i < tValues.length - 1; i++){
			if(Math.abs(rValues[i + 1] - rValues[i]) < DIST_THRESHOLD && Math.abs(rValues[i - 1] - rValues[i]) < DIST_THRESHOLD){
				rValuesFiltered[i] = -1;
			}
		}
		
		try{
			// Parse filtered arrays
			parseValues(tValues, rValuesFiltered);
			Scan.scanParsed = true;
		}
		catch(Exception e){
			//LCD.drawString(e.toString(), 0,0);
		}
	}
	// Parses the filtered data (calls itself recursively each time it finds an object)
	private void parseValues(double[] tValues, double[] rValues){		
		int start = -1;
		int end = -1;
		
		//Finds the start/end point of various series.
		// Starts when a radius is not -1
		// Ends when the radius is -1 again
		for(int i = 0; i < tValues.length; i++){	
			if(rValues[i] > -1 && start == -1){
				start = i;
			}
			else if (rValues[i] == -1 && start != -1 && end == -1){
				end = i - 1;
				break;
			}
		}
		
		// Determines if nothing was found/the end is the end of the object is the end of the data series
		if(start == -1 && end == -1)
			return;
		else if(end == -1)
			end = tValues.length - 1;
		
		// Creates x/y arrays using the start and end radii (inclusive)
		double[] xValues = new double[end - start + 1];
		double[] yValues = new double[end - start + 1];
		
		LCD.drawString((int)rValues[start] + "|",iteration * 4,1);
		
		Odometer.getPosition(pos);
		for(int i = start; i < end + 1; i++){	
			// Creates the (x,y) values from (r,t)
			
			xValues[i - start] = pos[0] + rValues[i] * Math.cos(Math.toRadians(tValues[i]));
			yValues[i - start] = pos[0] + rValues[i] * Math.sin(Math.toRadians(tValues[i]));

			LCD.setPixel((int)(xValues[i - start] - pos[0])/4 + 64, (int)(yValues[i - start] - pos[1])/4 + 32, 1);
		}
		Map.addBlock(xValues, yValues);
		
		// If the end of the object is the end of the data series, don't continue
		if(end == tValues.length - 1)
			return;
		
		// Otherwise recursively call this method using the remainder of the scan		
		double[] tValues2 = Arrays.copyOfRange(tValues, end + 1, tValues.length);
		double[] rValues2 = Arrays.copyOfRange(rValues, end + 1, rValues.length);
		
		iteration++;
		
		parseValues(tValues2, rValues2);
	}
}
