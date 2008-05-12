package bisbat;

import java.util.ArrayList;
import java.util.Vector;
import java.util.Hashtable;

public class Exit {

	public boolean isDoor;
	public boolean isDoorOpen;
	public Room nextRoom; // null if unknown
	public String direction;
	private boolean hypothetical = true;
	
	public void confirm() {
		hypothetical = false;
	}
	public boolean isConfirmed() {
		return !hypothetical;
	}
	
	/**
	 * Constructs an exit.
	 * @param dir: The direction of the exit.
	 */
	public Exit(String dir) {
		dir = dir.trim().toLowerCase(); // since "West" != "west"
		hypothetical = true;
		if (dir.charAt(0) == '[' || dir.charAt(0) == '<') {
			isDoor = true;
			if (dir.charAt(0) == '['){
				isDoorOpen = false;
			} else {isDoorOpen = true;}
			direction = dir.substring(1, (dir.length() - 1));
		} else {
			direction = dir;
			isDoor = false;
			isDoorOpen = true;
		}
	}
	
	public String getDoorCommand() {
		if (isDoor) {
			return "open " + direction;
		} else {
			return null;
		}
	}
	
	public String getDirection() {
		return direction;
	}
	
	public static String getOpposite(String command) {
		//System.out.println("Finding the opposite of " + command); // debugger
		if(command == null) return null;
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


	public static double spatialRelativityCalculation(Vector<String> path) {
		ArrayList<Exit> dummy = new ArrayList<Exit>();
		for (String dir : path) {
			Exit temp = new Exit(dir);
			dummy.add(temp);
		}
		return spatialRelativityCalculation(dummy);
	}
	
	public static double spatialRelativityCalculation(ArrayList<Exit> path) {
		Hashtable<String,Integer> exitList = new Hashtable<String,Integer>();
		String direction;
		ArrayList<String> directionList = new ArrayList<String>();

		for(Exit e : path) {
			direction = e.getDirection();
			if((direction.length() > 6) && (direction.contains("east") || direction.contains("west"))) {
				directionList.add(direction.substring(0,5));
				directionList.add(direction.substring(5));
			} else {
				directionList.add(direction);
				//System.out.println("Adding single direction to list:" + direction); // debugger
			}
		}
		
		for(String dir : directionList) {
			try{
				if(exitList.containsKey(dir) || exitList.containsKey(Exit.getOpposite(dir))) {
					if(exitList.containsKey(dir)) {
						exitList.put(dir, exitList.get(dir).intValue()+1);
					} else {
						exitList.put(Exit.getOpposite(dir), exitList.get(Exit.getOpposite(dir)).intValue()-1);
					}
				} else {
					exitList.put(dir, 1);
				}
			} catch (NullPointerException e) {
				System.err.println("dir='" + dir + "', opDir ='" + Exit.getOpposite(dir) + "'.");
				e.printStackTrace();
			}
		}
		
		double result = 0.0d;
		for(String s: exitList.keySet()) {
			int distance = exitList.get(s);
			//System.out.println("Distance for " + s + " was " + distance + "."); // debugger
			result += distance * distance;
		}
		result = Math.sqrt(result);
		//System.out.println("We just did some cool spatial calculations, and we got: " + result ); // debugger
		
		return result;
	}

	/**
	 * Tests to see if the exits re both doors, or both not doors, and that
	 * other is not null.
	 * @param other
	 * @return Whether or not the exits could be equal.
	 */
	public boolean equals(Exit other) {

		if (other == null) {
			return false;
		} else if (isDoor != other.isDoor) {
			return false;
		}
		return true;
	}
	
	
}
