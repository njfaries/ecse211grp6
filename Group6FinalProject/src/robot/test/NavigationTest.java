package robot.test;
import robot.base.LCDInfo;
import robot.base.RobotController.RobotMode;
import robot.mapping.Map;
import robot.navigation.*;
import lejos.nxt.NXTRegulatedMotor;
import static org.mockito.Mockito.*;

/**
 * Test class.
 * Uses mocked classes for the purpose of testing navigation.
 * 
 * @author Andreas
 * @version 1.1.0
 * @since 2013-11-09
 */
public class NavigationTest {	
	
	private static NXTRegulatedMotor leftMotor;
	private static NXTRegulatedMotor rightMotor;
	
	private static TwoWheeledRobot robo;
	private static Navigation nav;
	
	 // This controls which points the navigation travels to
	double[][] waypoints = new double[][]{{30,30},{30,60},{60,60}};
	int wpIndex = 0;
	
	public static void main(String[] args) {
		new NavigationTest();
	}
	public NavigationTest(){
		new Map(RobotMode.STACKER);
		
		OdometryCorrection corrector = mock(OdometryCorrection.class);
		robo = new TwoWheeledRobot(leftMotor, rightMotor);
		
		new Odometer(robo, corrector);
		
		nav = new Navigation(robo);
		
		new LCDInfo();
	}
	// Runs all the control code (calling localization, navigation, identification, etc)
	public void run(){
		while(wpIndex < waypoints.length){
			
			navigate();
					
			try{ Thread.sleep(1000); }
			catch(InterruptedException e){ }
		}
	}
	// Handles navigating to a point (allows the scanner to continue in case an unexpected obstacle appears (i.e. the other player)
	private void navigate(){
		nav.travelTo(waypoints[wpIndex][0], waypoints[wpIndex][1]);
		
		while(!nav.isDone()){
			try{ Thread.sleep(200); }
			catch(InterruptedException e){ }
		}
		
		wpIndex++;
	}
}

