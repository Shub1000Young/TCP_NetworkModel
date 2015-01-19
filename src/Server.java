import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;



public class Server {
	private int bufferCapacity;
	private long handlingDelay;
	protected static LinkedBlockingQueue<Packet> buffer;
	public static ReentrantLock lock; 
	public Server(int capacity, long delay){
		bufferCapacity = capacity;
		handlingDelay = delay;
		buffer = new LinkedBlockingQueue<Packet>(bufferCapacity);
		lock = new ReentrantLock();
	}


	
	public static boolean bufferFull(){
		if(buffer.remainingCapacity()==0){
			return true;
		}else{
			return false;
		}
		
	}
	
	public static void bufferAdd(Packet packet){
		lock.lock();
		try {
			buffer.put(packet);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	}
}
