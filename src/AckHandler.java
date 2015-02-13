
public class AckHandler implements Runnable{
	
	private int client;
	private boolean recovering;
	volatile Boolean running;
	protected int lastAck; // packet number of last ack received
	protected int highestBeforeFail;

	
	public AckHandler(int clientNo){
		client = clientNo;
		recovering = false;
		running = true;
		lastAck = -1;
	}
	//use: handleAcks()
	//pre: client exists in Client.clientArray
	//post: ack from head of server DelayQueue dealt with according to packet loss status
	// if packet lost recovery process started
	protected void handleAcks(){

    	Packet ack = null;
    	while(ack == null){
		//take packet passed to synchronous queue from server	
    	try {
    		ack = Client.clientArray.get(client).sync.take();
    		System.out.println(ack.getPacketNumber());
    	} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	// check if ack is the next ack expected
		if((ack.getPacketNumber() == lastAck+1)){
			//end recovery status if recovering
			if(recovering){
				recovering = false;
				System.out.println("recovered");
			}
			//handle success
			Client.clientArray.get(client).handleSuccess(ack);
			System.out.println("ack handled sucessfully");
		// if not next ack expected check if system recovering
		}else if(recovering){
			//do nothing with acks already in flight unless packets lost again
			if(ack.getPacketNumber() > highestBeforeFail){
				//restart recovery if all packets lost
				Client.clientArray.get(client).handleLoss();
				recovering = true;
				System.out.println("loss during recovery");
			}
		//begin recovery
		}else{	
			Client.clientArray.get(client).handleLoss();
			recovering = true;
			System.out.println("packet loss");
		}

	}


	public void interrupt(){
		running = false;
	}
	
	@Override
	public void run() {
		while(running){
			handleAcks();
		}
		
	}

}
