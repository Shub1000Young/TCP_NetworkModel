//import java.util.ArrayList;


public class Main {
	//testing values fill in with CLI later
	public static void main(String[] args) {

		Server server = new Server(100, 25000);
		Client client= new Client(100000,200);
		Client client1= new Client(750000,200);
		Client client2= new Client(50000,200);
		new Thread(server).start();
		new Thread(client).start();
		new Thread(client1).start();
		new Thread(client2).start();

		
	}

}
