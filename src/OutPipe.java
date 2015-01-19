
public class OutPipe extends Pipe{

	public OutPipe(int pipeNum, long pipeLen) {
		super(pipeNum, pipeLen);
	}
	
	public void addPacket(Packet packet){
		packet.setTimeSentOut(System.nanoTime());
		stream.add(packet);
	}
	
	private void dropPacket(){
		stream.remove();
	}
	
	private void sendPacket(){
		if (Server.lock.tryLock()) {
	          try {
	        	  if(!Server.queueFull()){
	        		  Server.queueAdd(stream.poll());
	        	  }else{
	        		  dropPacket();
	        	  }
	          } finally {
	              Server.lock.unlock();
	          }
	      } else {
	          dropPacket();
	      }
	}
	//TODO needs to be a runnable
	protected void movePackets(){
		while(!stream.isEmpty()){
			long now = System.nanoTime();
			if(stream.peek().getTimeSentOut()+pipeLength>=now){
				sendPacket();
			}
		}
		@SuppressWarnings("unused")
		int waitCount = 0;
		while(stream.isEmpty()){
			waitCount++;
		}
		movePackets();
	}
}
