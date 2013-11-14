/**
* @author Sean Lawlor, Stepan Salenikovich, Francois OD
* @date November 6, 2013
* @class ECSE 211 - Design Principle and Methods
*/
package robot.bluetooth;

/**
 * Skeleton class to hold datatypes needed for final project
 * 
 * Simply all public variables so can be accessed with 
 * Transmission t = new Transmission();
 * int fx = t.fx;
 * 
 * and so on...
 * 
 * Also the role is an enum, converted from the char transmitted. (It should never be
 * Role.NULL)
 */

public class Transmission {
	/**
	 * The role, Builder or Garbage Collector
	 */
	public PlayerRole role;
	/**
	 * starting corner, 1 through 4
	 */
	public StartCorner startingCorner;
	/**
	 * Green Zone coordinates (bottom-left XY, top-right XY)
	 */	
	public int[] greenZone = new int[4];
	/**
	 * Red Zone coordinates (bottom-left XY, top-right XY)
	 */	
	public int[] redZone = new int[4];
}
