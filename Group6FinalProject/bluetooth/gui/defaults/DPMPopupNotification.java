package gui.defaults;

import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class DPMPopupNotification extends Frame implements ActionListener, WindowListener {
	
	private Button ok;
	private Component c;
	
	public DPMPopupNotification(String notification, Component c) {
		this.setLayout(new BorderLayout());
		this.c = c;
		this.c.setEnabled(false);

		this.setLocationRelativeTo(null);
		this.setSize((int)(notification.length() * 8), 100);
		Label lbl = new Label(notification, Label.CENTER);
		this.add(lbl, BorderLayout.NORTH);		
		this.ok = new Button("OK");
		this.ok.addActionListener(this);
		this.add(this.ok, BorderLayout.SOUTH);
		
		this.setResizable(false);
		//this.setUndecorated(true);
		this.addWindowListener(this);
		this.setVisible(true);
		
	}
	
	public DPMPopupNotification(String notification, Component c, Font f) {
		this.setFont(f);
		this.setLayout(new BorderLayout());
		this.c = c;
		this.c.setEnabled(false);
		this.setLocationRelativeTo(null);
		this.setSize((int)(notification.length() * f.getSize()), 100 * f.getSize() / 20);
		Label lbl = new Label(notification, Label.CENTER);
		this.add(lbl, BorderLayout.NORTH);		
		this.ok = new Button("OK");
		this.ok.addActionListener(this);
		this.add(this.ok, BorderLayout.SOUTH);
		
		this.setResizable(false);
		//this.setUndecorated(true);
		this.addWindowListener(this);
		this.setVisible(true);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Button bt = (Button)e.getSource();
		if (bt.equals(this.ok))
			this.exitPopup();
	}
	
	public void exitPopup() {
		this.setVisible(false);
		this.setEnabled(false);
		this.c.setEnabled(true);
	}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {
		this.exitPopup();
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
