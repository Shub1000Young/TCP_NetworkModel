import java.util.concurrent.locks.ReentrantLock;



public class Server {
	public static ReentrantLock lock = new ReentrantLock();
	
	public static boolean queueFull(){
		// return true for now until implemented
		return true;
	}
	
	public static void queueAdd(Packet packet){
		
	}
}
