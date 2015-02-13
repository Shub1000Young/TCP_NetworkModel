import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements Runnable{
	
	
	private static int bufferCapacity;
	protected static DelayQueue<Packet> outputQueue;
	public static ReentrantLock lock; 
	public static LinkedBlockingQueue<Packet> buffer;
	volatile static boolean running = true;
	
	public Server(int capacity, long delay){
		bufferCapacity = capacity;
		buffer = new LinkedBlockingQueue<Packet>(bufferCapacity);
		outputQueue = new DelayQueue<Packet>();
		lock = new ReentrantLock();
		AckQueuer ackQueuer = new AckQueuer(delay);
		new Thread(ackQueuer).start();
		System.out.println("server created");
	}

	public static void bufferAdd(Packet packet){
		System.out.println("S "+ packet.getPacketNumber());
		System.out.println("packet added to Server buffer");	
		buffer.offer(packet); 

		System.out.println("server buffer length="+ buffer.size());
	}


	private static void sendAck(){
		Packet ack= null;
		try {
			ack = outputQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int destination = ack.getOriginatingClientNumber();
		ack.setTimeSentOut(System.nanoTime());
		try {
			Client.clientArray.get(destination).sync.put(ack);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("packet sent to destination " + destination);

		
	}

	
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
