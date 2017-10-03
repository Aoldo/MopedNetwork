import java.net.DatagramSocket;

/**
 * Contains variables that are used by both the Receiver and the Sender classes.
 * 
 * @author Mattias
 *
 */
public abstract class NetworkThingWorkingTitle {
	DatagramSocket socket;

	//Gyro seems to be 16bit, degree/sec 
	//TODO: Wheels seem to be __bit, ______
	byte[] packetData = new byte[5];

}
