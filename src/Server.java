import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;



public class Server {
	private int bufferCapacity;
	private long handlingDelay;
	private static LinkedBlockingQueue<Packet> buffer;
	public static ReentrantLock lock; 
	public Server(int capacity, long delay){
		bufferCapacity = capacity;
		handlingDelay = delay;
		buffer = new LinkedBlockingQueue<Packet>(bufferCapacity);
		lock = new ReentrantLock();
	}
	
	public static void bufferAdd(Packet packet){
			buffer.offer(packet);
	}
	
	
}
