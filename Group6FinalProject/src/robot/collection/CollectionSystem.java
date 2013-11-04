package robot.collection;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

/**
 * Positions the robot in such a way that it can collect the block then collects the block.
 * Also deals with releasing the stack after the cage is full.
 * 
 * @author Nathaniel
 *
 */
public class CollectionSystem extends Thread {
	
	private NXTRegulatedMotor motor1;
	private NXTRegulatedMotor motor2;
	private NXTRegulatedMotor motor3;
	private NXTRegulatedMotor motor4;
	

	public CollectionSystem(NXTRegulatedMotor motor1, NXTRegulatedMotor motor2, NXTRegulatedMotor motor3, NXTRegulatedMotor motor4) {
		this.motor1 = motor1;
		this.motor2 = motor2;
		this.motor3 = motor3;
		this.motor4 = motor4;
	}
	
	public void collect() {
		
	}
	
	public void releaseCage() {
		
	}
	
}
