package robot.mapping;

import java.awt.Rectangle;
import java.util.ArrayList;

import lejos.nxt.LCD;
import robot.bluetooth.PlayerRole;
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
	
	// ArrayList of all the detected blocks
	private static ArrayList<Block> blocks = new ArrayList<Block>();
	private static Block currentBlock = null;
	
	// Contains the bounds of the object and whether or not it has been investigated
	// private static Hashtable<Ellipse2D.Double, Boolean> objects = new Hashtable<Ellipse2D.Double, Boolean>();

	private static int[] endPoints;
	private static int[] avoidPoints;
	private Rectangle endZone = new Rectangle();
	private Rectangle avoidZone = new Rectangle();
	
	private static Object lock = new Object();
	/**
	 * Creates the map instance with the constructor of the robot mode 0-stacker 1-garbager
	 * @param mode
	 */
	public Map(PlayerRole role, int[] redZone, int[] greenZone){
		endPoints = greenZone;
		avoidPoints = redZone;
		
		endZone.setSize(endPoints[2] - endPoints[0] ,endPoints[3] - endPoints[1]);
		endZone.setLocation(endPoints[0] + 30, endPoints[1] + 30);
		
		avoidZone.setSize(avoidPoints[2] - avoidPoints[0] ,avoidPoints[3] - avoidPoints[1]);
		avoidZone.setLocation(avoidPoints[0] + 30, avoidPoints[1] + 30);
	}
	
	// Called when the next block to investigated must be found
	private static Block getNextBlock(){
		ArrayList<Block> tempBlocks = blocks;
		
		if(tempBlocks != null && tempBlocks.size() == 0)
			return null;
		
		for(int i=0; i < tempBlocks.size(); i++){
			if(tempBlocks.get(i).wasInvestigated())
				tempBlocks.remove(i);
		}
		
		double closestValue = 255;
		int closestIndex = -1;
				
		for(int i=0; i < tempBlocks.size(); i++){
			if(tempBlocks.get(i).distanceToBlock() < closestValue){
				closestValue = tempBlocks.get(i).distanceToBlock();
				closestIndex = i;
			}
		}

		return tempBlocks.get(closestIndex);
	}
	
	public static void cleanBlocks(){
		if(blocks.size() < 1)
			return;
		
		LCD.drawString(blocks.size() + "",0,7);
		for(int i=0; i<blocks.size(); i++){
			if(blocks.get(i).getConfidence() < CONFIDENCE_THRESHOLD){
				blocks.remove(i);
			}
		}
		LCD.drawString("                 ",0,7);
		LCD.drawString(blocks.size() + "",0,7);
	}
	
	// Creates an array of waypoints that contain the path to the final destination
	private static void findPathToWaypoint(double wpX, double wpY){
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
	 * Builds a waypoint list to get to the nearest un-identified block
	 */
	public static void buildNextBlockWaypoints(){
		waypointXs = new ArrayList<Double>();
		waypointYs = new ArrayList<Double>();
		
		// Find the next best block from the list of blocks
		currentBlock = getNextBlock();
		
		// If there is no next block available, return  without a new waypoint
		if(currentBlock == null){
			newWaypoint = false;
			return;
		}
		
		// Otherwise find a waypoint list to get to that point
		double[] center = currentBlock.getBlockCenter();
		waypointXs.add(center[0]);
		waypointYs.add(center[1]);
		//run below insead of the two lines above once the below lines are properly debugged
		//findPathToWaypoint(center[0], center[1]);
		
		synchronized(lock){
			newWaypoint = true;
			wpX = waypointXs.get(0);
			wpY = waypointYs.get(0);
		}
	}
	
	/**
	 * Builds a waypoint list to get to the end (green/red zone)
	 */
	public static void buildEndWaypoints(){
		waypointXs = new ArrayList<Double>();
		waypointYs = new ArrayList<Double>();
		
		double[] endPoint = new double[]{(endPoints[2] + endPoints[0]) / 2.0, (endPoints[3] + endPoints[1]) / 2.0};
		waypointXs.add(endPoint[0]);
		waypointYs.add(endPoint[1]);
		//run below insead of the two lines above once the below lines are properly debugged
		//findPathToWaypoint(endPoint[0] , endPoint[1]);
		
		synchronized(lock){
			newWaypoint = true;
			wpX = waypointXs.get(0);
			wpY = waypointYs.get(0);
		}
	}

	/**
	 * Tells the map that a waypoint has been reached
	 */
	public static void waypointReached(){
		waypointXs.remove(0);
		waypointYs.remove(0);
		
		if(waypointXs.size() == 0){ // End of waypoint list i.e block/endpoint has been reached
			newWaypoint = false;
		}
		else{
			synchronized(lock){
				newWaypoint = true;
				wpX = waypointXs.get(0);
				wpY = waypointYs.get(0);
			}
		}
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
}
