package gui.defaults;

import java.awt.Menu;
import gui.*;

public class DPMMenuItem extends Menu {
	private static final long serialVersionUID = -3857800464786488095L;
	protected MainWindow mw;
	
	public DPMMenuItem(MainWindow mw, String menuName) {
		super(menuName);
		this.mw = mw;
	}
}
