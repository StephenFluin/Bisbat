package bisbat;

import java.util.ArrayList;

public class Room {
	
	public Room(String rTitle, String rDescription, String rExits, String beingsAndObjects) {
		title = rTitle;
		description = rDescription;
		String[] exitList = rExits.split(", ");
		for(String s : exitList) {
			exits.add(s);
		}
		
		
		
	}
	public ArrayList<String> exits = new ArrayList<String>();
	public String description = new String();
	public String title = new String();
	public ArrayList<Being> beings = new ArrayList<Being>();
	public ArrayList<Item> items = new ArrayList<Item>();
	
	
	
}
