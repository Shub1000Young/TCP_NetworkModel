
public class PacketSender implements Runnable{

	protected int client;
	volatile Boolean running;
	
	public PacketSender(int clientNo){
		client = clientNo;
		running = true;
	}
	
	protected void transferPackets(){

		Packet packet = null;
		try {
			packet =Client.clientArray.get(client).uploadPipe.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*		while(packet == null){
			//do nothing, probably not necessary, try taking out later
		}*/
		if(!Server.lock.isLocked()){
			try{
				Server.lock.lock();
				Server.bufferAdd(packet);
			}finally{
			Server.lock.unlock();
			}
		}
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
