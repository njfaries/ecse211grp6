package robot.mapping;

import java.awt.Rectangle;
import java.util.ArrayList;

import robot.base.RobotController.RobotMode;
import robot.navigation.Odometer;

/**
 * Contains information about obstacles on the observed playing field. Contains static getters and setters as only one instance
 * of this class will exist.
 * 
 * @author Michael
 * @author Andreas
 * @version 2.0.0
 * @since 2013-11-09
 */
public class Map {
	//arrays need to be filled upon construction to avoid null pointers
	public static final double xMax = 360, yMax = 360;
	
	private static double wpX = 0, wpY = 0;
	private static double[][] waypointList = null;
	private static boolean newWaypoint = false;
	private static boolean goingHome = false;
	
	// ArrayList of all the detected blocks
	private static ArrayList<Block> blocks = new ArrayList<Block>();
	private static int currentBlockIndex = 0;
	
	// Contains the bounds of the object and whether or not it has been investigated
	// private static Hashtable<Ellipse2D.Double, Boolean> objects = new Hashtable<Ellipse2D.Double, Boolean>();

	private Rectangle endZone = new Rectangle();
	
	private static Object lock = new Object();
	
	/**
	 * Creates the map instance with the constructor of the robot mode 0-stacker 1-garbager
	 * @param mode
	 */
	public Map(RobotMode mode){
		if(mode.equals(RobotMode.STACKER)){
			endZone.setSize(60,30);
			endZone.setLocation(210, 150);
		}
		else{
			endZone.setSize(60,120);
			endZone.setLocation(30, 300);
		}
	}
	
	public boolean checkReadingWall(double readX, double readY) {
		return false;
	}
	
	public boolean checkNavWall(double posX, double posY) {
		return false;
	}
	
	public static void addNewObjectBoundary(double x1, double y1, double x2, double y2, double x3, double y3) {
		
	}
	
	public boolean checkNavObject(double posX, double posY) {
		return false;
	}
	
	public boolean checkReadingObject(double readX, double readY) {
		return false;
	}
	
	// Setters
	/**
	 * Sets the current block as having been checked. Also sets the block as being styrofoam or not
	 * @param sytrofoam - whether or not the block is styrofoam
	 */
	public static void blockChecked(boolean sytrofoam){
		blocks.get(currentBlockIndex).investigate();
		if(sytrofoam)
			blocks.get(currentBlockIndex).setStyrofoam();
	}
	
	/**
	 * Checks if the waypoint needs to be updated (new block to path to)
	 */
	public static void updateBlockWaypoint(){
		double[] newWp = blocks.get(currentBlockIndex).getWaypoint();
		
		synchronized(lock){
			wpX = newWp[0];
			wpY = newWp[1];
		}
	}
	public static void updatePointWaypoint(double wpX, double wpY){
		ArrayList<Double> xVals = new ArrayList<Double>();
		ArrayList<Double> yVals = new ArrayList<Double>();
		
		xVals.add(wpX);
		yVals.add(wpY);
		
		double[] pos = new double[3];
		Odometer.getPosition(pos);
		double currX = pos[0];
		double currY = pos[1];
		boolean hasNewWp = false;
		
		for(int i=0; i < blocks.size(); i++){
			if(blocks.get(i).lineIntersects(currX, currY, wpX, wpY)){
				double[] newWp = blocks.get(i).getNewWaypoint(currX, currY, wpX, wpY);
				
				currX = newWp[0];
				currY = newWp[1];
				
				hasNewWp = true;
				i = 0;
			}
			else if(i == blocks.size() - 1 && hasNewWp){
				xVals.add(currX);
				yVals.add(currY);
				
				wpX = currX;
				wpY = currY;
				currX = pos[0];
				currY = pos[1];
				
				i = 0;
			}
		}
	}

	/**
	 * Adds a new set of coordinates following a scan that updates 
	 * @param newCoords - New set of coordinates to add to the map
	 */
	public static void addBlock(Coordinates objectCoords){
		Block newBlock = new Block(objectCoords);
		for(int i=0; i < blocks.size(); i++){
			if(blocks.get(i).pointInside(newBlock.getCenterX(), newBlock.getCenterY()))
				blocks.get(i).merge(newBlock);
		}
		blocks.add(new Block(objectCoords));
	}

	/**
	 * Tells the map to find a path to get to the end zone rather than to a block
	 */
	public static void getToEndZone(){
		goingHome = true;
	}

	// Getters
	/**
	 * Checks if a new waypoint is available for the navigator to navigate to.
	 * @return boolean: newWaypoint
	 */
	public static boolean hasNewWaypoint(){
		synchronized(lock){
			return newWaypoint;
		}
	}
	
	/**
	 * Updates the waypoint array with the coordinates of the current waypoint to be traveled to.
	 * If the waypoint are new, set newWaypoints to false.
	 * @param wp - size 2 array for the x and y values of the waypoint
	 */
	public static void getWaypoint(double[] wp){
		synchronized(lock){
			wp[0] = wpX;
			wp[1] = wpY;
			newWaypoint = false;
		}
	}
	public static ArrayList<Block> getBlocks(){
		return blocks;
	}
	public static Block getCurrentBlock(){
		return blocks.get(currentBlockIndex);
	}
}
