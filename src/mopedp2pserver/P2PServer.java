package mopedp2pserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class P2PServer {
	//List of all mopeds
	//Mopeds can connect to this to get listed
	//Mopeds can timeout
	//App can call this server to recieve the moped list with all IPs
	final int port;

	List<MopedConnection> connections = Collections.synchronizedList(new ArrayList<>());

	public P2PServer(int port) throws Exception {
		//Starts a new welcomer thread.
		this.port = port;

		new Thread(new Welcomer(port)).start();
	}

	/**
	 * Returns the ip of the first occurrence of a client with the same name as
	 * the parameter. Returns "" if there is no occurrence.
	 * 
	 * @param name
	 * @return
	 */
	private String getClientIPByName(String name) {
		name = name.toLowerCase();
		for (int i = 0; i < connections.size(); i++) {
			System.out.println("getIP: " + i + " " + connections.get(i).name);
			if (connections.get(i).name.equals(name)) {
				return connections.get(i).socket.getRemoteSocketAddress().toString()
						.split(":")[0];
			}
		}
		return "";
	}

	private class Welcomer implements Runnable {
		ServerSocket welcomeSocket;

		public Welcomer(int port) throws IOException {
			welcomeSocket = new ServerSocket(port);
		}

		@Override
		public void run() {
			System.out.println("Running Welcomer");
			try {
				while (true) {
					System.out.println("Server: waiting");
					Socket newSocket = welcomeSocket.accept();

					//Prevents duplicate IPs from connecting, preventing terrible things like endless threads and connections.
					boolean duplicateConnection = false;
					for (int i = 0; i < connections.size(); i++) {
						if (newSocket.getInetAddress()
								.equals(connections.get(i).socket.getInetAddress())) {
							System.out.println("Server: duplicate connection, rejected");
							newSocket.close();
							duplicateConnection = true;
							break;
						}
					}
					if (duplicateConnection) {
						continue;
					}

					//Add new connection if it was allowed.
					MopedConnection newConnection = new MopedConnection(newSocket);
					connections.add(newConnection);
					new Thread(newConnection).start();
					System.out.println("Server: new connection");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class MopedConnection implements Runnable {
		//TODO: Timeout timer
		Socket socket;
		DataOutputStream out;
		BufferedReader reader;

		String name = "";
		String slaveName = "";

		public MopedConnection(Socket socket) throws IOException {
			this.socket = socket;
			out = new DataOutputStream(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}

		private void sendNewSlave(String ip) throws UnknownHostException, IOException {
			if (ip.equals(""))
				out.writeBytes("ms\n");
			else
				out.writeBytes("cs§" + ip + "\n");
		}

		private void readMopedPacket() throws IOException {
			String inString = reader.readLine();

			System.out.println("In: " + inString);
			parseMopedPacket(inString);
		}

		private void parseMopedPacket(String inString) {
			String[] strings = inString.split("§");
			if (strings[0].equals("ms") && strings.length == 3) {
				parseNameString(strings[1]);
				parseSlaveNameString(strings[2]);
			} else
				System.out.println("Server: Unknown packet");
		}

		private void parseNameString(String name) {
			name = name.toLowerCase();
			this.name = name;
		}

		private void parseSlaveNameString(String slaveName) {
			this.slaveName = slaveName;
		}

		@Override
		public void run() {
			try {
				while (true) {
					System.out.println("connection loop");
					//Move out of loop? Might only be needed on new connections?
					readMopedPacket();

					sendNewSlave(getClientIPByName(slaveName));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally
			{
				try {
					socket.close();
					connections.remove(this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}