package gui;

import java.awt.*;
import java.awt.event.*;
import gui.defaults.*;

public class MainWindowMenu extends DPMMenuItem implements ActionListener {
	private static final long serialVersionUID = 1L;
	private final static String EXIT = "Exit";
	
	public MainWindowMenu(MainWindow mw) {
		super(mw, "File");
		MenuItem mi;
		add(mi = new MenuItem(EXIT));
		mi.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		String item = e.getActionCommand();
		if (item.equals(EXIT)) { 
			this.mw.exit();
		} else {
			System.out.println("Bad Action on File Menu");
		}
	}
}
