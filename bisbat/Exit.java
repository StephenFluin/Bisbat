package bisbat;

import java.util.ArrayList;
import java.util.Hashtable;

public class Exit {

	public boolean isDoor;
	public boolean isDoorOpen;
	public Room nextRoom; // null if unknown
	public String direction;
	
	
	/**
	 * Constructs an exit.
	 * @param s The direction of the exit.
	 */
	public Exit(String s) {
		s = s.trim().toLowerCase(); // "West" != "west"
		if (s.charAt(0) == '[' || s.charAt(0) == '<') {
			isDoor = true;
			if (s.charAt(0) == '['){
				isDoorOpen = false;
			} else {isDoorOpen = true;}
			direction = s.substring(1, (s.length() - 1));
		} else {
			direction = s;
			isDoor = false;
			isDoorOpen = true; // opperates like an open door
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
		//System.out.println("Finding the opposite of " + command);
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
				//System.out.println("Adding single direction to list:" + direction);
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
				System.out.println("dir='" + dir + "', opDir ='" + Exit.getOpposite(dir) + "'.");
				e.printStackTrace();
			}
		}
		
		double result = 0.0f;
		for(String s: exitList.keySet()) {
			int distance = exitList.get(s);
			//System.out.println("Distance for " + s + " was " + distance + ".");
			result += distance*distance;
			
		}
		result = Math.sqrt(result);
		//System.out.println("We just did some cool spatial calculations, and we got: " + result );
		try{
			//Thread.sleep(300);
		} catch(Exception e) {
			
		}
		/*System.out.print("FYI, that was from path: ");
		for(Exit e : path) {
			System.out.print(e.getDirection() + ", ");
		}
		System.out.println();*/
		return result;
	}
	
	
}
