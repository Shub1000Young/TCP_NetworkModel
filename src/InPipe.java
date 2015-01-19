
public class InPipe extends Pipe{

	public InPipe(int pipeNum, long pipeLen) {
		super(pipeNum, pipeLen);
	}

	public void addPacket(Packet packet){
		packet.setTimeSentIn(System.nanoTime());
		stream.add(packet);
	}
	
	public Packet getAck(){
		return stream.poll();
	}
	
	private void ackReady(){
		//need to sort out Map of clients to get this working
	}
}
