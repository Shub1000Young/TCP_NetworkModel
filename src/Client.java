import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
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
	protected int highestBeforeFail;
	protected boolean recovering;
	//protected boolean ackWaiting;
	protected OutPipe outPipe;
	protected InPipe inPipe;
	//dragons, set to -1  for now to handle zero indexing in arraylist. Need to refactor for clarity.
	private static int numberOfClients = -1;
	public static ArrayList<Client> clientArray= new ArrayList<Client>();
	
	private ReentrantLock lock;
	protected ArrayList<Packet> resultArray;
	public static ArrayList<ArrayList<Packet>> masterResultArray = new ArrayList<ArrayList<Packet>>();
	protected LinkedBlockingQueue<Packet> buffer; 
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
		//create and start pipes
		outPipe= new OutPipe(clientNumber, RTT/2);
		new Thread(outPipe).start();
		inPipe = new InPipe(clientNumber, RTT/2);
		new Thread(inPipe).start();
		// initialise lock for ack handling
		lock = new ReentrantLock();
		clientArray.add(this);
		resultArray= new ArrayList<Packet>();
		masterResultArray.add(resultArray);
		running = true;
		buffer = new LinkedBlockingQueue<Packet>();
		System.out.println("Client created");
	}
	//does what it says on the tin
	public static int getClientCount(){
		return numberOfClients;
	}
	//spits out a packet POJO
	protected Packet createPacket(){
		++numberOfPackets;
		Packet packet = new Packet(clientNumber, numberOfPackets);
		return packet;
	}


	protected void handleAck(){
		lock.lock();  // block until condition holds
	    try {
	    	Packet packet = buffer.poll();//inPipe.getAck();
	    	packet.setArrivalTime(System.nanoTime());
	    	//add to logging here
			if((packet.getPacketNumber()==lastAck+1)){
				if(recovering){
					recovering = false;
				}
				packetsInFlight--;
				handleSuccess(packet);
				sendPackets();
			}else if(recovering){
				//do nothing with acks already in flight unless packets lost again
				if(packet.getPacketNumber()>highestBeforeFail){
					handleLoss();
				}
			}else{	
				handleLoss();
				sendPackets();
			}
			System.out.println("ack handled");
	    } finally {
		       lock.unlock();
		     }
	}
	
	protected void handleSuccess(Packet ack){
		rateOfFire = rateOfFire-(rateOfFire*(long)0.01);//overridden in subclasses according to algorithms
		ack.setArrivalRateOfFire(rateOfFire);
		resultArray.add(ack);
		lastAck = ack.getPacketNumber();
		System.out.println("hit");
	}
	
	protected void handleLoss(){
		rateOfFire=rateOfFire+(rateOfFire*(long)0.5);// overridden in subclasses according to algorithms
		
		highestBeforeFail=numberOfPackets;
		numberOfPackets = lastAck;
		packetsInFlight = 0;
		recovering = true;
		System.out.println("missed");
		
	}


	protected void sendPackets(){
		//send packets until maximum packets in flight or interrupted by an ack
		while((packetsInFlight<maxInFlight)&&(buffer.isEmpty())){
			Packet packet = createPacket();
			long now = System.nanoTime();
			//wait until time interval for next packet **~200ns + time to add packet to outPipe** test latency and granularity of nanotime to confirm
			while(now<last+rateOfFire){
				now = System.nanoTime();
			}
			outPipe.addPacket(packet);
			System.out.println("packet sent to outpipe" + clientNumber);
			packetsInFlight++;
			last = System.nanoTime();
		}
		if(!buffer.isEmpty()){
			handleAck();
			sendPackets();
		}else{
			//busy-wait for ack to arrive
			@SuppressWarnings("unused")
			int waitCount = 0;
			while(buffer.isEmpty()){
				waitCount++;
			}
			handleAck();
			sendPackets();
		}
	}
	public void interrupt(){
		running = false;
	}
	public static void saveData(){
		try {
		    FileOutputStream fos = new FileOutputStream("output");
		    ObjectOutputStream oos = new ObjectOutputStream(fos);   
		    oos.writeObject(masterResultArray); 
		    oos.close(); 
		} catch(Exception ex) {
		    ex.printStackTrace();
		}
	}
	
	@Override
	public void run(){
		while(running){
			sendPackets();
		}
		outPipe.interrupt();
		inPipe.interrupt();
	}
	

}
