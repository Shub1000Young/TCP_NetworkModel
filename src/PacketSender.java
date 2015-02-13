
public class PacketSender implements Runnable{

	protected int client;
	volatile Boolean running;
	
	public PacketSender(int clientNo){
		client = clientNo;
		running = true;
	}
	
	
	//use: transferPackets()
	//pre: none
	//post: packet added to server buffer if server not busy, otherwise packet dropped
	protected void transferPackets(){

		Packet packet = null;
		//takes packet from end of Client delayQueue, waiting if necessary
		try {
			packet =Client.clientArray.get(client).uploadPipe.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//test for server lock
		if(!Server.lock.isLocked()){
			//add packet to server buffer if server not locked
			try{
				Server.lock.lock();
				Server.bufferAdd(packet);
			}finally{
			Server.lock.unlock();
			}
		}
		//packet dropped if server locked
	}

	public void interrupt(){
		running = false;
	}
	
	@Override
	public void run() {
		while(running){
			transferPackets();
		}
		
	}

}
