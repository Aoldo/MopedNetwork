import java.net.InetAddress;
import java.net.SocketException;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Start");

		try {
			new Thread(new Receiver(6112)).start();
			new Thread(new Sender(6112, InetAddress.getByName("localhost"))).start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
