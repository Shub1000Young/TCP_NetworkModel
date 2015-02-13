
public class AckQueuer implements Runnable{
	private static long handlingDelay;
	private static long last;
	public volatile Boolean running = true; 
	
	public AckQueuer(long delay){
		handlingDelay = delay;
		last = System.nanoTime()-handlingDelay;
	}
	private static void queueAcks(){
		while(true){
			//wait for handling delay to elapse-has to be a better way to do this
			if(last+handlingDelay<=System.nanoTime()){
				//add head of buffer to tail of delayQueue
				try {
					Server.outputQueue.add(Server.buffer.take());
					last = System.nanoTime();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
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
