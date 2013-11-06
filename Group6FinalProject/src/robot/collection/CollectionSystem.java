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
	
	private NXTRegulatedMotor clawMotor;
	private Navigation nav;
	
	private boolean done = true, captureBlock = false, releaseStack = false;
	
	public CollectionSystem(NXTRegulatedMotor clawMotor, Navigation nav) {
		this.clawMotor = clawMotor;
		this.nav = nav;
		
		this.start();
	}
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
	// Orients the robot if necessary and collects the block
	private void collect(){
		
		captureBlock = false;
	}
	// Releases all the cages contents
	private void release(){
		
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
