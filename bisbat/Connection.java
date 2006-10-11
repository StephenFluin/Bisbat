/** NightShade */ /* Aug 14, 2006 */

package bisbat;

import java.io.*;
import java.net.*;

public class Connection {
	/* Opens a connection to a socket
	 * Prints input to/from system and socket */
	
	private Socket socket;
	private RecieveGameOutput in;
	private SendCommands out;
	private Bisbat bisbat;
	
	public Connection(Bisbat bisbat, String s, int port) {
		this.bisbat = bisbat;
		try {
			socket = new Socket(s, port);
			in = new RecieveGameOutput(bisbat, new InputStreamReader(socket.getInputStream()));
			out = new SendCommands(socket, bisbat);
		} catch (UnknownHostException uhe) {
			System.err.println("Host not found");
			uhe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("IO failure");
			ioe.printStackTrace();
		}
		in.start();
		out.start();
	}
	public void send(String s) {
		if(s == null) {
			System.out.println("The string I am trying ot send is null.");
		}
		out.send(s);
	}
	public void sendNavigation(String command) {
		send(command);
		bisbat.roomFindingThread.add(command);
	}
	public void follow(Exit chosenExit) {
		String dCommand = chosenExit.getDoorCommand();
		if(dCommand != null) {
			send(dCommand);
		}
		sendNavigation(chosenExit.direction);
		
		
	}
}