package bisbat;

import java.io.*;
import java.net.*;

public class SendCommands extends Thread {
	
	private BufferedReader reader;
	PrintWriter out;
	public Bisbat bisbat;
	
	public SendCommands(Socket socket, Bisbat bisbat) {
		this.bisbat = bisbat;
		try {
			reader = new BufferedReader(new InputStreamReader(System.in));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch(Exception e) {
			System.out.println("Can't set up the command sending thread.");
		}
	}
	
	public void run() {
		String command = null;
		try {
			command = reader.readLine();
			while (true) {
					if(command.equals("exit")) {
						throw(new NullPointerException());
					} else if(command.equals("printTree")) {
						bisbat.currentRoom.printTree();
					} else {
						out.println(command);
					}
					command = reader.readLine();
			}
			
		} catch (IOException ioe) {
			System.err.println("PrintInput Failed");
			ioe.printStackTrace();
		} catch (NullPointerException e) {
			System.err.println("Null pointer in Send Commands"); // We are probably done
			e.printStackTrace();
		}
		out.println(command);
	}
	
	public void send(String s) {
		out.write(s + "\n");
		out.flush();
		System.out.print("-> " + s + "\n");
	}

}