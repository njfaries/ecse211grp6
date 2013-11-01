package robot.collection;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class CollectionSystem extends Thread {	//why is this a thread with no run() method?
	
	private NXTRegulatedMotor cageMotor;
	private NXTRegulatedMotor motor2;
	private NXTRegulatedMotor motor3;
	private NXTRegulatedMotor motor4;
	
	public CollectionSystem(NXTRegulatedMotor motor1, NXTRegulatedMotor motor2, NXTRegulatedMotor motor3, NXTRegulatedMotor motor4) {
		cageMotor = motor1;
		this.motor2 = motor2;
		this.motor3 = motor3;
		this.motor4 = motor4;
	}
	
	//cage will start at what position?  This is important...
	//Motor rotations will be defined to be positive when cage is moving up and negative when cage is moving down.
	
	public void collect() {
		
	}
	
	public void releaseCage() {
		
	}
	
}
