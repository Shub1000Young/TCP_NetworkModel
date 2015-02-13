
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
		lastAck = 0;
	}
	
	protected void handleAcks(){

    	Packet ack = null;
		while(ack == null){
			try {
				ack = Client.clientArray.get(client).sync.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if((ack.getPacketNumber() == lastAck+1)){
			if(recovering){
				recovering = false;
				System.out.println("recovered");
			}	
			Client.clientArray.get(client).handleSuccess(ack);
			System.out.println("ack handled sucessfully");
		}else if(recovering){
			//do nothing with acks already in flight unless packets lost again
			if(ack.getPacketNumber() > highestBeforeFail){
				
				Client.clientArray.get(client).handleLoss();
				recovering = true;
				System.out.println("loss during recovery");
			}
		}else{	
			Client.clientArray.get(client).handleLoss();
			recovering = true;
			System.out.println("packet loss");
		}
		//handleAcks();
	}

	public void beginRecovery(){
		recovering = true;
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
