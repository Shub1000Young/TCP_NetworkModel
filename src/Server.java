import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements Runnable{
	private static int bufferCapacity;
	private static long handlingDelay;
	private static LinkedBlockingQueue<Packet> buffer;
	public static ReentrantLock lock; 
	private static long now;
	private static long last;
	volatile static boolean running = true;
	
	public Server(int capacity, long delay){
		bufferCapacity = capacity;
		handlingDelay = delay;
		buffer = new LinkedBlockingQueue<Packet>(bufferCapacity);
		lock = new ReentrantLock();
		last = System.nanoTime();
		System.out.println("server created");
	}
	
	public static void bufferAdd(Packet packet){
		System.out.println("packet added to buffer");	
		buffer.offer(packet);
	}
	

	private static void sendAck(){
		Packet ack = buffer.poll();
		int destinationPipe = ack.getOriginatingClientNumber();
		ack.setTimeSentIn(System.nanoTime());
		Client.clientArray.get(destinationPipe).inPipe.addAck(ack);
		System.out.println("packet sent to desination");
	}

	private static void unloadBuffer(){

		while(!buffer.isEmpty()){
			now = System.nanoTime();
			if(last+handlingDelay<=now){
				sendAck();
				last = System.nanoTime();
			}
		}
		@SuppressWarnings("unused")
		int waitCount = 0;
		while(buffer.isEmpty()){
			waitCount++;
		}
		unloadBuffer();
	}
	
	public static void interrupt(){
		running = false;
	}
	
	@Override
	public void run() {
		while(running){
			unloadBuffer();
		}
	}
	
	
}
