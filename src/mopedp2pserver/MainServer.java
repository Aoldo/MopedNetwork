package mopedp2pserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class MainServer {
	final static String configPath = "serverconfig.properties";
	static Properties properties;

	public static void main(String[] args) {
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
				System.out.println("Exception thrown, probably failed at creating new properties too. Stopping.");
				return;
			}
		}

		try {
			new P2PServer(Integer.parseInt(properties.getProperty("port")));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Server creation failed");
		}
	}

	private static Properties createProperties() throws IOException {
		Properties properties = new Properties();
		properties.setProperty("port", "6112");

		saveProperties(properties);
		return properties;

	}

	private static void saveProperties(Properties properties) throws IOException {
		OutputStream out = new FileOutputStream(configPath);
		properties.store(out, null);
	}

	private static Properties loadProperties() throws IOException {
		InputStream in = new FileInputStream(configPath);
		Properties properties = new Properties();
		properties.load(in);
		return properties;
	}
}
