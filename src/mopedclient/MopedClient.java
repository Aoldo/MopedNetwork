package mopedclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MopedClient implements Runnable {

	private Socket serverSocket;
	/**
	 * Size of UDP packets in bytes.
	 */
	private final int udpPacketSize = 8;

	/**
	 * Contains the data that will be send out the udp socket before it is sent.
	 */
	private byte[] udpOutDataBuffer;
	/**
	 * Contains the data coming in from the udp socket. Do not touch this
	 * directly since the udp reciever is on another thread so it will probably
	 * cause race conditions.
	 */
	private byte[] udpInDataBuffer;

	/**
	 * Contains the received data from the udp socket. Only touch this via
	 * synchronized methods since it is written to from another thread and can
	 * be read from via a public synchronized method.
	 */
	private byte[] receivedData;

	/**
	 * Name of the moped, used by other clients to find this client via the p2p
	 * server.
	 */
	private final String name;
	/**
	 * Name of the moped following this one, used by this client to find the
	 * following moped via the p2p server.
	 */
	private String slaveName;

	/**
	 * The port that the sockets work on.
	 */
	private int port;

	/**
	 * Used to send things to the p2p server.
	 */
	private DataOutputStream out;
	/**
	 * Used to read things from the p2p server.
	 */
	private BufferedReader in;

	/**
	 * The ip of the moped following this one, in text form.
	 */
	private String slaveIP = "";
	/**
	 * The ip of the moped following this one, the destination of all data sent
	 * excluding the server connection.
	 */
	private InetAddress udpTargetIP;
	/**
	 * Socket used for sending data to the following moped aswell as receiving
	 * data from the one ahead.
	 */
	private DatagramSocket udpSocket;

	public MopedClient(InetAddress ip, int port, String name, String slaveName) throws IOException {
		System.out.println("ip " + ip.toString());
		serverSocket = new Socket(ip, port);
		this.name = name;
		this.slaveName = slaveName;
		this.port = port;
		udpSocket = new DatagramSocket(port);
		udpOutDataBuffer = new byte[udpPacketSize];
		receivedData = new byte[udpPacketSize];
		udpInDataBuffer = new byte[udpPacketSize];
		new Thread(new UDPReceiver()).start();
		in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
		out = new DataOutputStream(serverSocket.getOutputStream());
	}

	/**
	 * Updates the values that should be sent to the follower moped.
	 * 
	 * @param gyro
	 *            The value read from the gyro.
	 * @param velocity
	 *            The value read from the wheel-speed-thing TODO: Proper name of
	 *            that thing
	 */
	public synchronized void setSensorData(int gyro, int velocity) {
		byte[] bytes = new byte[udpPacketSize];
		System.arraycopy(ByteBuffer.allocate(4).putInt(gyro).array(), 0, bytes, 0, 4);
		System.arraycopy(ByteBuffer.allocate(4).putInt(velocity).array(), 0, bytes, 4, 4);

		udpOutDataBuffer = bytes;
	}

	/**
	 * Checks the validity of the string recieved from the server, and then
	 * calls appropriate methods, depending on what the string contains.
	 * 
	 * 
	 * @see updateSlaveSocket
	 * @param packetData
	 */
	private void readPacket(String packetData) {

		System.out.println(name + ": Read packet");
		String[] strings = packetData.split("§");
		if (strings.length > 1) {
			if (strings[0].equals("cs")) {
				System.out.println(name + ": ChangeSlave received");
				updateSlaveSocket(strings[1].split("/")[1]);
			} else {
				System.out.println(name + ": unknown packet: " + packetData);
			}
		} else if (packetData.equals("ms")) {
			System.out.println(name + ": missing slave recieved");
		} else {
			System.out.println(name + ": unknown packet: " + packetData);
		}
	}

	/**
	 * Recreates the UDP socket, with the new IP as the target. Called after
	 * receiving a packet telling the client the IP of the moped following it.
	 * 
	 * @param ip
	 *            The IP in string format, ex: 127.0.0.1
	 */
	private void updateSlaveSocket(String ip) {
		System.out.println(name + ": New slave ip: " + ip);
		try {
			slaveIP = ip;
			udpTargetIP = InetAddress.getByName(ip);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a packet to the server contain this clients name aswell as the name
	 * of the moped that should be following it. The server should then return a
	 * packet containing the IP of said moped so that this client can send its
	 * data to it.
	 * 
	 * @see readPacket
	 * @throws IOException
	 */
	private void sendDataToServer() throws IOException {

		out.writeBytes("ms§" + name + "§" + slaveName + "\n");
	}

	/**
	 * Stores the parameter to the receivedData variable. Only use this if
	 * writing to receivedData to avoid race conditions.
	 * 
	 * @param data
	 */
	private synchronized void storeReceivedData(byte[] data) {
		receivedData = data;
	}

	/**
	 * Returns the data received from the other mopeds in the form it had when
	 * received as a packet, a byte array.
	 * 
	 * @return
	 */
	public synchronized byte[] getReceivedData() {
		return receivedData;
	}

	/**
	 * Returns the gyro values, extracted from the receivedData.
	 * 
	 * @return
	 */
	public synchronized int getGyro() {
		byte[] gyroBytes = new byte[udpPacketSize / 2];
		System.arraycopy(receivedData, 0, gyroBytes, 0, 4);
		return ByteBuffer.wrap(gyroBytes).getInt();
	}

	/**
	 * Returns the velocity values, extracted from the receivedData.
	 * 
	 * @return
	 */
	public synchronized int getVelocity() {
		byte[] gyroBytes = new byte[udpPacketSize / 2];
		System.arraycopy(receivedData, gyroBytes.length, gyroBytes, 0, 4);
		return ByteBuffer.wrap(gyroBytes).getInt();
	}

	/**
	 * Main loop of the moped client, sends and receives data from the server.
	 */ //TODO: Change to just send pings with less data after receiving an ip?
	@Override
	public void run() {
		System.out.println(name + ": Running moped client");
		try {

			while (true) {
				//TODO: Timeout stuff
				sendDataToServer();

				readPacket(in.readLine());

				sendUDPData();

				Thread.sleep(100);
			}
		} catch (Exception e) {
			//TODO: If the server disconnects this throws an exception, effectively terminating the entire client
			//TODO: Needs a solution for reconnecting? Or just do what Hearthstone did and leave it like this :DDDD
			e.printStackTrace();
			System.out.println(name + ": exception throw. Loop stopping.");

		} finally {
			udpSocket.close();
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(name + ": Sockets closed");
		}
	}

	/**
	 * Sends {@see udpOutData} to {@see udpTargetIp}, on {@see port}.
	 * 
	 * @param data
	 */
	public void sendUDPData() {
		if (!slaveIP.equals("")) {

			DatagramPacket packet = new DatagramPacket(udpOutDataBuffer, udpOutDataBuffer.length, udpTargetIP, port);
			try {
				System.out.println(name + ": Sending UDP: " + Arrays.toString(udpOutDataBuffer));
				udpSocket.send(packet);

				Thread.sleep(100);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Runnable private class that loops and reads UDP packets on {@see port}.
	 * Stores the packet bytes in {@see receivedData}.
	 * 
	 * @author Mattias
	 *
	 */
	private class UDPReceiver implements Runnable {
		@Override
		public void run() {
			System.out.println(name + ": UDPReceiver: Running");
			while (true) {
				DatagramPacket packet = new DatagramPacket(udpInDataBuffer, udpInDataBuffer.length);

				storeReceivedData(packet.getData());

				System.out.println(name + ": Receiving UDP");
				try {
					udpSocket.receive(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(name + ": UDP in: " + Arrays.toString(udpInDataBuffer));
			}
		}
	}
}