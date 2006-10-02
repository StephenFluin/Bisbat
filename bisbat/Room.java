package bisbat;

import java.util.ArrayList;

public class Room {
	
	public Room(String rTitle, String rDescription, String rExits, String beingsAndObjects) {
		title = rTitle;
		description = rDescription;
		String[] exitList = rExits.split(", ");
		for(String s : exitList) {
			exits.add(new Exit(s));
		}
			
		
	}
	public ArrayList<Exit> exits = new ArrayList<Exit>();
	public String description = new String();
	public String title = new String();
	public ArrayList<Being> beings = new ArrayList<Being>();
	public ArrayList<Item> items = new ArrayList<Item>();
	
	
	//returns true if this room is indistinguishable from given room
	public boolean matchesRoom(Room room) {
		if (!title.equals(room.title)) return false;
		if (!description.equals(room.description)) return false;
		if (exits.size() != room.exits.size()) return false;
		Exit temp;
		for (Exit e : exits) {
			temp = room.getExit(e.direction);
			if (temp == null) return false;
			if (e.isDoor != room.getExit(e.direction).isDoor) return false;
		}
		
		// Should check adjacent rooms for difference too
		// Possibly a depth limited bredth first search
		// Without this, Bisbat will probably fail when presented with identical rooms
		
		return true; // can't find a difference
	}
	
	//updates information of this room to match given room
	public boolean update(Room room) {
		if (!matchesRoom(room)) return false; //rooms don't match
		beings = room.beings;
		items = room.items;
		Exit temp;
		for (Exit e : exits) {
			temp = room.getExit(e.direction);
			if (e.isDoor) e.isDoorOpen = temp.isDoorOpen;
		}
		return true; 
	}
	
	
	//prints a string representation to system.out 
	//this is a debugging method
	public void print() {
		System.out.println("\n" + title + "\n" + description);
		for (Exit e : exits) {
			System.out.print(e.getCommand() + "  ");
		} 
		System.out.print("\n\n");		
	}
	
	Exit getRandomUnexploredExit() {
		return getExit((int)Math.round(Math.random() * (exits.size() -1)));
		
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
	void printTree(String tabs) {
		System.out.println(tabs + title);
		for(Exit e : exits) {
			if(e.nextRoom != null && tabs.length() < 4) {
				e.nextRoom.printTree(tabs + "\t");
			}
		}
	}
	void printTree() {
		printTree("");
	}
	
}
