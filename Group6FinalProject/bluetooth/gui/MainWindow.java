package gui;
import java.awt.*;
import java.awt.event.*;

public class MainWindow extends Frame implements ComponentListener {
	private static final long serialVersionUID = 3783819071726964932L;
	private static final int WIDTH = 1000, HEIGHT = 600;
	
	private NXTInformationPanel nxtInformationPanel;
	private TimerPanel timerPanel;
	private BluetoothOutput btOut;
	
	public MainWindow() {
		super("ECSE 211 - Fall 2012 Final Competition");
		this.createWindow();
		this.setVisible(true);
	}
	
	private void createWindow() {
		this.setSize(WIDTH, HEIGHT);
		this.setLocationRelativeTo(null);
		
		// setup menu bar
		MenuBar mb = new MenuBar();
		mb.add(new MainWindowMenu(this));
		this.setMenuBar(mb);
		
		// layout subpanels via new gridbag
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		// add send select here
		
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 10;
		c.gridwidth = 1;
		// add main information panel
		nxtInformationPanel = new NXTInformationPanel(this);
		this.add(nxtInformationPanel, c);
		
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 10;
		c.gridwidth = 2;
		Label lbl = new Label("         ");
		this.add(lbl, c);
		
		c.gridx = 3;
		c.gridy = 0;
		c.gridheight = 3;
		c.gridwidth = 1;
		// add timer shat here
		timerPanel = new TimerPanel(this);
		this.add(timerPanel, c);
		//this.timerPanel.start();
		
		c.gridx = 3;
		c.gridy = 3;
		c.gridheight = 7;
		c.gridwidth = 1;
		// add system output
		btOut = new BluetoothOutput(this);
		this.add(btOut, c);
		
		
		// set close listener
		this.setCloseListener();
		// set window resize listener
		this.setWindowSizeListener();
	}
	
	public void exit() {
		this.setVisible(false);
		this.dispose();
		System.exit(0);
	}
	
	private void setCloseListener() {
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
	}
	
	private void setWindowSizeListener() {
		this.addComponentListener(this);
	}
	
	public void displayOutput(String out, boolean secondNewline) {
		btOut.append(out, secondNewline);
	}
	
	public void pauseTimer() {
		this.timerPanel.stop();
	}
	
	public void startTimer() {
		this.timerPanel.start();
	}
	
	public void clearTimer() {
		this.timerPanel.clear();
	}
	
	public void clearBluetoothPanel() {
		this.btOut.clear();
	}
	
	
	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		//this.resize(getHeight() / 20, getWidth() / 20);
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
