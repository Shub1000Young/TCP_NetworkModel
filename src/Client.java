import java.util.ArrayList;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.SynchronousQueue;


public class Client implements Runnable{
	
	//class data structures
	public static ArrayList<Client> clientArray= new ArrayList<Client>();
	public static ArrayList<ArrayList<Packet>> masterResultArray = new ArrayList<ArrayList<Packet>>();

	//class variables
	//set to -1  to handle zero indexing in arraylist.
	private static int numberOfClients = -1;

	//instance data structures
	protected ArrayList<Packet> resultArray;	
	protected DelayQueue<Packet> uploadPipe;
	public SynchronousQueue<Packet> sync;
	
	//child thread instances
	AckHandler ackHandler;
	PacketSender packetSender;
	
	//instance variables
	protected long RTT; //nanoseconds
	protected int maxInFlight;
	protected int clientNumber;
	protected int numberOfPackets; // total packets sent, rolled back on failure to lastAck
	protected int packetsInFlight;
	protected long rateOfFire; // time between packets in ns
	protected long last; // time of last packet sent (nanotime)

	volatile boolean running = false;
	
	
	/*
	 * 
	 * 
	 */
	public Client(long instanceRTT, int instanceMaxInFlight){
		RTT = instanceRTT;
		maxInFlight = instanceMaxInFlight;
		numberOfPackets = 0;
		packetsInFlight = 0;
		rateOfFire = RTT*2;//override this in algorithms with slow start
		last = System.nanoTime()-rateOfFire;// make first packet available to send immediately
		clientNumber = ++numberOfClients;
		sync = new SynchronousQueue<Packet>(true);
		resultArray= new ArrayList<Packet>();		
		masterResultArray.add(resultArray);
		uploadPipe = new DelayQueue<Packet>();
		ackHandler = new AckHandler(clientNumber);
		packetSender = new PacketSender(clientNumber);
		clientArray.add(this);
		new Thread(ackHandler).start();
		new Thread(packetSender).start();
		running = true;
		System.out.println("Client created"); 
	}
	//does what it says on the tin, zero indexed
	public static int getClientCount(){
		return numberOfClients;
	}
	//spits out a packet POJO
	protected Packet createPacket(){
		++numberOfPackets;
		Packet packet = new Packet(clientNumber, numberOfPackets, RTT/2);
		return packet;
	}

	//use: Client.clientArray.get(clientNumber).handleSuccess()
	//pre: ack.packetNumber == lastAck + 1
	//post: ack added to result array, lastAck == ack.packetNumber , packets in flight decremented
	protected void handleSuccess(Packet ack){
		increaseROF();
		ack.setArrivalRateOfFire(rateOfFire);
		resultArray.add(ack);
		ackHandler.lastAck = ack.getPacketNumber();
		packetsInFlight--;
	}
	private void increaseROF(){
		rateOfFire = rateOfFire-(rateOfFire*(long)0.01);//overridden in subclasses according to algorithm
	}
	
	//use: Client.clientArray.get(clientNumber).handleFailure()
	//pre: ack.packetNumber != lastAck+1
 	//post: sending restarted from last good packet +1
	protected void handleLoss(){			
		decreaseROF();
		ackHandler.highestBeforeFail = numberOfPackets;
		numberOfPackets = ackHandler.lastAck;
		packetsInFlight = 0;		
	}

	private void decreaseROF(){
		rateOfFire=rateOfFire+(rateOfFire*(long)0.5);// overridden in subclasses according to algorithms
	}
	
	//use: queuePackets()
	//pre: packetsInFlight <= maxInFlight
	//post: packetsInFlight == maxInFlight
	private void queuePackets(){
		while((packetsInFlight<maxInFlight)){
			//avoid rate of fire changes interfering
			long rof = rateOfFire;
			if (System.nanoTime()+rof>=last+rof) {
				Packet packet = createPacket();
				packetsInFlight++;
				uploadPipe.add(packet);
			}
		}
	}
	
	
	public void interrupt(){
		running = false;
	}

	
	@Override
	public void run(){

		while(running){
			queuePackets();
		}
	}
}
