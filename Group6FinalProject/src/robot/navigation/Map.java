package robot.navigation;

/**
 * Contains information about obstacles on the observed playing field
 * 
 * @author Andreas
 * @verions 1.0
 */
public class Map {
	//arrays need to be filled upon construction to avoid null pointers
	private int[] wallCorners;
	private double[] wallBoundaryEquations;
	private int woodBlocksFound;
	private double[] objectBoundaryEquations;
	//x, y, haveSearched 0 or 1 
	private int[][][] locations;
	private double[] finalDestinationCorners;
	private double[] finalDestinationBoundaryEquations;
	
	public Map(){
		
	}
	public boolean checkReadingWall(double readX, double readY) {
		return false;
	}
	
	public boolean checkNavWall(double posX, double posY) {
		return false;
	}
	
	public void addNewObjectBoundary(double x1, double y1, double x2, double y2, double x3, double y3) {
		
	}
	
	public boolean checkNavObject(double posX, double posY) {
		return false;
	}
	
	public boolean checkReadingObject(double readX, double readY) {
		return false;
	}
	
}
