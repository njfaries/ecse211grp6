package robot.mapping;

import java.util.ArrayList;

import lejos.nxt.LCD;
import robot.base.Status;
import robot.navigation.Navigation;
import robot.navigation.Navigation2;
import robot.navigation.Odometer;
import robot.sensors.USGather;

public class Scan {

	public static boolean scanParsed = true;
	private static Object lock = new Object();
	/**
	 * Starts a scan that takes in the robot's navigator and ultrasonic information then, after the scan is complete,
	 * runs a new ScanAnalysis with the collected data;
	 * @param nav - The Navigation class being used
	 * @param us - The USGather class being used
	 */
	public Scan(Navigation2 nav, USGather us){	
		scanParsed = false;
		// Creates empty array lists that data is added to
		double[] pos = new double[3];
		
		ArrayList<Double> tValues = new ArrayList<Double>();
		ArrayList<Double> rValues = new ArrayList<Double>();
		
		// keeps taking in data until the robot stops turning (i.e. the scan completes)
		while(!nav.isDone()){	
			Status.setStatus("Scanning");
			Odometer.getPosition(pos);
			double dist = us.getDistance();
			double angle = (int)pos[2];

			if(dist > 60)
				continue;
			if(tValues.contains(angle) && rValues.get(tValues.indexOf(angle)) != -1){
				int i = tValues.indexOf(angle);
				tValues.set(i, (rValues.get(i) + dist) / 2);
			}
			else{
				tValues.add(angle);
				rValues.add(dist);
			}

			try { Thread.sleep(50); } catch (InterruptedException e) {}
		}
		nav.stop();
		
		// Convert the arrayLists to arrays
		
		double[] tArray = new double[tValues.size()];
		double[] rArray = new double[rValues.size()];
		double[] xArray = new double[tValues.size()];
		double[] yArray = new double[rValues.size()];
		Odometer.getPosition(pos);
		
		for(int i=0; i < tValues.size(); i++){
			tArray[i] = tValues.get(i);
			rArray[i] = rValues.get(i);
			xArray[i] = pos[0] + rArray[i] * Math.cos(Math.toRadians(rArray[i]));
			yArray[i] = pos[0] + rArray[i] * Math.sin(Math.toRadians(rArray[i]));
		}
		
		Status.setStatus("" + tArray.length);
		LCD.drawString((int)tArray.length + "",0,0);
		
		// Creates a new set of coordinates with the scan data
		new ScanAnalysis(tArray, rArray);
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