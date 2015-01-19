
public class InPipe extends Pipe{

	public InPipe(int pipeNum, long pipeLen) {
		super(pipeNum, pipeLen);
	}

	public void addPacket(Packet packet){
		packet.setTimeSentIn(System.currentTimeMillis());
		stream.add(packet);
	}
	
	public Packet getAck(){
		return stream.poll();
	}
	
	private void ackReady(){
		
	}
}
