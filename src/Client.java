import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.SynchronousQueue;


public class Client implements Runnable{
	protected long RTT; //nanoseconds
	protected int maxInFlight;
	protected int clientNumber;
	protected int numberOfPackets; // total packets sent, rolled back on failure to lastAck
	protected int packetsInFlight;
	protected long rateOfFire; // time between packets in ns
	protected long last; // time of last packet sent (nanotime)
	protected int lastAck; // packet number of last ack received
	protected int highestBeforeFail;
	protected boolean recovering;
	protected OutPipe outPipe;
	protected InPipe inPipe;
	//dragons, set to -1  for now to handle zero indexing in arraylist. Need to refactor for clarity.
	private static int numberOfClients = -1;
	public static ArrayList<Client> clientArray= new ArrayList<Client>();
	protected ArrayList<Packet> resultArray;
	public static ArrayList<ArrayList<Packet>> masterResultArray = new ArrayList<ArrayList<Packet>>();
	protected DelayQueue<Packet> buffer;
	public SynchronousQueue<Packet> sync;
	volatile boolean running;
	
	public Client(long instanceRTT, int instanceMaxInFlight){
		RTT = instanceRTT;
		maxInFlight = instanceMaxInFlight;
		numberOfPackets = 0;
		packetsInFlight = 0;
		rateOfFire = RTT*2;//override this in algorithms with slow start
		last = System.nanoTime()-rateOfFire;// make first packet available to send immediately
		lastAck = 0;
		clientNumber = ++numberOfClients;
		sync = new SynchronousQueue<Packet>();
		resultArray= new ArrayList<Packet>();		
		masterResultArray.add(resultArray);
		running = true;
		buffer = new DelayQueue<Packet>();
		clientArray.add(this);
		System.out.println("Client created");
	}
	//does what it says on the tin
	public static int getClientCount(){
		return numberOfClients;
	}
	//spits out a packet POJO
	protected Packet createPacket(){
		++numberOfPackets;
		Packet packet = new Packet(clientNumber, numberOfPackets, RTT/2);
		return packet;
	}


	protected void handleAck(){

	    	Packet packet = null;
			while(packet == null){
				try {
					packet = sync.take();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    	//add to logging here
			if((packet.getPacketNumber()==lastAck+1)){
				if(recovering){
					recovering = false;
					System.out.println("recovered");
				}	
				handleSuccess(packet);
				packetsInFlight--;
				System.out.println("ack handled sucessfully");
			}else if(recovering){
				//do nothing with acks already in flight unless packets lost again
				if(packet.getPacketNumber()>highestBeforeFail){
					handleLoss();
					System.out.println("loss during recovery");
				}
			}else{	
				handleLoss();
				System.out.println("packet loss");
			}
			handleAck();
	}
	
	protected void handleSuccess(Packet ack){
		rateOfFire = rateOfFire-(rateOfFire*(long)0.01);//overridden in subclasses according to algorithms
		ack.setArrivalRateOfFire(rateOfFire);
		resultArray.add(ack);
		lastAck = ack.getPacketNumber();
	}
	
	protected void handleLoss(){
		rateOfFire=rateOfFire+(rateOfFire*(long)0.5);// overridden in subclasses according to algorithms
		
		highestBeforeFail=numberOfPackets;
		numberOfPackets = lastAck;
		packetsInFlight = 0;
		recovering = true;		
	}

	protected void bufferPackets(){
		while((packetsInFlight<maxInFlight)){
			Packet packet = createPacket();
			packetsInFlight++;
			buffer.add(packet);
		}
	}
	
	protected void sendPackets(){
		Packet packet = null;
		try {
			packet =buffer.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(buffer == null){
			//do nothing, probably not necessary, try taking out later
		}
		try{
			Boolean serverAvailable=Server.lock.tryLock();
			if(serverAvailable){
				Server.bufferAdd(packet);
			}
		}finally{
			Server.lock.unlock();
		}
		sendPackets();
	}

	public void interrupt(){
		running = false;
	}

	
	@Override
	public void run(){

		while(running){
			bufferPackets();
		}
	}
	

}
