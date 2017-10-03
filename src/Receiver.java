import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class Receiver extends NetworkThingWorkingTitle implements Runnable {

	public Receiver(int port) throws SocketException {
		socket = new DatagramSocket(port);
	}

	@Override
	public void run() {
		System.out.println("Receiver running");

		DatagramPacket packet = new DatagramPacket(packetData, packetData.length);

		try {
			for (int i = 0; i < 100; i++) {
				Thread.sleep(300);
				System.out.println("Receiving");
				socket.receive(packet);
				System.out.println("Data: " + Arrays.toString(packetData));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			socket.close();
		}
	}
}
