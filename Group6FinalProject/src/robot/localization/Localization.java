package robot.localization;
import robot.sensors.ColorGather;
import robot.sensors.USGather;
import robot.bluetooth.StartCorner;
import robot.navigation.*;
import lejos.nxt.*;

/**
* The localization class responsible for getting the precise position of the robot.
*
* @author Nathaniel
* @version 1.1.0
* @since 2013-11-09
*
*/

public class Localization {
        //public enum StartCorner {BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT};
		private final int US_OFFSET = 8;                                //Measured value (distance from centre)
        private final double LS_OFFSET_ANGLE = 29.74488;//Measured value (degrees from central axis)
        private final double LS_OFFSET_DIST = 7;                //Measured value (distance from centre of rotation)
        private final int WALL_DISTANCE = 40;                         //Arbitrary value. Not tested
        private final int ROTATION_SPEED = 300;                 //Needs to be tested
        private Navigation2 nav;        
        private USGather usGather;
        private ColorGather colorGather;
        private StartCorner corner;
        private int angleAdjustment = 0;
        
        
        public Localization(USGather us, ColorGather cg, StartCorner corner, Navigation2 nav) {
                this.corner = corner;
                usGather = us;
                colorGather = cg;
                this.nav = nav;
                
        }
        
	public void localize() {
        	switch(corner) {
                case BOTTOM_LEFT:	angleAdjustment = 0;
                case BOTTOM_RIGHT:	angleAdjustment = 270;
                case TOP_RIGHT:		angleAdjustment = 180;
                case TOP_LEFT: 		angleAdjustment = 90;
                default:			angleAdjustment = 0;
        	} 
                usLocalization();
                LCD.drawString("us done", 0, 3);

                nav.turnTo(0, 0);
                while(!nav.isDone()){
                	try { Thread.sleep(500); }  catch (InterruptedException e) {}
                }
                nav.stop();
                
                lightLocalization();
                
                nav.travelTo(60,60);
                while(!nav.isDone()){
                	try { Thread.sleep(500); }  catch (InterruptedException e) {}
                }   
                nav.stop();
        }
        
        /**
         * The method responsible for handling the ultrasonic part of localization. It will
         * set the heading in the odometer.
         */        
        
	public void usLocalization() {
		double angleA = 0; double angleB = 0;
		double[] array = new double[3];                        //Just to make getPosition happy...
                
/*		nav.rotate(0); 
		while (usGather.getRawDistance() < (WALL_DISTANCE + 2 * US_OFFSET)) {                                //If starts facing a wall it will rotate until
			try { Thread.sleep(20); } catch (InterruptedException e) {}                                                                                                       //it is no longer facing wall.
		}
		nav.stop();*/
             
		nav.rotate(0);
		while(true) {        //while still localizing, may put bool variable here later.                        
			if (usGather.getRawDistance() < (WALL_DISTANCE + 2 * US_OFFSET)) {                                //Once the rising edge is detected...
				nav.stop(); 
				//from the odometer
				Odometer.getPosition(array);
                                
				if (angleA == 0) {                                                                                                        //If this is the first time it's stopped (angleA hasn't
					angleA = Math.toRadians(array[2]);                                              
				} 
				else {
					angleB = Math.toRadians(array[2]);
					break;
				}
				
                //been changed) then set angleA and reverse the direction.                                                                                            //The sleep is to ensure that the same edge is not picked
				nav.rotate(1);
				try { Thread.sleep(1000); } catch (InterruptedException e) {}        //up again.
			}
			
			try { Thread.sleep(50); }  catch (InterruptedException e) {}   
		}
		array[2] = Math.toDegrees(calculateUS(angleA, angleB)) + angleAdjustment;                        //Calculates the true angle and adjusts for corner.
		Odometer.setPosition(array, new boolean [] {false, false, true});        //Sets the theta in the odometer, but NOT x and y.                                        
		
	}
        
        /**
         * The method responsible for handling the light sensor part of localization.
         * It will set the x and y of the odometer.
         */
        
        public void lightLocalization() {
        	LCD.drawString("li start", 0, 4);
                boolean direction = true;
                int counter = 0;
                double[] angles = new double[4];
                double[] array = new double[3];
                
                boolean isOnLine = false;
                
                nav.rotate(0);
                while(counter < 4) {
                        if (colorGather.isOnLine(0) && !isOnLine) {				//0 = left sensor on robot
                                Odometer.getPosition(array);
                                angles[counter] = array[2] + LS_OFFSET_ANGLE/2 - 180;	//store the current angle and
                                if(angles[counter] > 360)
                                	angles[counter] -= 360;
                                else if(angles[counter] < 0)
                                	 angles[counter] += 360;
                                
                                Sound.beep();
                                LCD.drawInt(counter, 0, 5);
                                counter++;										//increment the counter.
                                isOnLine = true;
                        }
                        else if(!colorGather.isOnLine(0)){
                        	isOnLine = false;
                        }
                        try { Thread.sleep(50); } catch (InterruptedException e) {}
                }
                nav.stop();
                Odometer.setPosition(calculateLS(angles), new boolean[] {true, true, false});
        }
        
        /**
         * This method is responsible for calculating the actual heading of the robot.
         *
         * @param angleA - The first angle read by the robot in ultrasonic localization
         * @param angleB - The second angle read by the robot in ultrasonic localization
         * @return realAngle - The calculated angle.
         */
        
        private double calculateUS(double angleA, double angleB) {
                double realAngle;
                double[] position = new double[3];
                Odometer.getPosition(position);
                if(angleA<angleB){
                        //as in the rising edge, the angle in which the back wall is detected become angleB now (in falling edge, it is angleA), we invert the condition for calculation
                        
                        realAngle = Math.toRadians(position[2]) + (Math.PI / 4 - ((angleA + angleB) / 2)) + Math.PI;        //Also will have a small number to make final correction.         
                }                                                                                                                                                        
                else {
                        realAngle = Math.toRadians(position[2]) + (Math.PI * 1.25 - ((angleA + angleB) / 2)) + Math.PI; //See above.
                }
                return realAngle;
        }
        
        /**
         * This method is responsible for calculating the x, y, and theta of the robot based on the light sensor
         *
         * @param angles - the array holding the angles at which a line was detected.
         * @return array - the array holding the calculated x, y, and theta.
         */
        
        private double[] calculateLS(double[] angles) {
        		double x,y;
                double[] newPosition = new double[3];
                double[] position = new double[3];
                Odometer.getPosition(position);
                
                //equations to calculate the actual x and y position of the robot
                double angleX = angles[2] - angles[0];
                if(angleX >= 360)
                	angleX -= 360;
                else if(angleX < 0)
                	angleX += 360;
                
                double angleY = angles[3] - angles[1];
                if(angleY >= 360)
                	angleY -= 360;
                else if(angleX < 0)
                	angleY += 360;
                
                x = 30 - LS_OFFSET_DIST * Math.cos(Math.toRadians(angleX) / 2);
                y = 30 - LS_OFFSET_DIST * Math.cos(Math.toRadians(angleY) / 2);
                
                //calculates actual heading of robot.
                double deltaTheta = 90 - (angleY / 2) + (angles[3] - 180) + 180;
                if(deltaTheta >= 360)
                	deltaTheta -= 360;
                else if(deltaTheta < 0)
                	deltaTheta += 360;
                
                newPosition[0] = x; 
                newPosition[1] = y;        
                newPosition[2] = deltaTheta;
                
                return newPosition;
        }
        
        /**
         * The method responsible for rotating the robot to scan the walls.
         * It uses a boolean to determine the direction.
         * @param direction - If true, rotates robot clockwise. If false, rotates counterclockwise.
         */
        private void rotate(boolean direction) {
                Motor.A.setSpeed(ROTATION_SPEED);
                Motor.B.setSpeed(ROTATION_SPEED);
                //if the direction is true, rotate to clockwise
                if (direction) {
                        Motor.A.forward();
                        Motor.B.backward();
                } else {
                        //if the direction is false, rotate to counterclockwise
                        Motor.A.backward();
                        Motor.B.forward();
                }
        }
}
