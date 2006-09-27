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
	public Exit(String s, Room nRoom) {
		nextRoom = nRoom;
		s = s.trim().toLowerCase(); // "West" != "west"
		if (s.charAt(0) == '[' || s.charAt(0) == '<') {
			isDoor = true;
			direction = s.substring(1, (s.length() - 1));
		} else {
			direction = s;
			isDoor = false;
		}
	}
	
	public String getExitCommand() {
		if (isDoor) {
			return "open " + direction + "\n" + direction;
		} else {
			return direction;
		}
	}
	
	
}
