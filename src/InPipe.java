
public class InPipe extends Pipe implements Runnable{

	public InPipe(int pipeNum, long pipeLen) {
		super(pipeNum, pipeLen);
	}

	public void addAck(Packet packet){
		packet.setTimeSentIn(System.nanoTime());
		stream.add(packet);
	}
	
	public Packet getAck(){
		return stream.poll();
	}
	
	private void ackReady(){
		//need to sort out Map of clients to get this working
	}
	
	protected void moveAcks(){
		while(!stream.isEmpty()){
			long now = System.nanoTime();
			if(stream.peek().getTimeSentIn()+pipeLength>=now){
				ackReady();
			}
		}
		@SuppressWarnings("unused")
		int waitCount = 0;
		while(stream.isEmpty()){
			waitCount++;
		}
		moveAcks();
	}
	
	public void run(){
		moveAcks();
	}
}
