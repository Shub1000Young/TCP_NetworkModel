
public class TCP_Reno extends Client{

	public TCP_Reno(long instanceRTT, int instanceMaxInFlight) {
		super(instanceRTT, instanceMaxInFlight);
		// TODO code algorithm here
	}
	@Override
	protected void handleSuccess(Packet packet){
		
	}
	
	@Override
	protected void handleLoss(Packet packet){
		
	}
}
