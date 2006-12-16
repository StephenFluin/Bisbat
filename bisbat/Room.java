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
	
	/**
	 * The list of actual beings in the room.
	 */
	public Vector<Being> beings = new Vector<Being>();
	public Vector<Item> items = new Vector<Item>();
	boolean confirmed = false;
	
	/**
	 * Determines if two rooms could be the same.
	 * @param room: room that might match this room.
	 * @return: True if this room is undistinguishable from given room.
	 */
	public boolean matchesRoom(Room room) {
		if (room == null) {
			return true; // null room matches everything
		} else if (!title.equals(room.title)) {	
			return false;
		} else if (!description.equals(room.description)) {
			return false;
		} else if (exits.size() != room.exits.size()) {
			return false;
		} else {
			Exit temp;
			for (Exit e : exits) {
				temp = room.getExit(e.direction);
				if (temp == null) {
					return false;
				} else if (e.isDoor != temp.isDoor) {
					return false;
				}
			}
		}
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
	 * confirmes this room as being a unique room in the knowledge base
	 * 
	 */
	public void confirm() {
		confirmed = true;
	}
	
	/**
	 * Returns true if room has been uniquely confirmed
	 * @return: true if confirmed, else false
	 */
	public boolean confirmed() {
		return confirmed;
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
		result += "\nBeings:";
		for(Being b : beings) {
			result += b.shortDesc + "\t";
		}
		result += "\nConfirmed: " + confirmed;
		return result;
	}
	
	public Exit getRandomUnexploredExit() {
		ArrayList<Exit> une = getUnexploredExits();
		if(une.size() == 0) {
			//System.out.println("Finished exploring this room."); // debugger
			return null; // all exits from this room have been explored
		}
		return une.get((int)Math.round(Math.random() * (une.size() -1)));		
	}
	
	public Exit getRandomConfirmedExit() {
		ArrayList<Exit> ce = new ArrayList<Exit>();
		for(Exit e : exits) {
			if(e.confirmed) {
				ce.add(e);
			}
		}
		if (ce.isEmpty()) {
			return null;
		} else {
			return ce.get((int)Math.round(Math.random() * (ce.size() -1)));	
		}
	}
	
	public Exit getExit(int i) {
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
						+ currentPair.left.getUnconfirmedExits().size() + "/"
						+ currentPair.left.exits.size() + "/"
						+ currentPair.left.confirmed + ")"); // debugger
				for(Exit e : currentPair.left.exits) {
					if(e.nextRoom != null) {
						searchQueue.add(new Pair<Room,String>(e.nextRoom, currentPair.right + "  "));
					}
				}
			}
		}
		System.out.println("Total rooms:" + counter); // debugger
	}
	
	
	/**
	 * Prints the number of rooms from this room
	 */
	void printCount(){
		int counter = 0;
		LinkedList<Room> exploredRooms = new LinkedList<Room>();
		LinkedList<Pair<Room,String>> searchQueue = new LinkedList<Pair<Room,String>>();
		searchQueue.add(new Pair<Room,String>(this, ""));
		
		while(searchQueue.size() > 0) {
			Pair<Room,String> currentPair = searchQueue.removeFirst();
			if(!exploredRooms.contains(currentPair.left)) {
				counter++;
				exploredRooms.add(currentPair.left);
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
	
	public ArrayList<Exit> getConfirmedExits() {
		ArrayList<Exit> une = new ArrayList<Exit>();
		for(Exit e : exits) {
			if(e.isConfirmed()) {
				une.add(e);
			}
		}
		return une;
	}
	
	public Room roomAfterPath(LinkedList<String> path) {
		if (path.isEmpty()) {
			return this;
		}
		if (getExit(path.getFirst()) == null) {
			return null;
		}
		Room next = getExit(path.getFirst()).nextRoom;
		if (next == null) {
			return null;
		} else {
			LinkedList<String> nextPath = new LinkedList<String>(path);
			nextPath.removeFirst();
			return next.roomAfterPath(nextPath);
		}
	}
	
	public ArrayList<Exit> getUnconfirmedExits() {
		ArrayList<Exit> une = new ArrayList<Exit>();
		for(Exit e : exits) {
			if(!e.isConfirmed()) {
				une.add(e);
			}
		}
		return une;
	}
	
	
}
