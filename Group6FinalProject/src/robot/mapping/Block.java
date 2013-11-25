package robot.mapping;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import lejos.nxt.LCD;
import robot.navigation.Odometer;

public class Block {
	public static final double blockRadius = 20, waypointDistance = 35;
	private ArrayList<Double> xPoints = new ArrayList<Double>();
	private ArrayList<Double> yPoints = new ArrayList<Double>();
	
	private double centerX, centerY;
	
	private boolean investigated = false;
	private boolean isStyrofoam = false;
	
	private Rectangle2D bounds;
	double[] pos;
	
	/**
	 * Creates a block from a set of coordinates generated from a scan
	 * @param objectCoordinates - coordinates class containing information about the object
	 */
	public Block(double[] xValues, double[] yValues){
		pos = new double[3];
		
		for(int i=0; i<xValues.length; i++){
			xPoints.add(xValues[i]);
			yPoints.add(yValues[i]);
		}
		
		findCenter();
		
		bounds = new Rectangle2D.Double(centerX - 15, centerY - 15, 30, 30);
	}
	public Block(double centerX, double centerY){
		this.centerX = centerX;
		this.centerY = centerY;
		
		bounds = new Rectangle2D.Double(centerX - 15, centerY - 15, 30, 30);
	}
	// Averages the points to find the block's center
	private void findCenter(){
		double xSum = 0, ySum = 0;
		
		for(double d: xPoints){xSum += d;}
		for(double d: yPoints){ySum += d;}
		
		centerX = xSum / xPoints.size();
		centerY = ySum / yPoints.size();
	}
	
	// Setters
	/**
	 * Sets this block as having been investigated
	 */
	public void investigate(){
		this.investigated = true;
	}
	/**
	 * Sets this block as being sytrofoam
	 */
	public void setStyrofoam(){
		this.isStyrofoam = true;
	}
	
	// Getters
	/**
	 * Returns whether or not this block has been investigated
	 * @return boolean - investigated
	 */
	public boolean wasInvestigated(){
		return investigated;
	}
	/**
	 * Returns whether or not this block is styrofoam
	 * @return boolean - isStyrofoam
	 */
	public boolean isStyrofoam(){
		return isStyrofoam;
	}
	/**
	 * Returns the center point in the form of a length 2 array
	 * @return double[] - {centerX, centerY}
	 */
	public double[] getBlockCenter(){
		return new double[]{centerX, centerY};
	}	
	/**
	 * Returns a new waypoint in the form of double[]{x,y}
	 * @return double[] - new waypoint
	 */
	public double[] getWaypoint(){
		Odometer.getPosition(pos);
		
		double dX = centerX - pos[0];
		double dY = centerY - pos[1];
		double t = Math.atan2(dY, dX);
		
		double distanceToPoint = Math.sqrt(dX * dX + dY * dY) - waypointDistance;
		
		return new double[]{pos[0] + distanceToPoint * Math.sin(t), pos[1] + distanceToPoint * Math.cos(t)};
	}
	/**
	 * Determines if the path goes through this block
	 * @param x - destination x
	 * @param y - destination y
	 * @return boolean
	 */
	public boolean lineIntersects(double x1, double y1, double x2, double y2){
		Odometer.getPosition(pos);
		Line2D.Double path = new Line2D.Double(x1, y1, x2, y2);
		return path.intersects(bounds.getBounds());
	}
	/**
	 * Determines if a point is inside the block (i.e. a point relates to this block)
	 * @param x - x value to test
	 * @param y - y value to test
	 * @return boolean
	 */
	public boolean containsPoint(double x, double y){
		double dist = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y-centerY, 2));
		if(dist <= blockRadius)
			return true;
		return false;
	}
	/**
	 * Gets new a new waypoint at the edge of the block that allows for the shortest path to the final destination
	 * @param startX - the x value to change
	 * @param startY - the y value to change
	 * @param wpX - the x value of the end waypoint
	 * @param wpY - the y value of the end waypoint
	 * @return double[]{x,y}
	 */
	public double[] getNewWaypoint(double startX, double startY, double wpX, double wpY){
		// Finds the angle of the current path of motion
		double lineAngle = Math.atan2((wpY - startY), (wpX - startX));
		
		// shift the angle 90 degrees (perpendicular to the current path)
		lineAngle += 90; 
		if(lineAngle < 0)
			lineAngle += 360;
		else if(lineAngle >= 360)
			lineAngle -= 360;
		
		double[] newWayPoint = null;
		
		// find new waypoints at either exterior of the block
		double newWpX1 = centerX + waypointDistance * Math.cos(lineAngle);
		double newWpY1 = centerY + waypointDistance * Math.sin(lineAngle);
		double newWpX2 = centerX - waypointDistance * Math.cos(lineAngle);
		double newWpY2 = centerY - waypointDistance * Math.sin(lineAngle);
		
		// Determines the shortest path 
		double distance1 = Math.sqrt(Math.pow(startX - newWpX1,2) + Math.pow(startY - newWpY1,2)) + Math.sqrt(Math.pow(wpX - newWpX1,2) + Math.pow(wpY - newWpY1,2));
		double distance2 = Math.sqrt(Math.pow(startX - newWpX2,2) + Math.pow(startY - newWpY2,2)) + Math.sqrt(Math.pow(wpX - newWpX2,2) + Math.pow(wpY - newWpY2,2));
		if(distance1 < distance2)
			newWayPoint = new double[]{newWpX1, newWpY1};
		else
			newWayPoint = new double[]{newWpX2, newWpY2};
		
		// Returns new waypoint
		return newWayPoint;
	}
	public ArrayList<Double> getXValues(){
		return xPoints;
	}
	public ArrayList<Double> getYValues(){
		return yPoints;
	}
	public void mergeBlock(Block newBlock){
		xPoints.addAll(newBlock.getXValues());
		yPoints.addAll(newBlock.getYValues());
		
		findCenter();
	}
	public int getConfidence(){
		return xPoints.size();
	}
	public double distanceToBlock(){
		Odometer.getPosition(pos);
		return Math.sqrt(Math.pow(centerX - pos[0],2) + Math.pow(centerY - pos[1],2));
	}
}
