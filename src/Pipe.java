import java.util.LinkedList;

public class Pipe {
	protected final int pipeNumber;
	protected long pipeLength;
	protected LinkedList<Packet> stream; 
	protected volatile boolean running = true;
	
	public Pipe (int pipeNum, long pipeLen){
		pipeNumber = pipeNum;
		pipeLength = pipeLen;
		stream = new LinkedList<Packet>();

	}
	
	public int getPipeNumber(){
		return pipeNumber;
	}
	// RTT/2
	public long getPipeLength(){
		return pipeLength;
	}
	//might want to simulate reroute later. Needs to be done for in and out pipe in client
	public void setPipeLength(Long newPipeLength){
		pipeLength = newPipeLength;
	}
	
	public void interrupt(){
		running = false;
	}
}
