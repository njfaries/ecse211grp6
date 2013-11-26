package robot.mapping;

import robot.navigation.*;
import robot.sensors.*;
import java.util.ArrayList;
import lejos.nxt.LCD;

public class Scanner {
	private Navigation2 nav;
	private USGather us;
	private double[] rs; //distances read by us 
	private double[] xs;
	private double[] ys;
	private double[] ts;
	private int[] ca; //array of closed angles
	private final double DIST_THRESH = 60.0;
	private final double DIST_OPEN = DIST_THRESH - 10.0;
	private final int OPEN_RANGE = 15;
	private final int NUM_POTENTIAL_OPEN = 12;
	private final int OPEN_ANGLE_INTERVAL = 30;
	
	private enum ThetaType { OPEN, CLOSED, UNKNOWN };
	private ThetaType[] tt;
	private double[] pos = new double[3];
	
	//constructor scanner and execution scan to be called from other threads
	public Scanner(Navigation2 nav, USGather us) {
		this.nav = nav;
		this.us = us;
	}

	public void scanRange(double ti, double tf) {
		//turn to initial
		nav.turnTo(ti, 0);
		while( !nav.isDone() ) {
			try { Thread.sleep(200); } catch(InterruptedException e){ }
		}
		
		reset();
		
		Odometer.getPosition(pos);
		double xi, yi, r, t;
		xi = pos[0];
		yi = pos[1];
		ArrayList<Double> tValues = new ArrayList<Double>();
		ArrayList<Double> rValues = new ArrayList<Double>();
		
		//turn while recording values
		nav.turnTo(tf, 1);
		while( !nav.isDone() ) {
			Odometer.getPosition(pos);
			r = us.getR();
			//mod to make sure array is under the size of 360
			t = pos[2];
			//only include values under distance threshold
			
			if(r < DIST_THRESH){
				tValues.add(t);
				rValues.add(r);
			}
			try { Thread.sleep(40); } catch(InterruptedException e){ }
		}
		
		// Convert the arrayLists to arrays
		ts = new double[tValues.size()];
		rs = new double[tValues.size()];
		xs = new double[tValues.size()];
		ys = new double[tValues.size()];
		
		for(int i=0; i < tValues.size(); i++){
			ts[i] = tValues.get(i);
			rs[i] = rValues.get(i);
			xs[i] = xi + rs[i] * Math.cos(Math.toRadians(ts[i]));
			ys[i] = yi + rs[i] * Math.sin(Math.toRadians(ts[i]));
			LCD.setPixel((int)(xs[i] - xi)/4 + 64, (int)(ys[i] - yi)/4 + 32, 1);
		}
		updateAngleInfo();
		
	}
	
	//methods to process data
	public int rangeClosed(double t) {
		double ti = t - OPEN_RANGE;
		double tf = t + OPEN_RANGE;
		for(int i = 0; i < ts.length; i++) {
			//false if a ts value is within the range and less that the distance threshold
			if(ts[i] > ti && ts[i] < tf && rs[i] < DIST_OPEN) {
				return 1;
			}
		}
		//else return true
		return 0;
	}
	public void updateAngleInfo() {
		ca = new int[NUM_POTENTIAL_OPEN];
		for(int i = 0; i < ca.length; i++) {
			//checking each 45 angle to see if its open
			ca[i] = rangeClosed(OPEN_ANGLE_INTERVAL*i);
		}

		for(int i = 0; i < ca.length; i++) {
			LCD.drawInt(ca[i], i, 4);
		}
		
	}
	public int bestOpenAngle(double ti, double tf, double xf, double yf) {
		int[] ca = getCA();
		int bestAngle = -1;
		
		Odometer.getPosition(pos);
		double tCenter = Math.toDegrees(Math.atan2(yf - pos[1], xf - pos[0]));
		
		for(int i = 0; i < ca.length; i++) {
			//if open, closest angle to desired, and within scan range: update best angle 
			if( ca[i] == 0 && Math.abs(i*OPEN_ANGLE_INTERVAL - tCenter) < Math.abs(bestAngle - tCenter)
				&& i*OPEN_ANGLE_INTERVAL > ti && i*OPEN_ANGLE_INTERVAL < tf) {
				bestAngle = i*OPEN_ANGLE_INTERVAL;
			}
		}
		//flagging no open with -1
		return bestAngle;
	}

	private void reset() {
		tt = new ThetaType[360];
		ts = new double[360];
		rs = new double[360];
		xs = new double[360];
		ys = new double[360];
		for(int i = 0; i < tt.length; i++ ) {
			tt[i] = ThetaType.UNKNOWN;
			rs[i] = -1;
			ts[i] = i;
			
		}
	}

	//getters
	public int[] getCA() {
		return ca;
	}
	public double[] getXs() {
		return xs;
	}
	public double[] getYs() {
		return ys;
	}


}