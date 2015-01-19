import java.util.concurrent.locks.ReentrantLock;



public class Server {
	//just a few skeletons in here for now to stop things complaining
	public static ReentrantLock lock = new ReentrantLock();
	
	public static boolean queueFull(){
		// return true for now until implemented
		return true;
	}
	
	public static void queueAdd(Packet packet){
		
	}
}
