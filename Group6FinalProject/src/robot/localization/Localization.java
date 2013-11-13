package robot.localization;
import robot.sensors.ColorGather;
import robot.sensors.USGather;
import robot.navigation.*;
//import src.bluetooth.StartCorner;
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
        public enum StartCorner {BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT, TOP_LEFT};
		private final int US_OFFSET = 7;                                //Measured value (distance from centre)
        private final double LS_OFFSET_ANGLE = 29.74488;//Measured value (degrees from central axis)
        private final double LS_OFFSET_DIST = 7;                //Measured value (distance from centre of rotation)
        private final int WALL_DISTANCE = 40;                         //Arbitrary value. Not tested
        private final int ROTATION_SPEED = 100;                 //Needs to be tested
        private Navigation nav;        
        private USGather usGather;
        private ColorGather colorGather;
        private StartCorner corner;
        private int angleAdjustment;
        
        
        public Localization(UltrasonicSensor us, ColorSensor csLeft, ColorSensor csRight, ColorSensor csBlock, StartCorner corner, TwoWheeledRobot robo) {
                this.corner = corner;
                usGather = new USGather(us);
                colorGather = new ColorGather(csLeft, csRight, csBlock);
                nav = new Navigation(robo);
                
        }
        
        public void localize() {
                switch(corner) {
                case BOTTOM_LEFT:         angleAdjustment = 0;
                case BOTTOM_RIGHT:         angleAdjustment = 270;
                case TOP_RIGHT:         angleAdjustment = 180;
                case TOP_LEFT:                 angleAdjustment = 90;
                default:                         angleAdjustment = 0;
                } 
                usLocalization();
                nav.travelTo(10, 10);
                lightLocalization();
                
        }
        
        /**
         * The method responsible for handling the ultrasonic part of localization. It will
         * set the heading in the odometer.
         */        
        
        public void usLocalization() {
        		double angleA = 0; double angleB = 0;
                double[] array = new double[3];                        //Just to make getPosition happy...
                boolean direction = true;
                while (usGather.getDistance() < (WALL_DISTANCE - US_OFFSET)) {                                //If starts facing a wall it will rotate until
                        rotate(direction);                                                                                                                //it is no longer facing wall.
                }
                while(true) {        //while still localizing, may put bool variable here later.
                        rotate(direction);
                        LCD.clear();
                        LCD.drawInt((int) usGather.getDistance(), 13, 0);
                        if (usGather.getDistance() < (WALL_DISTANCE - US_OFFSET)) {                                //Once the rising edge is detected...
                                LCD.drawString("In...", 0, 0);
                        		Motor.A.stop();                                                                                                                //stop the motors and get the theta value
                                Motor.B.stop();                                                                                                                //from the odometer
                                Odometer.getPosition(array);                                                                                 //Need to be able to get theta more easily...
                                LCD.drawInt((int) array[2], 0, 5);
                                if (angleA == 0) {                                                                                                        //If this is the first time it's stopped (angleA hasn't
                                        angleA = Math.toRadians(array[2]);                                                                                                //been changed) then set angleA and reverse the direction.
                                        direction = false;                                                                                                //The sleep is to ensure that the same edge is not picked
                                        rotate(direction);
                                        LCD.drawInt((int) angleA, 0, 0);
                                        try { Thread.sleep(2000); } catch (InterruptedException e) {}        //up again.
                                } else {
                                        angleB = Math.toRadians(array[2]);                                                                                                //Sets the second angle,
                                        LCD.drawInt((int) angleB, 0, 1);
                                        array[2] = Math.toDegrees(calculateUS(angleA, angleB)) + angleAdjustment;                        //Calculates the true angle and adjusts for corner.
                                        Odometer.setPosition(array, new boolean [] {false, false, true});        //Sets the theta in the odometer, but NOT x and y.
                                        nav.turnTo(angleAdjustment, 0);
                                        return;
                                }
                        }
                }
        }
        
        /**
         * The method responsible for handling the light sensor part of localization.
         * It will set the x and y of the odometer.
         */
        
        public void lightLocalization() {
                boolean direction = true;
                int counter = 0;
                double[] angles = new double[4];
                double[] array = new double[3];
                while(true) {
                        rotate(direction);
                        LCD.drawString(colorGather.getLineSensorReading(0) + "", 0, 0);
                        if (colorGather.isOnLine(0)) {                                                //0 = left sensor on robot
                                Odometer.getPosition(array);
                                angles[counter] = array[2] - LS_OFFSET_ANGLE;        //store the current angle and
                                Sound.beep();
                                LCD.drawInt(counter, 0, 5);
                                counter++;                                                                                //increment the counter.
                                try { Thread.sleep(500.); } catch (InterruptedException e) {}
                        }
                        if (counter == 4) {                                                                        //After four lines have been scanned
                                Motor.A.stop();                                                                        //stop the motors and calculate the        
                                Motor.B.stop();                                                                        //new odometer values.
                                Odometer.setPosition(calculateLS(angles), new boolean[] {true, true, true});
                                return;
                        }
                }
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
                double x, y;
                double[] array = new double[3];
                //these equations deal with any wraparound from going from 359 to 0 degrees
                if (angles[2] < angles[0])
                        angles[2] += (Math.PI * 2);
                if (angles[3] < angles[1])
                        angles[3] += (Math.PI * 2);
                //equations to calculate the actual x and y position of the robot
                double angleX=Math.abs(angles[2]-angles[0]);
                double angleY=Math.abs(angles[3]-angles[1]);
                y = -LS_OFFSET_DIST * Math.cos(angleX / 2);
                x = -LS_OFFSET_DIST * Math.cos(angleY / 2);
                
                //calculates actual heading of robot.
                double deltaTheta = (Math.PI / 2) + (angleY / 2) - (angles[3] - Math.PI);
                array[0] = x; array[1] = y;        array[2] = deltaTheta;
                return array;
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
