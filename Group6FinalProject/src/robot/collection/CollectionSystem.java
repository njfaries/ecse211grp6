package robot.collection;

import robot.navigation.Navigation;
import lejos.nxt.NXTRegulatedMotor;

/**
 * Positions the robot in such a way that it can collect the block then collects the block.
 * Also deals with releasing the stack after the cage is full.
 * 
 * @author Nathaniel
 * @version 1.1.0
 * @since
 */
public class CollectionSystem {
	private static final int elevate = 300; //experimentally determined number of degrees the motor must rotate to reach a 45 degree angle.
    private static final int openClaw = 200; //experimentally determined number of degrees the motor must rotate to open the claw.
    
	private NXTRegulatedMotor cageMotor;
	private Navigation nav;
	
	/**
	 * The collection system requires access to the motor controlling the cage in order to collect the block. It also
	 * requires the Navigation method in order to orient the robot properly so that the block can be collected.
	 * @param cageMotor - The motor controlling the cage/claw
	 * @param nav - The Navigation class being used
	 */
	public CollectionSystem(NXTRegulatedMotor cageMotor, Navigation nav) {
		this.cageMotor = cageMotor;
		this.nav = nav;
	}
	
    //cage will start at what position?  This is important...
    //Motor rotations will be defined to be positive when cage is moving up and negative when cage is moving down,
    //and negative when opening the cage and positive when closing the cage.
	
	/**
	 * Initiates the collection of a block (it is assumed that the block has been identified and is therefore fairly close
	 * to the robot. However it is not necissarily oriented correctly.
	 * 
	 * @return void
	 */
	public void collect(){
		// If robot needs to be oriented
		// orientRobot();
		cageMotor.rotate(openClaw + elevate);
		cageMotor.rotate(-elevate - openClaw);
        
	}
	// Orients the robot
	private void orientRobot(){
		// Orients the robot
	}
	/**
	 * Tells the CollectionSystem to release the cages contents (i.e. opens and backs away)
	 * To be done at the end of the alloted time to place the stack in the stack zone. (or garbage in the garbage zone)
	 * @return void
	 */
	public void release(){
		cageMotor.rotate(elevate + openClaw);
	}
		
	/**
	 * Lifts the cage (will be called on startup and after identification)
	 */
	public void raiseCage(){
		cageMotor.rotate(-elevate);
	}
	/**
	 * Lowers the cage (will be called before identification)
	 */
	public void lowerCage(){
		cageMotor.rotate(elevate);
	}
	/**
	 * Opens the cage
	 */
	public void openCage(){
		cageMotor.rotate(openClaw);
	}
	/**
	 * Closes the cage (will be called before identification)
	 */
	public void closeCage(){
		cageMotor.rotate(-openClaw);
	}
	/**
	 * Rotates the cage a certain distance
	 * @param degrees
	 */
	public void rotateCage(int degrees){
		cageMotor.rotate(degrees);
	}
}
