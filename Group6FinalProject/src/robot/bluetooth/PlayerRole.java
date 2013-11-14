/**
* @author Sean Lawlor, Francois OD
* @date November 6, 2013
* @class ECSE 211 - Design Principle and Methods
*/
package robot.bluetooth;

/**
 * Builder: value 1, string "B"
 * Garbage Collector: value 2, string "G"
 *
 */
public enum PlayerRole {
	BUILDER(1, "B"),
	GARBAGECOLLECTOR(2, "G"),
	NULL(0, "");
	
	private int role;
	private String str;
	
	private PlayerRole(int rl, String str) {
		this.role = rl;
		this.str = str;
	}
	
	public String toString() {
		return this.str;
	}
	
	/**
	 * @return the value of the role
	 */
	public int getId() {
		return this.role;
	}
	
	public static PlayerRole lookupRole(int rl) {
		for (PlayerRole role : PlayerRole.values())
			if (role.getId() == rl)
				return role;
		return PlayerRole.NULL;
	}
}
