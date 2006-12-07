package bisbat;

import java.io.*;
import java.net.*;

public class SendCommands extends Thread {
	
	private BufferedReader reader;
	public PrintWriter out;
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
			if(command == null) {
				// Connection was severed.
				return;
			}
			while (true) {
					try{
						//if(command.equals("exit")) {
							//throw(new NullPointerException());
						//} else
						handleUserCommand(command);
						command = reader.readLine();
					} catch(Exception e) {
						// This happens only when we shut down the game.
						//e.printStackTrace();
						return;
					}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		out.println(command);
	}
	
	public void send(String s) {
		out.write(s + "\n");
		out.flush();
		//System.out.print("-> " + s + "\n");
	}
	
	public void handleUserCommand(String command) {
		if(command.equalsIgnoreCase("printTree")) {
			bisbat.currentRoom.printTree();
		} else if(command.equalsIgnoreCase("printToDo")) {
			Bisbat.print(bisbat.toDoList.toString());
		} else if(command.equalsIgnoreCase("moblist")) {
			for(Being b : bisbat.knownBeingList) {
				Bisbat.print(b.toString());
			}
		} else if(command.equalsIgnoreCase("sleep")) {
			bisbat.toDoList.add(new Pair<String,Object>("sleep",10));
		} else if (command.equalsIgnoreCase("printRoom")) {
			Bisbat.print(bisbat.currentRoom.toString());
		} else if (command.equalsIgnoreCase("printReference")) {
			Bisbat.print(bisbat.referenceRoom.toString());
		} else if (command.equalsIgnoreCase("foundRooms")) {
			Bisbat.print("" + bisbat.roomFindingThread.isEmpty());
		} else if (command.equalsIgnoreCase("reference")) {
			bisbat.toDoList.add(new Pair<String,Object>("reference",null));
		} else if (command.equalsIgnoreCase("return")) {
			bisbat.toDoList.add(new Pair<String,Object>("firstRoom",null));
		} else if(command.equalsIgnoreCase("count")) {
			System.out.println("Rooms known from the reference room:");
			bisbat.referenceRoom.printCount();
			System.out.println("Rooms known from the CURRENT room:");
			bisbat.currentRoom.printCount();
			
		} else {
			out.println(command);
		}
	}

}