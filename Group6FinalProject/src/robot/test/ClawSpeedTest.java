package robot.test;

import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;

/**
 * 
 * @author nfarie
 * @version 1.0
 * @since 2013-11-23
 */

public class ClawSpeedTest extends Thread {
	private static int CLAW_SPEED = 2000;  //speed in deg/s
	private static int CLAW_TIME = 2500;  //time in ms
	private NXTRegulatedMotor motor = new NXTRegulatedMotor(MotorPort.C);
	
	public static void main(String[] args) {
		new ClawSpeedTest();
	}
	
	public ClawSpeedTest() {
		motor.rotate(-330);
		this.start();
	}
	
	public void run() {
		motor.setSpeed(CLAW_SPEED);
		motor.forward();
		try { Thread.sleep(CLAW_TIME); } catch(InterruptedException e) {}
		motor.stop();
		try { Thread.sleep(500); } catch(InterruptedException e) {}
		motor.backward();
		try { Thread.sleep(CLAW_TIME); } catch(InterruptedException e) {}
		motor.stop();
		System.exit(0);
	}
}
