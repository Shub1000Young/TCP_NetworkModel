
public class AckQueuer implements Runnable{
	private static long handlingDelay;
	private static long last;
	public volatile Boolean running = true; 
	
	public AckQueuer(long delay){
		handlingDelay = delay;
		last = System.nanoTime()-handlingDelay;
	}
	private static void queueAcks(){
		while(!Server.buffer.isEmpty()){
			if(last+handlingDelay<=System.nanoTime()){
				try {
					Server.outputQueue.add(Server.buffer.take());
					last = System.nanoTime();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//queueAcks();
	}
	
	public void interrupt(){
		running = false;
	}
	@Override
	public void run() {
		while(running){
			queueAcks();
		}
		
	}

}
