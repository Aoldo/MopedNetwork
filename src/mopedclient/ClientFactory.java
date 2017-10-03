package mopedclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Properties;

/**
 * This class creates an instance of P2PServer and runs it. The port that the
 * server runs on is decided in the config file using {@see configPath} as file
 * name. The config file will be created if it is missing, such as on the first
 * run of the class.
 * 
 * TODO: Rename to ClientFactory and make main return the MopedClient class,
 * when everything is ready to integrated instead of standalone.
 * 
 * @author Mattias
 *
 */
public class ClientFactory {
	/**
	 * The name of the config file, if there should be any conflict with file
	 * names just change this to something else.
	 */
	final static String configPath = "clientconfig.properties";
	static Properties properties;

	/**
	 * Used to test the client, use createAndRunClient for general use.
	 * 
	 * @see createAndRunClient
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String args[]) throws InterruptedException {
		MopedClient client = createAndRunClient();
		int gyro = 0;
		int vel = 0;

		while (true) {
			client.setSensorData(gyro, vel);
			gyro++;
			vel++;

			Thread.sleep(100);

			System.out.println(
					"DebugMain: Gyro:" + client.getGyro() + "/" + gyro + "Vel:" + client.getVelocity() + "/" + vel);
		}

	}

	/**
	 * Attempts to create a new MopedClient and run it. If successful it will
	 * return a MopedClient that is currently running on a new thread. If it
	 * fails it will return null.
	 * 
	 * @return
	 */
	public static MopedClient createAndRunClient() {
		try {
			properties = loadProperties();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Exception thrown, creating new config file.");
			try {
				properties = createProperties();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Exception thrown, probably failed at creating new properties too. Returning null.");
				return null;
			}
		}

		try {
			MopedClient client = new MopedClient(InetAddress.getByName(properties.getProperty("serverip")),
					Integer.parseInt(properties.getProperty("port")), properties.getProperty("mopedname"),
					properties.getProperty("followername"));
			new Thread(client).start();

			return client;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a new config file.
	 * 
	 * @return
	 * @throws IOException
	 */
	private static Properties createProperties() throws IOException {
		Properties properties = new Properties();
		properties.setProperty("mopedname", "vroom");
		properties.setProperty("followername", "nneeaoowww");
		properties.setProperty("serverip", "127.0.0.1");
		properties.setProperty("port", "6112");

		saveProperties(properties);
		return properties;

	}

	/**
	 * Saves the config file.
	 * 
	 * @param properties
	 * @throws IOException
	 */
	private static void saveProperties(Properties properties) throws IOException {
		OutputStream out = new FileOutputStream(configPath);
		properties.store(out, null);
	}

	/**
	 * Loads the config file.
	 * 
	 * @return
	 * @throws IOException
	 */
	private static Properties loadProperties() throws IOException {
		InputStream in = new FileInputStream(configPath);
		Properties properties = new Properties();
		properties.load(in);
		return properties;
	}
}