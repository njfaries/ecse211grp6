import lejos.util.TimerListener;
import lejos.nxt.UltrasonicSensor;

public class USGather implements TimerListener {
	
	private double distance;
	private double readX;
	private double readY;
	
	private UltrasonicSensor usFront;
	private UltrasonicSensor usBack;
	private Odometer odo;
	private SensorMotor frontSenMotor;
	private SensorMotor backSenMotor;
	
	public USGather(Odometer odo, UltrasonicSensor usFront, UltrasonicSensor usBack, SensorMotor frontSenMotor, SensorMotor backSenMotor) {
		this.usFront = usFront;
		this.usBack = usBack;
		this.odo = odo;
		this.frontSenMotor = frontSenMotor;
		this.backSenMotor = backSenMotor;
	}
	
	public void timedOut() {
		
	}
	
	public int getMedianData() {
		return 4;
	}
	
	public void updateReadValues(double d) {

	}
	
	public void focusOnPoint(double latchedX, double latchedY) {
		
	}
		
}
