import lejos.nxt.NXTRegulatedMotor;
import lejos.util.TimerListener;

public class SensorMotor implements TimerListener {
	
	private NXTRegulatedMotor senMotor;
	//will need getters and setters
	private double senTheta;
	
	public SensorMotor(NXTRegulatedMotor senMotor) {
			this.senMotor = senMotor;
	}
	
	public void timedOut() {
		
	}

	public void pan() {
		
	}
	
	public void pointForward() {
		
	}
	
	public int getSenMotorTachoCount() {
		return 4;
	}
	
}
