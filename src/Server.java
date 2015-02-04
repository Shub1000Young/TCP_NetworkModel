import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements Runnable{
	private static int bufferCapacity;
	private static long handlingDelay;
	private static DelayQueue<Packet> outputQueue;
	public static ReentrantLock lock; 
	private static long now;
	private static long last;
	public static LinkedBlockingQueue<Packet> buffer;
	volatile static boolean running = true;
	
	public Server(int capacity, long delay){
		bufferCapacity = capacity;
		handlingDelay = delay;
		buffer = new LinkedBlockingQueue<Packet>(bufferCapacity);
		outputQueue = new DelayQueue<Packet>();
		lock = new ReentrantLock();
		last = System.nanoTime();
		System.out.println("server created");
	}
	//data invariant:
	public static void bufferAdd(Packet packet){
		Boolean bufferAdded;
		System.out.println("packet added to outputQueue");	
		bufferAdded = buffer.offer(packet); //bug right here??
		System.out.println("USoutputQueue "+ bufferAdded);
		System.out.println("server outputQueue length="+ buffer.size());
	}


	private static void sendAck(){
		Packet ack= null;
		try {
			ack = outputQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*		while(ack == null){
			//do nothing, same as client try removing later
		}*/
		int destination = ack.getOriginatingClientNumber();
		ack.setTimeSentOut(System.nanoTime());
		try {
			Client.clientArray.get(destination).sync.put(ack);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("packet sent to destination " + destination);
		sendAck();
		
	}

/*	private static void unloadoutputQueue(){

		while(!outputQueue.isEmpty()){
			now = System.nanoTime();
			if(last+handlingDelay<=now){
				sendAck();
				last = System.nanoTime();
			}
		}
		@SuppressWarnings("unused")
		int waitCount = 0;
		while(outputQueue.isEmpty()){
			waitCount++;
		}
		unloadoutputQueue();
	}*/
	
	public static void interrupt(){
		running = false;
	}
	
	@Override
	public void run() {
		while(running){
			sendAck();
		}
	}
	
	
}
