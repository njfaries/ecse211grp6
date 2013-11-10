package robot.base;

public class Status {
	private static String status = "";
	private static Object lock = new Object();
	
	public Status(){
		
	}
	public static void setStatus(String s){
		synchronized(lock){
			status = s;
		}
	}
	public static String getStatus(){
		synchronized(lock){
			return status;
		}
	}
}

