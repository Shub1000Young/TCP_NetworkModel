import java.util.concurrent.TimeUnit;


public class InPipe extends Pipe implements Runnable{
	
	
	public InPipe(int pipeNum, long pipeLen) {
		super(pipeNum, pipeLen);
		System.out.println("inpipe"+ pipeNumber +"created");


	}

	public void addAck(Packet packet){
		packet.setTimeSentIn(System.nanoTime());
		stream.add(packet);
	}
	private void passAck() throws InterruptedException{
		//ugly monster but should get it done. refactor later
		//gets client associated with pipe from client array, 
		//passes head of pipe stream to tail of client buffer. 
		//waits up to 2 seconds if buffer is busy
		Client.clientArray.get(pipeNumber).buffer.offer(stream.poll(), 2, TimeUnit.SECONDS);

	}
	
	protected void moveAcks(){
		while(!stream.isEmpty()){
			long now = System.nanoTime();
			if(stream.peek().getTimeSentIn()+pipeLength<=now){
				try {
					passAck();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
