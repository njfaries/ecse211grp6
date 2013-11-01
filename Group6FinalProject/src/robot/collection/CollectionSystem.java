package robot.collection;
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
	}
	
}
