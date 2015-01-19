import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;


public class Client implements Runnable{
	protected long RTT; //nanoseconds
	protected int maxInFlight;
	protected int clientNumber;
	protected int numberOfPackets; // total packets sent, rolled back on failure to lastAck
	protected int packetsInFlight;
	protected long rateOfFire; // time between packets in ns
	protected long last; // time of last packet sent (nanotime)
	protected int lastAck; // packet number of last ack received
	protected boolean ackWaiting;
	protected OutPipe outPipe;
	protected InPipe inPipe;
	//dragons, set to -1  for now to handle zero indexing in arraylist. Need to refactor for clarity.
	private static int numberOfClients = -1;
	public static ArrayList<Client> clientArray= new ArrayList<Client>();
	private ReentrantLock lock;
	
	public Client(long instanceRTT, int instanceMaxInFlight){
		RTT = instanceRTT;
		maxInFlight = instanceMaxInFlight;
		numberOfPackets = 0;
		packetsInFlight = 0;
		rateOfFire = RTT*2;//override this in algorithms with slow start
		last = System.nanoTime()-rateOfFire;// make first packet available to send immediately
		lastAck = 0;
		ackWaiting = false;
		clientNumber = ++numberOfClients;
		//create and start pipes
		OutPipe outPipe = new OutPipe(clientNumber, RTT/2);
		new Thread(outPipe).start();
		InPipe inPipe = new InPipe(clientNumber, RTT/2);
		new Thread(inPipe).start();
		// initialise lock for ack handling
		lock = new ReentrantLock();
		clientArray.add(this);
	}
	//does what it says on the tin
	public static int getClientCount(){
		return numberOfClients;
	}
	//spits out a packet POJO
	protected Packet createPacket(){
		++numberOfPackets;
		Packet packet = new Packet(this.clientNumber, this.numberOfPackets);
		return packet;
	}

	public void notifyAckWaiting(){
	     lock.lock();  // block until condition holds
	     try {
	    	 ackWaiting = true;
	     } finally {
	       lock.unlock();
	     }
	}
	
	protected void handleAck(){
		lock.lock();  // block until condition holds
	    try {
	    	Packet packet = inPipe.getAck();
			if((packet.getPacketNumber()==lastAck+1)){//fix later, needs to handle acks after loss too
				packetsInFlight--;
				handleSuccess(packet);
				ackWaiting = false;
				sendPackets();
			}else{
				handleLoss(packet);
				ackWaiting = false;
				sendPackets();
			}
	    } finally {
		       lock.unlock();
		     }
	}
	
	protected void handleSuccess(Packet packet){
		// overridden in subclasses according to algorithms
	}
	
	protected void handleLoss(Packet packet){
		// overridden in subclasses according to algorithms
	}
	

	protected void sendPackets(){
		//send packets until maximum packets in flight or interrupted by an ack
		while((packetsInFlight<maxInFlight)&&(ackWaiting==false)){
			Packet packet = createPacket();
			long now = System.nanoTime();
			//wait until time interval for next packet **~200ns + time to add packet to outPipe** test latency and granularity of nanotime to confirm
			while(now<last+rateOfFire){
				now = System.nanoTime();
			}
			outPipe.addPacket(packet);
			last = System.nanoTime();
		}
		if(ackWaiting == true){
			handleAck();
			sendPackets();
		}else{
			//busy-wait for ack to arrive
			@SuppressWarnings("unused")
			int waitCount = 0;
			while(ackWaiting == false){
				waitCount++;
			}
			handleAck();
			sendPackets();
		}
	}
	
	@Override
	public void run(){
		sendPackets();
	}
	

}
