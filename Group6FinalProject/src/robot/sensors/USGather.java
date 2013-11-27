package robot.sensors;

import java.util.Arrays;

import robot.navigation.Odometer;
import lejos.util.Timer;
import lejos.util.TimerListener;
import lejos.nxt.LCD;
import lejos.nxt.UltrasonicSensor;

public class USGather implements TimerListener {
	
	public enum HeightCategory {WOODEN_BLOCK, BLUE_BLOCK, FLOOR};
	private HeightCategory zType = HeightCategory.FLOOR;
	private double r, oldR,x,y,z,floorDistance;
	private final int SEN_TO_CENTER = 8;
	private final int BLUE_WOODEN_THRESH = 11;
	private final int FLOOR_BLUE_THRESH = 3;
	private final int SLEEP_TIME = 5;
	private final int TIMER_PERIOD = 25;
	private UltrasonicSensor usXY;
	private UltrasonicSensor usZ;
	private Object lock;
	private double[] pos = new double[3];

	private double[] topValues = new double[3];
	private int topIndex = 0;
	
	public USGather( UltrasonicSensor usXY, UltrasonicSensor usZ )  {
		this.usXY = usXY;
		this.usZ = usZ;
		lock = new Object();
		//initialize distance from top sensor to floor
		do {
			this.floorDistance = usZ.getDistance();
		} while(usZ.getDistance() == 255);
		
		Timer timer = new Timer(TIMER_PERIOD, this);
		timer.start();
	}
	
	public void timedOut() {
		synchronized(lock){
			updateXY();
			updateZ();
		}
		LCD.clear(3);
		LCD.clear(4);
		LCD.clear(5);
		LCD.clear(6);		
		LCD.drawString(( "usR: " + (int)getR() ), 0, 3);
		LCD.drawString(( "usX: " + (int)getX() ), 0, 4);
		LCD.drawString(( "usY: " + (int)getY() ), 8, 4);
		LCD.drawString(( "usZ: " + (int)getZ() ), 0, 5);
		LCD.drawString(( "usZType: " + getZType() ), 0, 6);
	}
	
	//methods to update x,y and z values
	private void updateXY() {
		//do a ping
		usXY.ping();
		//wait for the ping to complete
		try { Thread.sleep(SLEEP_TIME); } catch (InterruptedException e) {}
		
		//if 255 return without update
		if( usXY.getDistance() != 255 ) {
			Odometer.getPosition(pos);
			oldR = r;
			r = usXY.getDistance() + 2 * SEN_TO_CENTER;
			
			x = pos[0] + r * Math.cos(Math.toRadians(pos[2]));
			y = pos[1] + r * Math.sin(Math.toRadians(pos[2]));
		}
		else{
			r = -1;
		}
	}
	private void updateZ() {
		//do a ping
		usZ.ping();
		//wait for the ping to complete
		try { Thread.sleep(SLEEP_TIME); } catch (InterruptedException e) {}
		//if 255 return without update
		if( usZ.getDistance() != 255 ) {
			double zDistance = usZ.getDistance();			
			z = floorDistance - zDistance;
			if (z < 0) z = 0;
			if( z < FLOOR_BLUE_THRESH ) this.zType = HeightCategory.FLOOR;
			else if( z < BLUE_WOODEN_THRESH ) this.zType = HeightCategory.BLUE_BLOCK;
			else this.zType = HeightCategory.WOODEN_BLOCK;
		}	
	}
	
	//getters
	public double getR() {
		synchronized (lock) {
			return r;
		}
	}
	
	public double getX() {
		double result;
		synchronized (lock) {
			result = x;
		}
		return result;
	}
	public double getY() {
		double result;
		synchronized (lock) {
			result = y;
		}
		return result;
	}
	public double getZ() {
		double result;
		synchronized (lock) {
			result = z;
		}
		return result;
	}
	public HeightCategory getZType() {
		HeightCategory result;
		synchronized (lock) {
			result = zType;
		}
		return result;
	}
	public boolean flagObstruction() {
		if (r < 60 && r  > 0)
			return true;
		return false;
	}
	public boolean flagError() {
		if (r <= 0 || (r >= 40 && r <= 45 && oldR < 28))
			return true;
		return false;
	}
	public boolean flagImminent() {
		if (r  > 0 && r < 25)
			return true;
		return false;
	}
	public boolean criticalFlag() {
		if (zType != HeightCategory.FLOOR)
			return true;
		return false;
	}
}
