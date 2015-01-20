
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
	//A bit ugly but should do the job
	//Gets client associated with pipe from Client class arraylist and notifies it 
	//that ack is waiting to be processed 
	private void ackReady(){
		Client.clientArray.get(pipeNumber).notifyAckWaiting();
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
	@Override
	public void run(){
		while(running){
			moveAcks();
		}
	}
}
