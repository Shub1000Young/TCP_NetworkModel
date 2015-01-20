


public class OutPipe extends Pipe implements Runnable{	
	
	public OutPipe(int pipeNum, long pipeLen) {
		super(pipeNum, pipeLen);
		
	}
	
	public void addPacket(Packet packet){
		packet.setTimeSentOut(System.nanoTime());
		stream.add(packet);
	}
	
	
	private void sendPacket(){
		if (Server.lock.tryLock()) {
	          try {
	        	  Server.bufferAdd(stream.poll());

	          } finally {
	              Server.lock.unlock();
	          }
	      } else {
	          stream.remove();
	      }
	}
	
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
	
	
	@Override
	public void run(){
		while(running){
			movePackets();
		}
	}
}
