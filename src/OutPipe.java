


public class OutPipe extends Pipe implements Runnable{	
	
	public OutPipe(int pipeNum, long pipeLen) {
		super(pipeNum, pipeLen);
		System.out.println("outpipe "+ pipeNumber +" created");
		
	}
	
	public void addPacket(Packet packet){
		packet.setTimeSentOut(System.nanoTime()+ pipeLength);
		System.out.println("packet "+ packet.getPacketNumber() +" added to stream" + pipeNumber);
		stream.add(packet);
		System.out.println("outpipe queue length="+stream.size());
	}
	
	
	private void sendPacket(){
		if (Server.lock.tryLock()) {
	          try {
	        	  Server.bufferAdd(stream.poll());
	        	  System.out.println("packet sent to server from " + pipeNumber);

	          } finally {
	              Server.lock.unlock();
	          }
	      } else {
	          stream.remove();
	          System.out.println("server buffer full");
	      }
	}
	
	protected void movePackets(){
		while(!stream.isEmpty()){
			long now = System.nanoTime();
			System.out.println("now" + now);
			System.out.println("outTime" + stream.peek().getTimeSentOut());
			System.out.println("now- outTime=" + Long.toString(now-stream.peek().getTimeSentOut()));
			if(now-stream.peek().getTimeSentOut() >= 0){
				System.out.println("packet sent to server");
				sendPacket();
			}
		}
		@SuppressWarnings("unused")
		int waitCount = 0;
		while(stream.isEmpty()){
			//System.out.println("outpipe waiting");
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
