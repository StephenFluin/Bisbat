/** NightShade */ /* Aug 14, 2006 */

package bisbat;

import java.io.*;
import java.net.*;

public class SendCommands extends Thread {
	/* Thread: Constantly prints user input to socket */
	
	private Socket socket;
	private BufferedReader reader;
	PrintWriter out;
	
	public SendCommands(Socket s) {
		socket = s;
		try {
			reader = new BufferedReader(new InputStreamReader(System.in));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch(Exception e) {
			System.out.println("Can't set up the command sending thread.");
		}
	}
	
	public void run() {
		try {
			
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
	public void send(String s) {
		out.write(s);
		out.flush();
		System.out.print(s);
	}
}