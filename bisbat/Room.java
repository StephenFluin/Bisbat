package bisbat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

public class Room {
	
	public Room(String rTitle, String rDescription, String rExits, Vector<Being> Beings, Vector<Item> Items) {
		title = rTitle;
		description = rDescription;
		String[] exitList = rExits.split(", ");
		for(String s : exitList) {
			exits.add(new Exit(s));
		}
		beings = Beings;
		items = Items;
	}
	
	public ArrayList<Exit> exits = new ArrayList<Exit>();
	public String description = new String();
	public String title = new String();
	public Vector<Being> beings = new Vector<Being>();
	public Vector<Item> items = new Vector<Item>();
	
	
	/**
	 * Returns true if this room is indistinguishable from given room.
	 */
	public boolean matchesRoom(Room room) {
		/* //debugger
		System.out.println("Findy room:");
		room.print();
		System.out.println("This room:");
		print();
		*/
		if (!title.equals(room.title)) {	
			return false;
		}
		if (!description.equals(room.description)) {
			return false;
		}
		if (exits.size() != room.exits.size()) {
			return false;
		}
		Exit temp;
		for (Exit e : exits) {
			temp = room.getExit(e.direction);
			if (temp == null) {
				return false;
			}
			if (e.isDoor != temp.isDoor) {
				return false;
			}
		}
		/*
		 Should check adjacent rooms for difference too
		 Possibly a depth limited breadth first search
		 Without this, Bisbat will probably fail when presented with identical rooms
		*/
		return true; // can't find a difference
	}
	
	/** 
	 * updates information about the current state of this room 
	 * using the given room.
	 * @param room: A room that has the update information about doors, items, and beings.
	 */
	public boolean update(Room room) {
		if (!matchesRoom(room)) return false; //rooms don't match
		beings = room.beings;
		items = room.items;
		Exit temp;
		for (Exit e : exits) {
			temp = room.getExit(e.direction);
			if (e.isDoor) {
				e.isDoorOpen = temp.isDoorOpen;
			}
		}
		return true; 
	}
	
	/**
	 * prints a string representation to system.out 
	 * !TODO this is a debugging method
	 */
	public void print() {
		System.out.println(toString());
		System.out.print("\n");		
	}
	
	public String toString() {
		String result = "TITLE:" + title + "\nDESCRIPTION:" + description + "\n";
		for(Exit e : exits) {
			result += e.getDirection() + "\t";
		}
		return result;
	}
	
	Exit getRandomUnexploredExit() {
		ArrayList<Exit> une = getUnexploredExits();
		if(une.size() == 0) {
			//System.out.println("Finished exploring this room."); // debugger
			return null; // all exits from this room have been explored
		}
		return une.get((int)Math.round(Math.random() * (une.size() -1)));		
	}
	
	Exit getExit(int i) {
		return exits.get(i);
		
	}
	Exit getExit(String s) {
		for(Exit e : exits) {
			if(e.direction.equals(s)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * Prints a tree representation of the mapped world from current location.
	 * Rooms found with a bredth-first-search from current room.
	 * Printed in depth-first-search reprentation for more a more intuitive mapping. !TODO
	 */
	void printTree(){
		int counter = 0;
		LinkedList<Room> exploredRooms = new LinkedList<Room>();
		LinkedList<Pair<Room,String>> searchQueue = new LinkedList<Pair<Room,String>>();
		searchQueue.add(new Pair<Room,String>(this, ""));
		
		while(searchQueue.size() > 0) {
			Pair<Room,String> currentPair = searchQueue.removeFirst();
			if(!exploredRooms.contains(currentPair.left)) {
				counter++;
				exploredRooms.add(currentPair.left);
				System.out.println(currentPair.right + currentPair.left.title + "(" 
						+ currentPair.left.getUnexploredExits().size() + "/"
						+ currentPair.left.exits.size() + ")"); // debugger
				for(Exit e : currentPair.left.exits) {
					if(e.nextRoom != null) {
						searchQueue.add(new Pair<Room,String>(e.nextRoom, currentPair.right + "  "));
					}
				}
			}
		}
		
		System.out.println("Total rooms:" + counter); // debugger
	}

	public ArrayList<Exit> getUnexploredExits() {
		ArrayList<Exit> une = new ArrayList<Exit>();
		for(Exit e : exits) {
			if(e.nextRoom == null) {
				une.add(e);
			}
		}
		return une;
	}
	
}
