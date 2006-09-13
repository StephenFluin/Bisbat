/** NightShade */ /* Aug 14, 2006 */

package bisbat;

import java.io.*;
import java.net.*;

public class SendCommands extends Thread {
	/* Thread: Constantly prints user input to socket */
	
	private Socket socket;
	
	public SendCommands(Socket s) {
		socket = s;
	}
	
	public void run() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			String command = reader.readLine();
			while (!command.equals("exit")) {
				out.println(command);
				command = reader.readLine();
			}
			out.println(command);
		} catch (IOException ioe) {
			System.err.println("PrintInput Failed");
			ioe.printStackTrace();
		}
	}
}