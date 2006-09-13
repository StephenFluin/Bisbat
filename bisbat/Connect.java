/** NightShade */ /* Aug 14, 2006 */

package bisbat;

import java.io.*;
import java.net.*;

public class Connect {
	/* Opens a connection to a socket
	 * Prints input to/from system and socket */
	
	private Socket socket;
	private RecieveGameOutput in;
	private SendCommands out;
	
	public Connect(String s, int port) {
		try {
			socket = new Socket(s, port);
			in = new RecieveGameOutput(new InputStreamReader(socket.getInputStream()));
			out = new SendCommands(socket);
		} catch (UnknownHostException uhe) {
			System.err.println("Host not found");
			uhe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("IO failure");
			ioe.printStackTrace();
		}
	}
	
	public static void main (String[] args) {
		Connect connection = new Connect("www.mortalpowers.com", 4000);
		connection.in.start();
		connection.out.start();
	}
}