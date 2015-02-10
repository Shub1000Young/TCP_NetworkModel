import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;


public class Packet implements Delayed{
	private final int originatingClientNumber;
	private final int packetNumber;
	private long delay;
	private Long timeSentOut;
	private Long arrivalTime;
	private Long arrivalRateOfFire;
	
	public Packet(int originatingClient, int packetId, long pipeDelay){
		originatingClientNumber = originatingClient;
		packetNumber = packetId;
		delay = pipeDelay;
		timeSentOut = System.nanoTime();
		arrivalTime = timeSentOut + delay;
		arrivalRateOfFire = null;
	}
	
	@Override
	public long getDelay(TimeUnit NANOSECONDS){
		return arrivalTime-System.nanoTime();
	}
	public int getOriginatingClientNumber(){
		return originatingClientNumber;
	}
	
	public int getPacketNumber(){
		return packetNumber;
	}
	
	
	public Long getTimeSentOut(){
		return timeSentOut;
	}
	
	public Long getArrivalTime(){
		return arrivalTime;
	}
	
	public Long getArrivalRateOfFire(){
		return arrivalRateOfFire;
	}
		
	public void setTimeSentOut(long timeOut){
		timeSentOut = timeOut; 
	}
		
	public void setArrivalRateOfFire(long rateOfFire){
		arrivalRateOfFire = rateOfFire;
	}
	
	//dirty fix here for null pointer exceptions
	@Override
	public int compareTo(Delayed o) {
        if(((Packet)o)!= null){
        	if (this.arrivalTime < ((Packet) o).arrivalTime) {
            	return -1;
        	}
			if (this.arrivalTime > ((Packet) o).arrivalTime){
        		return 1;
        	}
        	return 0;
        }else{
        	return -1;
        }
        
    }

}
