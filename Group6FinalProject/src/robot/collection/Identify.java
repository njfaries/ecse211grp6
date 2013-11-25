package robot.collection;

import robot.navigation.Navigation;
import robot.navigation.Navigation2;
import robot.sensors.ColorGather;
import robot.sensors.USGather;

public class Identify {
	//must lower collection motor cage must be lowered to use ultrasonic sensor
	//also assumes the color sensor at a distance to check the color of the block 
	
	private ColorGather cg;
	private USGather us;
	private Navigation2 nav;
	private final int TESTING_DISTANCE = 21;
	private final int SENSOR_DISTANCE = 7;
	
	public Identify(ColorGather cg, USGather us, Navigation2 nav) {
		this.cg = cg;
		this.us = us;
		this.nav = nav;
	}
	
	public boolean isBlue() {
		
		if (cg.isBlue()) {
			nav.reverse();

			try{ Thread.sleep(1000); }
			catch(InterruptedException e){ }
			
			nav.stop();
			
			return true;
		}
		//not a blue block so backup to a constant and return false
		else {
			nav.reverse();

			try{ Thread.sleep(500); }
			catch(InterruptedException e){ }
			
			nav.stop();
			
			return false;
		}
	}
}
