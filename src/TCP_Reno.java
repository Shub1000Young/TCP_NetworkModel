
public class TCP_Reno extends Client{

	public TCP_Reno(long instanceRTT, int instanceMaxInFlight) {
		super(instanceRTT, instanceMaxInFlight);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void handleSuccess(Packet packet){
		
	}
	
	@Override
	protected void handleLoss(Packet packet){
		
	}
}
