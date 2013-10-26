package robot.sensors;

import lejos.nxt.ColorSensor;
import lejos.util.TimerListener;

public class ColorGather implements TimerListener {
	
	private double currentColorFront;
	private double currentColorBack;
	private ColorSensor csFront;
	private ColorSensor csBack;
	private ColorSensor csBlockReader;
	//should i correct odometer in this class??
	
	public ColorGather(ColorSensor csFront, ColorSensor csBack, ColorSensor csBlockReader) {
		this.csFront = csFront;
		this.csBack = csBack;
		this.csBlockReader = csBlockReader;
	}
	
	public void timedOut() {
		
	}
	
	public boolean isBlockBlue() {
		return false;
	}
	
	//likely have to sleep timer in this case, can you sleep a timer? i think so...
	public boolean isOnLine() {
		return false;
	}
	
	public void updateBoardColor() {
		
	}
}