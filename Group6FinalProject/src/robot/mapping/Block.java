package robot.mapping;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import robot.navigation.Odometer;

public class Block {
	private static final double blockRadius = 15;
	private double centerX, centerY;
	
	private boolean investigated;
	private boolean isStyrofoam;
	
	private Ellipse2D.Double bounds;
	double[] pos;
	//private Coordinates objectCoordinates;
	
	/**
	 * Creates a block from a set of coordinates generated from a scan
	 * @param objectCoordinates - coordinates class containing information about the object
	 */
	public Block(Coordinates objectCoordinates){
		//this.objectCoordinates = objectCoordinates;
		double[] center = objectCoordinates.getObjectCenter();
		pos = new double[3];
		
		centerX = center[0];
		centerY = center[1];
		
		bounds = new Ellipse2D.Double(center[0] + 15, center[1] + 15, 30, 30);
		
		investigated = false;
		isStyrofoam = false;
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
	 * Returns the bounds of the block
	 * @return Ellipse2D.Double - bounds
	 */
	public Ellipse2D.Double getBounds(){
		return bounds;
	}
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
		double t = Math.atan2(dX, dY);
		
		double newD = Math.sqrt(dX * dX + dY * dY) - 15;
		
		return new double[]{pos[0] + newD * Math.sin(t), pos[1] + newD * Math.cos(t)};
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
	 * Gets new a new waypoint at the edge of 
	 * @param startX - the x value to change
	 * @param startY - the y value to change
	 * @param wpX - the x value of the end waypoint
	 * @param wpY - the y value of the end waypoint
	 * @return double[]{x,y}
	 */
	public double[] getNewWaypoint(double startX, double startY, double wpX, double wpY){
		double newLineSlope =  -(startX - wpX) / (startY - wpY);
		double lineAngle = Math.atan2(newLineSlope,1);
		
		return getExteriorPoint(lineAngle, blockRadius, wpX, wpY);
	}
	/**
	 * Gets a point outside of the blocks bounds that will allow for a path to the waypoint that does not 
	 * intersect with the block.
	 * @param lineAngle - angle of line to check on
	 * @param blockRadius - radial bounds
	 * @param wpX - end waypoint
	 * @param wpY - end waypoint
	 * @return double[]{x,y}
	 */
	private double[] getExteriorPoint(double lineAngle, double blockRadius, double wpX, double wpY){
		double[] newWayPoint = null;
		
		double newWpX1 = centerX + blockRadius * Math.sin(lineAngle);
		double newWpY1 = centerY + blockRadius * Math.cos(lineAngle);
		double newWpX2 = centerX - blockRadius * Math.sin(lineAngle);
		double newWpY2 = centerY - blockRadius * Math.cos(lineAngle);
		
		Line2D.Double path1 = new Line2D.Double(newWpX1, newWpY1, wpX, wpY);
		Line2D.Double path2 = new Line2D.Double(newWpX2, newWpY2, wpX, wpY);
		
		if(path1.intersects(bounds.getBounds()) && path2.intersects(bounds.getBounds()))
			newWayPoint = getExteriorPoint(lineAngle, blockRadius + 1, wpX, wpY);
		else if(!path1.intersects(bounds.getBounds()))
			newWayPoint = new double[]{newWpX1, newWpY1};
		else if(!path2.intersects(bounds.getBounds()))
			newWayPoint = new double[]{newWpX2, newWpY2};
		
		return newWayPoint;
	}
	
}
