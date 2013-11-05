package robot.navigation;

/**
 * Contains information about obstacles on the observed playing field. Contains static getters and setters as only one instance
 * of this class will exist.
 * 
 * @author Michael
 * @author Andreas
 * @version 1.1.0
 * @since 2013-11-04
 */
public class Map {
	//arrays need to be filled upon construction to avoid null pointers
	private static double wpX = 0, wpY = 0;
	private static boolean newWaypoint = false;
	private int woodBlocksFound;
	
	
	private int[] wallCorners;
	private double[] wallBoundaryEquations;
	private double[] objectBoundaryEquations;
	//x, y, haveSearched 0 or 1 
	private int[][][] locations;
	private double[] finalDestinationCorners;
	private double[] finalDestinationBoundaryEquations;
	
	private static Object lock = new Object();
	
	public Map(){
		
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
