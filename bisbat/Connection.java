package bisbat;

import java.io.*;
import java.net.*;

public class Connection {
	
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
		if(s != null) {
			out.send(s);
		} else {
			System.out.println("The string I am trying to send is null.");
			throw new NullPointerException();
		}
		
		
	}
	
	public void sendNavigation(String command) {
		send(command);
		bisbat.roomFindingThread.add(command);
	}
	public void close() {
		try{
			socket.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}