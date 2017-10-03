package tcp;


import mopedclient.ClientFactory;
import mopedp2pserver.MainServer;
import mopedp2pserver.P2PServer;

/**
 * Main class used to run & test the classes.
 * @author Mattias
 *
 */
public class Main {

	public static void main(String[] args) {
		P2PServer server;
		try {
			System.out.println("Starting");
			MainServer.main(null);
			//ClientFactory.main(null);
			
			Thread.sleep(200);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}