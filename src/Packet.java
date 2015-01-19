
public class Packet {
	//Just a POJO with packet details
	private final int originatingClientNumber;
	private final int packetNumber;
	private Long timeSentIn;
	private Long timeSentOut;
	private Long arrivalTime;
	
	public Packet(int originatingClient, int packetId){
		originatingClientNumber = originatingClient;
		packetNumber = packetId;
		timeSentIn = null;
		timeSentOut = null;
		arrivalTime = null;
	}
	
	public int getOriginatingClientNumber(){
		return originatingClientNumber;
	}
	
	public int getPacketNumber(){
		return packetNumber;
	}
	
	public Long getTimeSentIn(){
		return timeSentIn;
	}
	
	public Long getTimeSentOut(){
		return timeSentOut;
	}
	
	public Long getArrivalTime(){
		return arrivalTime;
	}
	
	public void setTimeSentIn(long timeIn){
		timeSentIn = timeIn;
	}
	
	public void setTimeSentOut(long timeOut){
		timeSentOut = timeOut; 
	}
	
	public void setArrivalTime(long timeArrived){
		arrivalTime = timeArrived;
	}
}
