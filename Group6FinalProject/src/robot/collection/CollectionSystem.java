package robot.collection;

import robot.navigation.Navigation;
import robot.sensors.USGather;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.UltrasonicSensor;

/**
 * Positions the robot in such a way that it can collect the block then collects the block.
 * Also deals with releasing the stack after the cage is full.
 * 
 * @author Nathaniel
 * @version 1.1.0
 * @since
 */
public class CollectionSystem extends Thread {
	private static final int elevate = -280; //experimentally determined number of degrees the motor must rotate to reach a 45 degree angle.
    private static final int openClaw = 200; //experimentally determined number of degrees the motor must rotate to open the claw.
    
	private NXTRegulatedMotor cageMotor;
	private Navigation nav;
	private USGather us;
	
	private boolean done = true, captureBlock = false, releaseStack = false, hasBlock = false;
	
	/**
	 * The collection system requires access to the motor controlling the cage in order to collect the block. It also
	 * requires the Navigation method in order to orient the robot properly so that the block can be collected.
	 * @param cageMotor - The motor controlling the cage/claw
	 * @param nav - The Navigation class being used
	 */
	public CollectionSystem(NXTRegulatedMotor cageMotor, Navigation nav, USGather us) {
		this.cageMotor = cageMotor;
		this.nav = nav;
		this.us = us;
		
		this.start();
	}
	
    //cage will start at what position?  This is important...
    //Motor rotations will be defined to be positive when cage is moving down and negative when cage is moving up,
    //and negative when closing the cage and positive when opening the cage.
	
	@Override
	public void run(){
		while(true){
			if(captureBlock)
				collect();
			else if(releaseStack)
				release();
			else
				done = true;
			
			try{
				Thread.sleep(50);
			}
			catch(InterruptedException e){
				
			}
		}
	}

	// Determines if the robot needs to be oriented then collects the block
	private void collect(){
		// If robot needs to be oriented
		// orientRobot();
		if (!hasBlock) {
			cageMotor.rotate(-elevate + openClaw);
			nav.moveStraight(10);
			while(!nav.isDone());
			nav.reverse();
			while(us.getDistance() < 4) { try {Thread.sleep(50);} catch(InterruptedException e) {} }
			nav.stop();
		}
		captureBlock = false;
		cageMotor.rotate(-elevate + openClaw);
		Motor.A.rotate(360, true);
		Motor.B.rotate(360, false);
        cageMotor.rotate(-openClaw + elevate);
	}
	// Releases all the cages contents
	private void release(){
		cageMotor.rotate(-elevate + openClaw);
		releaseStack = true;
	}
	
	/**
	 * Initiates the collection of a block (it is assumed that the block has been identified and is therefore fairly close
	 * to the robot. However it is not necissarily oriented correctly.
	 * 
	 * @return void
	 */
	public void collectBlock() {
		captureBlock = true;
		done = false;
	}
	
	/**
	 * Tells the CollectionSystem to release the cages contents (i.e. opens and backs away)
	 * To be done at the end of the alloted time to place the stack in the stack zone. (or garbage in the garbage zone)
	 * @return void
	 */
	public void releaseStack() {
		releaseStack = true;
		done = true;
	}
	
	/**
	 * Return whether or not the robot has completed its task of collecting a block or releasing the entire stack.
	 * @return boolean: done
	 */
	public boolean isDone(){
		return done;
	}
	
	/**
	 * Lifts the cage (will be called on startup and after identification)
	 */
	public void raiseCage(){
		done = false;
		cageMotor.rotate(-elevate);
		done = true;
	}
	/**
	 * Lowers the cage (will be called before identification)
	 */
	public void lowerCage(){
		done = false;
		cageMotor.rotate(elevate);
		done = true;
	}
}
