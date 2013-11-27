package robot.base;

import robot.bluetooth.*;
import robot.collection.*;
import robot.localization.Localization;
import robot.mapping.*;
import robot.navigation.*;
import robot.sensors.*;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

/**
 * Contains the main method for the robot. Initiates classes and passes them the
 * necessary motors, sensors, and various constants. Controls and and delegates
 * tasks to various subroutines.
 * 
 * @author Andreas, Nathaniel
 * @version 1.2.0
 * @since 2013-11-04
 */
public class RobotController extends Thread {
	public enum FunctionType {
		IDLE, LOCALIZE, BOARD_NAVIGATE, RELEASE, RETURN
	};

	public enum RobotMode {
		STACKER, GARBAGE
	};
	private static NXTRegulatedMotor leftMotor = new NXTRegulatedMotor(MotorPort.A);
	private static NXTRegulatedMotor rightMotor = new NXTRegulatedMotor(MotorPort.B);
	private static NXTRegulatedMotor cageMotor = new NXTRegulatedMotor(MotorPort.C);

	private static UltrasonicSensor usFront = new UltrasonicSensor(SensorPort.S4);
	private static UltrasonicSensor usTop = new UltrasonicSensor(SensorPort.S3);
	
	private static ColorSensor csLeft = new ColorSensor(SensorPort.S1);
	private static ColorSensor csRight = new ColorSensor(SensorPort.S2);

	private CollectionSystem collection;
	private Navigation2 nav;
	private TwoWheeledRobot robo;

	private USGather us;
	private ColorGather cg;

	private Localization loc;
	
	StartCorner corner = StartCorner.BOTTOM_LEFT;
	PlayerRole role = PlayerRole.BUILDER;
	int[] greenZone = new int[] {165,165,195,195};
	int[] redZone  = new int[4];
	int[] startCenter = new int[2];
	double[] endCenter;
	private int blocksCollected = 0;
	private int maxBlocks = 2;
	
	private FunctionType function = FunctionType.LOCALIZE;

	private double[] pos = new double[3];

	public static void main(String[] args) {
		new RobotController();
	}

	/**
	 * The robot controller delegates the starting and ending of various
	 * subtasks like localization, searching and collection.
	 */
	public RobotController() {
		//receive();
		
		new Map2(role, redZone, greenZone);
		endCenter = Map2.getEndCenter();
		
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		nav = new Navigation2(robo);
		
		collection = new CollectionSystem(cageMotor, nav);
		collection.rotateCage(-325);
		
		int buttonChoice = Button.waitForAnyPress();
		while (buttonChoice != Button.ID_ENTER){}
		
		us = new USGather(usFront, usTop);
		cg = new ColorGather(csLeft, csRight, new OdometryCorrection());
		
		//need to construct localization with transmission.startingCorner
		loc = new Localization(us, cg, StartCorner.BOTTOM_LEFT, nav);
		
		//corrector = new OdometryCorrection(cg, WHEEL_RADIUS, ODOCORRECT_SENS_WIDTH, ODOCORRECT_SENS_DIST);
		new Odometer(robo);

		//id = new Identify(cg, us, nav);
				
		this.start();
	}

	// Runs all the control code (calling localization, navigation,
	// identification, etc)
	public void run() {
		boolean running = true;
		while (running) {
			switch(function) {
				case IDLE:
					try { Thread.sleep(500); }
					catch(InterruptedException e){ }
					break;
				case LOCALIZE:
					localize();
					function = FunctionType.BOARD_NAVIGATE;
					break;
				case BOARD_NAVIGATE:
					navigateBoard(1);
					break;
				case RELEASE:
					release();
					break;
				case RETURN:
					returnToStart(1);
					running = false;
					break;
			}
			
			try { Thread.sleep(50); } catch (InterruptedException e) { }
		}
	}

	// Receives instruction via bluetooth
	private void receive() {
		BluetoothConnection conn = new BluetoothConnection();
		// as of this point the bluetooth connection is closed again, and you can pair to another NXT (or PC) if you wish
		
		Transmission t = conn.getTransmission();
		if (t == null) {
			LCD.drawString("Failed to read transmission", 0, 5);
		} else {
			corner = t.startingCorner;
			role = t.role;
			// green zone is defined by these (bottom-left and top-right) corners:
			greenZone = t.greenZone;
			
			// red zone is defined by these (bottom-left and top-right) corners:
			redZone = t.redZone;
		}
		// stall until user decides to end program
		Button.waitForAnyPress();
	}

	// Initiates the localization of the robot
	private void localize() {
		loc.localize();
		Odometer.getPosition(pos);
		startCenter[0] = (int)pos[0] - 45;
		startCenter[1] = (int)pos[1] - 45;
	}
	
	private void navigateBoard(int mode){
		// If done, return
		LCD.clear(1);
		LCD.drawString("Navigating",0,1);
		Odometer.getPosition(pos);
		double distance = Math.sqrt(Math.pow(endCenter[0] - pos[0],2) + Math.pow(endCenter[1] - pos[1],2));
		if(distance < 15){
			nav.stop();
			function = FunctionType.RELEASE;
			return;
		}
		
		// Back up a bit
		if(mode != 1){
			nav.reverse();
			try { Thread.sleep(1000); } 
			catch (InterruptedException e) {}
			nav.stop();
		}
		
		// Turn either by +-90degrees or to face the green (or red) zone
		Odometer.getPosition(pos);
		LCD.drawString(endCenter[0] + "," + endCenter[1],0,0);
		double turnAngle = Math.toDegrees(Math.atan2(endCenter[1] - pos[1], endCenter[0] - pos[0]));
		
		if(mode != 1){
			if((pos[2] > 45 && pos[2] < 135) || (pos[2] > 225 && pos[2] < 315))
				turnAngle = pos[2] - 80;
			else
				turnAngle = pos[2] + 80;
		}
		// Fix angle if needed
		if(turnAngle < 0)
			turnAngle += 360;
		turnAngle = turnAngle % 360;
		
		if(Map2.intersectsAvoidZone(turnAngle))
			navigateBoard(mode);
		
		// Turn
		nav.turnTo(turnAngle, 0);
		while(!nav.isDone()){
			try { Thread.sleep(250); } 
			catch (InterruptedException e) {}
		}
		nav.stop();
		
		// Go forward until an obstacle is found
		nav.move();
		long startTime = System.currentTimeMillis();
		while(!us.flagObstruction() && !us.criticalFlag()){
			if(mode != 1){
				long timeElapsed = System.currentTimeMillis() - startTime;
				double distanceTraveled = timeElapsed * Odometer.getSpeed() / 1000;
				if(distanceTraveled >= 60)
					break;
			}

			Odometer.getPosition(pos);
			distance = Math.sqrt(Math.pow(endCenter[0] - pos[0],2) + Math.pow(endCenter[1] - pos[1],2));
			if(distance < 15){
				nav.stop();
				function = FunctionType.RELEASE;
				return;
			}
			
			try { Thread.sleep(50); } 
			catch (InterruptedException e) {}
		}
		nav.stop();
		
		try { Thread.sleep(300); } 
		catch (InterruptedException e) {}
		
		if(!us.criticalFlag())
			approachBlock();

		try { Thread.sleep(300); } 
		catch (InterruptedException e) {}
		
		// If there is a blue block, collect it
		if(us.getZType() == USGather.HeightCategory.BLUE_BLOCK)
			collect();
		else if(us.getZType() == USGather.HeightCategory.WOODEN_BLOCK)// Otherwise call this again
			navigateBoard(-1);
		else
			navigateBoard(1);
	}
	// While an obstruction is flagged and no block is seen, continue forward (may return instantly)
	private void approachBlock(){
		nav.stop();
		
		LCD.clear(1);
		LCD.drawString("approaching",0,1);
		
		boolean veryClose = false;
		nav.move();
		long startTime = System.currentTimeMillis();
		double distance = 0;
		
		while(us.flagObstruction() && !us.criticalFlag() && !us.flagImminent() && distance < 60){
			long elapsedTime = System.currentTimeMillis() - startTime;
			distance = Odometer.getSpeed() * elapsedTime / 1000;
			
			if(us.flagError()){
				veryClose = true;
				break;
			}
			try { Thread.sleep(50); } 
			catch (InterruptedException e) {}
		}
		nav.stop();
		
		try { Thread.sleep(100); } 
		catch (InterruptedException e) {}
		
		if(us.flagImminent() && us.getZType() != USGather.HeightCategory.WOODEN_BLOCK)
			collect();
		else if(veryClose){
			nav.reverse();
			try { Thread.sleep(1000); } 
			catch (InterruptedException e) {}
			nav.stop();
			
			if(us.flagImminent())
				collect();
		}
		else if(!us.flagError() && !us.flagImminent() && !us.flagObstruction() && !us.criticalFlag()){
			overshot();
		}
	}

	//Handles overshooting a block
	private void overshot(){
		LCD.clear(1);
		LCD.drawString("Overshot",0,1);
		Odometer.getPosition(pos);
		
		double turnAngle = pos[2] + 45;
		turnAngle = turnAngle % 360;
		
		nav.turnTo(turnAngle, 1);
		while(!us.flagObstruction() && !us.criticalFlag() && !nav.isDone()){
			try { Thread.sleep(50); } 
			catch (InterruptedException e) {}
		}
		nav.stop();
		
		if(us.flagObstruction() && !us.criticalFlag())
			approachBlock();
		else{
			turnAngle = pos[2] - 90;
			if(turnAngle < 0)
				turnAngle += 360;
			
			nav.turnTo(turnAngle, 2);
			while(!us.flagObstruction() && !us.criticalFlag() && !nav.isDone()){
				try { Thread.sleep(50); } 
				catch (InterruptedException e) {}
			}
			nav.stop();
			
			if(us.flagObstruction() && !us.criticalFlag())
				approachBlock();
		}
	}
	// Collects said block
	private void collect() {
		if(blocksCollected == 0){
			nav.reverse();
			try { Thread.sleep(1500); } 
			catch (InterruptedException e) {}
			nav.stop();
			
			collection.lowerCage();
			collection.openCage();
			
			nav.move();
			try { Thread.sleep(3500); } 
			catch (InterruptedException e) {}
			nav.stop();
			
			try { Thread.sleep(250); } 
			catch (InterruptedException e) {}
			
			collection.closeCage();
			collection.raiseCage();
			
			blocksCollected++;
		}
		else{
			nav.move();
			try {Thread.sleep(3000);} catch(InterruptedException e) {}
			nav.stop();
			
			nav.reverse();
			try {Thread.sleep(100);} catch(InterruptedException e) {}
			//while(us.getDistance() > 5);
			nav.stop();
			
			collection.lowerCage();
			collection.openCage();
			
			nav.move();
			try {Thread.sleep(500);}
			catch (InterruptedException e) {}
			nav.stop();
			
			collection.closeCage();
			collection.raiseCage();
		}
		
		blocksCollected++;
		
		navigateBoard(1);
		//function = FunctionType.OPEN_NAVIGATE;
	}

	// Releases the entire stack (only done at the end of the match)
	private void release() {
		collection.release();
		function = FunctionType.RETURN;
	}
	
	private void returnToStart(int mode){
		// If done, return
		Odometer.getPosition(pos);
		int[] endCenter = startCenter;
		double distance = Math.sqrt(Math.pow(endCenter[0] - pos[0],2) + Math.pow(endCenter[1] - pos[1],2));
		if(distance < 15)
			return;
		
		// Back up a bit
		if(mode != 0){
			nav.reverse();
			try { Thread.sleep(1000); } 
			catch (InterruptedException e) {}
			nav.stop();
		}
		
		
		// Turn either by +-90degrees or to face the green (or red) zone
		Odometer.getPosition(pos);
		double turnAngle = 0;
		
		LCD.drawString(endCenter[0] + "," + endCenter[1],0,0);
		double heading = Math.toDegrees(Math.atan2(endCenter[1] - pos[1], endCenter[0] - pos[0]));
		if(mode == 1)
			turnAngle = heading;
		else if(mode != 0){
			if((pos[2] > turnAngle && turnAngle < 180) || (turnAngle > 180 && pos[2] < turnAngle))
				turnAngle = pos[2] - 80;
			else
				turnAngle = pos[2] + 80;
		}
		// Fix angle if needed
		if(turnAngle < 0)
			turnAngle += 360;
		turnAngle = turnAngle % 360;
		
		while(Map2.intersectsAvoidZone(turnAngle)){
			if((pos[2] > 45 && pos[2] < 135) || (pos[2] > 225 && pos[2] < 315))
				turnAngle -= 10;
			else
				turnAngle += 10;
		}
		
		if(turnAngle < 0)
			turnAngle += 360;
		turnAngle = turnAngle % 360;
		
		// Turn
		nav.turnTo(turnAngle, 0);
		while(!nav.isDone()){
			try { Thread.sleep(250); } 
			catch (InterruptedException e) {}
		}
		nav.stop();
		
		// Go forward until an obstacle is found
		nav.move();
		long startTime = System.currentTimeMillis();
		while(!us.flagObstruction() && !us.criticalFlag()){
			if(mode == -1){
				long timeElapsed = System.currentTimeMillis() - startTime;
				double distanceTraveled = timeElapsed * Odometer.getSpeed() / 1000;
				if(distanceTraveled >= 45)
					break;
			}

			try { Thread.sleep(50); } 
			catch (InterruptedException e) {}
		}
		nav.stop();
		
		if(!us.criticalFlag())
			returnToStart(0);
		else if(mode == 0)
			returnToStart(-1);
		else
			returnToStart(mode * -1);
			
	}

}
