package robot.mapping;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.awt.geom.*;

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
public class Map2 {	
	
	// Waypoints now used for getting home again
	private static double wpX = 30, wpY = 30;
	private static ArrayList<Point2D> waypoints = new ArrayList<Point2D>();
	
	// Points to avoid
	
	private static ArrayList<Point2D> points = new ArrayList<Point2D>();
	
	// Contains the bounds of the object and whether or not it has been investigated
	// private static Hashtable<Ellipse2D.Double, Boolean> objects = new Hashtable<Ellipse2D.Double, Boolean>();

	private static int[] endPoints;
	private static int[] avoidPoints;
	private Rectangle avoidZone = new Rectangle();
	
	private static Object lock = new Object();
	/**
	 * Creates the map instance with the constructor of the robot mode 0-stacker 1-garbager
	 * @param mode
	 */
	public Map2(PlayerRole role, int[] redZone, int[] greenZone){
		endPoints = greenZone;
		avoidPoints = redZone;
		
		avoidZone.setSize(avoidPoints[2] - avoidPoints[0] ,avoidPoints[3] - avoidPoints[1]);
		avoidZone.setLocation(avoidPoints[0], avoidPoints[1]);
	}
	
	// Setters
	
	/**
	 * Adds a point to avoid
	 * @param newCoords - New set of coordinates to add to the map
	 */
	public static void addPoint(double x, double y){
		Point2D point = new Point2D.Double(x, y);
		points.add(point);
	}
	
	// Getters
	public static boolean checkPoint(double x, double y){
		for(int i=0; i<points.size(); i++){
			double distance = Point2D.distance(points.get(i).getX(), points.get(i).getY(), x, y);
		}
		return false;
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
		}
	}

	public static double[] getEndCenter(){
		return new double[]{(endPoints[2] + endPoints[0])/  2, (endPoints[3] + endPoints[1]) / 2};
	}
}
