package robot.mapping;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import lejos.nxt.LCD;
import robot.navigation.Odometer;

public class Block {
	private static final double blockRadius = 12, waypointDistance = 20;
	private ArrayList<Double> xPoints = new ArrayList<Double>();
	private ArrayList<Double> yPoints = new ArrayList<Double>();
	
	private double centerX, centerY;
	
	private boolean investigated;
	private boolean isStyrofoam;
	
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
		
		LCD.setPixel((int)centerX, (int)centerY, 1);
		
		bounds = new Rectangle2D.Double(centerX + 15, centerY + 15, 30, 30);
		
		investigated = false;
		isStyrofoam = false;
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
