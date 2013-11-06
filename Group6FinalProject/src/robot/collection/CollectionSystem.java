/*package robot.collection;
import robot.navigation.Navigation;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class CollectionSystem extends Thread {	//why is this a thread with no run() method?
	
	private NXTRegulatedMotor cageMotor;
	private NXTRegulatedMotor motor2;
	private NXTRegulatedMotor motor3;
	private NXTRegulatedMotor motor4;
	private static final int elevate = 180; //experimentally determined number of degrees the motor must rotate to reach a 45 degree angle.
	private static final int openClaw = -140; //experimentally determined number of degrees the motor must rotate to open the claw.
	
	
	public CollectionSystem(NXTRegulatedMotor motor1, NXTRegulatedMotor motor2, NXTRegulatedMotor motor3, NXTRegulatedMotor motor4) {
		cageMotor = motor1;
		this.motor2 = motor2;
		this.motor3 = motor3;
		this.motor4 = motor4;
	}
	
	//cage will start at what position?  This is important...
	//Motor rotations will be defined to be positive when cage is moving up and negative when cage is moving down,
	//and negative when opening the cage and positive when closing the cage.
	
	
	//This design operates by first navigating the robot to an appropriate position.  This method is responsible to
	//lower the cage over the block and loosen to allow the new block to come into the cage.
	public void collectD1() {
		//assume cage starts at an angle of 45 degrees
		//positioning?
			//robot.navigation.Navigation.turnTo(angle); --> rotate robot so cage is over the discovered block.
		cageMotor.rotate(-elevate + openClaw);
		cageMotor.rotate(-openClaw + elevate);
		//...and that's all, folks.
	}
	
	//This design uses tread system and assumes a feedback system from the cage to confirm that the brick has fallen into the cage
	public void collectD2() {
		//while (brickNotInCage) --> some method/sensor feedback here
		//motors rotate
	}
	
	public void releaseCage() {
		cageMotor.rotate(-elevate + openClaw); //puts tower down on the ground.
		//drive away...whichever direction is necessary.
=======*/
package robot.collection;

import robot.navigation.Navigation;
import lejos.nxt.NXTRegulatedMotor;

/**
 * Positions the robot in such a way that it can collect the block then collects the block.
 * Also deals with releasing the stack after the cage is full.
 * 
 * @author Nathaniel
 * @version 1.0.0
 * @since
 */
public class CollectionSystem extends Thread {
	private static final int elevate = 180; //experimentally determined number of degrees the motor must rotate to reach a 45 degree angle.
    private static final int openClaw = -140; //experimentally determined number of degrees the motor must rotate to open the claw.
    
	private NXTRegulatedMotor cageMotor;
	private Navigation nav;
	
	private boolean done = true, captureBlock = false, releaseStack = false;
	
	/**
	 * The collection system requires access to the motor controlling the cage in order to collect the block. It also
	 * requires the Navigation method in order to orient the robot properly so that the block can be collected.
	 * @param cageMotor - The motor controlling the cage/claw
	 * @param nav - The Navigation class being used
	 */
	public CollectionSystem(NXTRegulatedMotor cageMotor, Navigation nav) {
		this.cageMotor = cageMotor;
		this.nav = nav;
		
		this.start();
	}
	
    //cage will start at what position?  This is important...
    //Motor rotations will be defined to be positive when cage is moving up and negative when cage is moving down,
    //and negative when opening the cage and positive when closing the cage.
	
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
		
		captureBlock = false;
		cageMotor.rotate(-elevate + openClaw);
        cageMotor.rotate(-openClaw + elevate);
	}
	// Orients the robot
	private void orientRobot(){
		// Orients the robot
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
}
