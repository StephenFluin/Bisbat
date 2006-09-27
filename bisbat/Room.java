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
