package robot.test;

import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import robot.navigation.OdometryCorrection;
import robot.sensors.ColorGather;

public class BlockSensorTest extends Thread {
	private static ColorSensor csLeft = new ColorSensor(SensorPort.S1);
	private static ColorSensor csRight = new ColorSensor(SensorPort.S2);
	private static ColorSensor csBlockReader = new ColorSensor(SensorPort.S3);
	
	ColorGather cg;
	
	public static void main(String[] args){
		new BlockSensorTest();
	}
	public BlockSensorTest(){
		cg = new ColorGather(csLeft, csRight, csBlockReader, new OdometryCorrection());
		this.start();
	}
	public void run(){
		while(true){
			LCD.drawString("                      ",0,0);
			LCD.drawString(cg.isBlue() + "", 0,0);
		}
	}
}
