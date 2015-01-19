import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements Runnable{
	private int bufferCapacity;
	private long handlingDelay;
	private static LinkedBlockingQueue<Packet> buffer;
	public static ReentrantLock lock; 
	private static long now;
	private static long last;
	public Server(int capacity, long delay){
		bufferCapacity = capacity;
		handlingDelay = delay;
		buffer = new LinkedBlockingQueue<Packet>(bufferCapacity);
		lock = new ReentrantLock();
		last = System.nanoTime();
	}
	
	public static void bufferAdd(Packet packet){
			buffer.offer(packet);
	}
	

	private void sendAck(){
		Packet ack = buffer.poll();
		int destinationPipe = ack.getOriginatingClientNumber();
		ack.setTimeSentIn(System.nanoTime());
		Client.clientArray.get(destinationPipe).inPipe.addAck(ack);
	}

	private void unloadBuffer(){

		while(!buffer.isEmpty()){
			now = System.nanoTime();
			if(last+handlingDelay>=now){
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
	@Override
	public void run() {
		unloadBuffer();
	}
	
	
}
