
public class TCP_Tahoe extends Client{

	public TCP_Tahoe(long instanceRTT, int instanceMaxInFlight) {
		super(instanceRTT, instanceMaxInFlight);
		// TODO code algorithm here
	}

	@Override
	protected void handleSuccess(Packet packet){
		
	}
	
	@Override
	protected void handleLoss(){
		
	}
}
