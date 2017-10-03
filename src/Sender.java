import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class Sender extends NetworkThingWorkingTitle implements Runnable {
	int port;
	InetAddress ip;

	public Sender(int port, InetAddress targetAddress) throws SocketException {
		ip = targetAddress;
		socket = new DatagramSocket();
		this.port = port;

	}

	@Override
	public void run() {
		System.out.println("Sender running");
		packetData[0] = (byte) 0xFF;

		System.arraycopy(ByteBuffer.allocate(4).putInt(0xFFFFFFFF).array(), 0, packetData,
				1, 4);
		DatagramPacket packet = new DatagramPacket(packetData, packetData.length, ip,
				port);

		try {
			for (int i = 0; i < 100; i++) {
				Thread.sleep(300);
				socket.send(packet);
				packetData[0]++;
				packetData[4]--;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			socket.close();
		}
	}
}