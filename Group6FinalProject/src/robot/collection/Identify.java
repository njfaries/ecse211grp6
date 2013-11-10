package robot.collection;

import robot.navigation.Navigation;
import robot.sensors.ColorGather;
import robot.sensors.USGather;

public class Identify {
	//must lower collection motor cage must be lowered to use ultrasonic sensor
	//also assumes the color sensor at a distance to check the color of the block 
	
	private ColorGather cg;
	private USGather us;
	private Navigation nav;
	private final double BACKUP_DISTANCE = 10.0;
	private boolean done = false;
	
	public Identify(ColorGather cg, USGather us, Navigation nav) {
		this.cg = cg;
		this.us = us;
		this.nav = nav;
	}
	
	public boolean needToCollectBlue() {
		// Maybe move forward towards the block
		//if the block is blue, flag for collection by returning true
		if (cg.isBlue() == true) {
			 return true;
		}
		//not a blue block so backup to a constant and return false
		else {
			nav.moveStraight(-BACKUP_DISTANCE);
			while(!nav.isDone()) { }
			return false;
		}
	}
}
