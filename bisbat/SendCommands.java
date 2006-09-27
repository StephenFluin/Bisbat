/** NightShade */ /* Aug 14, 2006 */

package bisbat;

import java.io.*;
import java.net.*;

public class SendCommands extends Thread {
	/* Thread: Constantly prints user input to socket */
	
	private Socket socket;
	private BufferedReader reader;
	PrintWriter out;
	public Bisbat bisbat;
	

	public SendCommands(Socket s, Bisbat b) {
		bisbat = b;
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
			if(command.equals("printTree")) {
				bisbat.currentRoom.printTree();
			}
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
		out.write(s + "\n");
		out.flush();
		System.out.print("->" + s + "\n");
	}
}