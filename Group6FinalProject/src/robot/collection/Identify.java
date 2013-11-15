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
	private final double BACKUP_DISTANCE = 10.0;	
	
	public Identify(ColorGather cg, USGather us, Navigation2 nav) {
		this.cg = cg;
		this.us = us;
		this.nav = nav;
	}
	
	public boolean isBlue() {
		double distFromSensorToBlock = us.getRawDistance() - 14;
		if(distFromSensorToBlock > 21){
			nav.move();
		
			while(distFromSensorToBlock > 21){
				try{ Thread.sleep(10); }
				catch(InterruptedException e){ }
				
				distFromSensorToBlock = us.getRawDistance() - 14;
			}
			
			nav.stop();
		}
		
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

			try{ Thread.sleep(1500); }
			catch(InterruptedException e){ }
			
			nav.stop();
			
			return false;
		}
	}
}
