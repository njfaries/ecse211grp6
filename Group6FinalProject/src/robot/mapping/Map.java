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
	public static final int CONFIDENCE_THRESHOLD = 10;
	
	private static double wpX = 30, wpY = 30;
	private static ArrayList<Double> waypointXs = new ArrayList<Double>();
	private static ArrayList<Double> waypointYs = new ArrayList<Double>();
	private static boolean newWaypoint = false;
	private static boolean goingHome = false;
	private static boolean isHome = false;
	
	// ArrayList of all the detected blocks
	private static ArrayList<Block> blocks = new ArrayList<Block>();
	private static Block currentBlock = null;
	
	// Contains the bounds of the object and whether or not it has been investigated
	// private static Hashtable<Ellipse2D.Double, Boolean> objects = new Hashtable<Ellipse2D.Double, Boolean>();

	private Rectangle endZone = new Rectangle();
	
	private static Object lock = new Object();
	
	/**
	 * Creates the map instance with the constructor of the robot mode 0-stacker 1-garbager
	 * @param mode
	 */
	public Map(int endzoneX1, int endzoneY1, int endzoneX2, int endzoneY2){
		endZone.setSize(endzoneX2 - endzoneX1 ,endzoneY2 - endzoneY1);
		endZone.setLocation(endzoneX1, endzoneY1);
	}
	
	// Called when the next block to investigated must be found
	private static Block getNextBlock(ArrayList<Block> tempBlocks){
		double closestValue = 255;
		int closestIndex = -1;
		
		if(tempBlocks.size() == 0)
			return null;
		
		for(int i=0; i < tempBlocks.size(); i++){
			if(tempBlocks.get(i).distanceToBlock() < closestValue){
				closestValue = tempBlocks.get(i).distanceToBlock();
				closestIndex = i;
			}
		}
		
		if(tempBlocks.get(closestIndex).wasInvestigated()){
			tempBlocks.remove(closestIndex);
			return getNextBlock(tempBlocks);
		}
		return blocks.get(closestIndex);
	}
	
	public static void cleanBlocks(){
		for(int i=0; i<blocks.size(); i++){
			if(blocks.get(i).getConfidence() < CONFIDENCE_THRESHOLD){
				blocks.remove(i);
			}
		}
	}
	// Setters
	/**
	 * Sets the current block as having been checked. Also sets the block as being styrofoam or not
	 * @param sytrofoam - whether or not the block is styrofoam
	 */
	public static void blockChecked(boolean sytrofoam){
		currentBlock.investigate();
		if(sytrofoam)
			currentBlock.setStyrofoam();
	}
	
	/**
	 * Checks if the waypoint needs to be updated
	 */
	public static void updateWaypoint(){
		if(isHome)
			return;
		
		double[] newWp = new double[2];
		
		if(!goingHome){
			currentBlock = getNextBlock(blocks);
			
			if(currentBlock.equals(null))
				return;
			
			newWp = currentBlock.getWaypoint();
			
			newWaypoint = true;
		}
		else if(wpX != waypointXs.get(0) && wpY != waypointYs.get(0)){
			newWp[0] = waypointXs.get(0);
			newWp[1] = waypointYs.get(0);
			
			if(waypointXs.size() == 1)
				isHome = true;
			waypointXs.remove(0);
			waypointYs.remove(0);
			newWaypoint = true;
		}
		else{
			newWaypoint = false;
			return;
		}

		
		synchronized(lock){
			wpX = newWp[0];
			wpY = newWp[1];
		}
	}
	
	public static void findPathToWaypoint(double wpX, double wpY){
		waypointXs.add(wpX);
		waypointYs.add(wpY);
		
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
		}
		if(hasNewWp)
			findPathToWaypoint(currX, currY);
	}

	/**
	 * Adds a new set of coordinates following a scan that updates 
	 * @param newCoords - New set of coordinates to add to the map
	 */
	public static void addBlock(double[] xValues, double[] yValues){
		Block newBlock = new Block(xValues, yValues);
		double[] center = newBlock.getBlockCenter();
		
		for(int i=0; i < blocks.size(); i++){
			if(blocks.get(i).containsPoint(center[0], center[1])){
				blocks.get(i).mergeBlock(newBlock);
				return;
			}
		}
		blocks.add(new Block(xValues, yValues));
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
	/**
	 * Returns the arrayList of all blocks
	 * @return ArrayList<Block> blocks
	 */
	public static ArrayList<Block> getBlocks(){
		return blocks;
	}
	public static Block getCurrentBlock(){
		return currentBlock;
	}
	public static int getBlockCount(){
		return blocks.size();
	}
}
