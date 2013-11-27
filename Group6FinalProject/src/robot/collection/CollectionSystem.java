package robot.collection;

import robot.navigation.Navigation;
import robot.navigation.Navigation2;
import lejos.nxt.NXTRegulatedMotor;

/**
 * Positions the robot in such a way that it can collect the block then collects the block.
 * Also deals with releasing the stack after the cage is full.
 * 
 * @author Nathaniel
 * @version 1.2.0
 * @since
 */
public class CollectionSystem {
	
    private static final int CLAW_SPEED = 150; //speed in degrees per second
    private static final int CLAW_TIME = 3300; //time in ms
    private static final int RAISE_TIME = 2000; //time in ms
    private static final int OPEN_TIME = 1300; //time in ms
    
	private NXTRegulatedMotor cageMotor;
	private Navigation2 nav;
	
	/**
	 * The collection system requires access to the motor controlling the cage in order to collect the block. It also
	 * requires the Navigation method in order to orient the robot properly so that the block can be collected.
	 * @param cageMotor - The motor controlling the cage/claw
	 * @param nav - The Navigation class being used
	 */
	public CollectionSystem(NXTRegulatedMotor cageMotor, Navigation2 nav) {
		this.cageMotor = cageMotor;
		this.nav = nav;
		this.cageMotor.setSpeed(CLAW_SPEED);
	}
	
	/**
	 * Initiates the collection of a block (it is assumed that the block has been identified and is therefore fairly close
	 * to the robot. However it is not necissarily oriented correctly.
	 * 
	 * @return void
	 */
	public void collect() {
		cageMotor.forward();
		try {Thread.sleep(CLAW_TIME);} catch(InterruptedException e) {}
		cageMotor.stop();
		
		try {Thread.sleep(CLAW_TIME);} catch(InterruptedException e) {}
		
		cageMotor.backward();
		try {Thread.sleep(CLAW_TIME);} catch(InterruptedException e) {}
        cageMotor.stop();
	}
	
	/**
	 * Tells the CollectionSystem to release the cages contents (i.e. opens and backs away)
	 * To be done at the end of the alloted time to place the stack in the stack zone. (or garbage in the garbage zone)
	 * @return void
	 */
	public void release() {
		cageMotor.forward();
		try {Thread.sleep(CLAW_TIME);} catch(InterruptedException e) {}
		cageMotor.stop();
		
		nav.reverse();
		try {Thread.sleep(3000);} catch(InterruptedException e) {} //arbitrary number
		nav.stop();
		
		cageMotor.backward();
		try {Thread.sleep(CLAW_TIME);} catch(InterruptedException e) {}
		cageMotor.stop();
	}
		
	/**
	 * Lifts the cage (will be called on startup and after identification)
	 */
	public void raiseCage() {
		cageMotor.backward();
		try {Thread.sleep(RAISE_TIME);} catch(InterruptedException e) {}
		cageMotor.stop();
	}
	/**
	 * Lowers the cage (will be called before identification)
	 */
	public void lowerCage() {
		cageMotor.forward();
		try {Thread.sleep(RAISE_TIME);} catch(InterruptedException e) {}
		cageMotor.stop();
	}
	/**
	 * Opens the cage
	 */
	public void openCage() {
		cageMotor.forward();
		try {Thread.sleep(OPEN_TIME);} catch(InterruptedException e) {}
		cageMotor.stop();
	}
	/**
	 * Closes the cage (will be called before identification)
	 */
	public void closeCage() {
		cageMotor.backward();
		try {Thread.sleep(OPEN_TIME);} catch(InterruptedException e) {}
		cageMotor.stop();
	}
	/**
	 * Rotates the cage a certain distance.  Converts value passed in degrees into an amount of time to rotate.
	 * @param degrees
	 */
	public void rotateCage(int degrees) {
		int time = (int) ((degrees/150.0) * 1000);	//Explicitly wrote out value for CLAW_TIME to force floating point division.
		if (time > 0) {
			cageMotor.forward();
			try {Thread.sleep(time);} catch(InterruptedException e) {}
			cageMotor.stop();
		} else {
			time = -time;
			cageMotor.backward();
			try {Thread.sleep(time);} catch(InterruptedException e) {}
			cageMotor.stop();
		}
	}
}
