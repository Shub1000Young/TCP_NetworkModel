
public class TCP_Vegas extends Client{

	public TCP_Vegas(long instanceRTT, int instanceMaxInFlight) {
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
