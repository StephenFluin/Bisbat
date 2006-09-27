package bisbat;

import java.util.ArrayList;

public class Exit {

	public boolean isDoor;
	public Room nextRoom; // null if unknown
	public String direction;
	
	public Exit(String s) {
		s = s.trim().toLowerCase(); // "West" != "west"
		if (s.charAt(0) == '[' || s.charAt(0) == '<') {
			isDoor = true;
			direction = s.substring(1, (s.length() - 1));
		} else {
			direction = s;
			isDoor = false;
		}
	}
	/*public Exit(String s, Room nRoom) {
		nextRoom = nRoom;
		s = s.trim().toLowerCase(); // "West" != "west"
		if (s.charAt(0) == '[' || s.charAt(0) == '<') {
			isDoor = true;
			direction = s.substring(1, (s.length() - 1));
		} else {
			direction = s;
			isDoor = false;
		}
	}*/
	
	public String getCommand() {
		if (isDoor) {
			return "open " + direction + "\n" + direction;
		} else {
			return direction;
		}
	}
	public static String getOpposite(String command) {
		System.out.println("Finding the opposite of " + command);
		if(command.equals("west")) return "east";
		if(command.equals("east")) return "west";
		if(command.equals("north")) return "south";
		if(command.equals("south")) return "north";
		if(command.equals("northwest")) return "southeast";
		if(command.equals("southeast")) return "northwest";
		if(command.equals("northeast")) return "southwest";
		if(command.equals("southwest")) return "northeast";
		if(command.equals("up")) return "down";
		if(command.equals("down")) return "up";
		return null;
	}
	
	
}
