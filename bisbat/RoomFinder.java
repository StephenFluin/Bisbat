package bisbat;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;;

public class RoomFinder extends Thread {
	
	
	
	private LinkedList<Room> foundRooms;
	private Bisbat bisbat = null;
	public RoomFinder(Bisbat b) {
		bisbat = b;
	}
	public void start() {
		foundRooms = new LinkedList<Room>();
	}
	public void add(Room r) {
		foundRooms.add(r);
	}
	public Room pop() {
		while(foundRooms.size() <= 0) {
			try {
				Thread.sleep(750); // Optimize with semaphores? !TODO
			} catch(Exception e) {
				
			}
		}
		if(bisbat.currentRoom == null) {
			bisbat.currentRoom = foundRooms.getFirst();
		} else {
			Room tmp = searchForMatchingRoom(bisbat.currentRoom, foundRooms.getFirst());
			if(tmp != null) {
				System.out.println("We found a room that matched!  Replacing " + foundRooms.getFirst().title + " with " + tmp.title);
				foundRooms.removeFirst();
				foundRooms.addFirst(tmp);
			}
		}
		
		return foundRooms.removeFirst(); // get first does not "pop" the list
	}
	
	/**
	 *  Breadth-First-Search from a room matching findMe
	 */
	static public Room searchForMatchingRoom(Room indexRoom, Room findMe) {
		
		
		LinkedList<Room> exploredRooms = new LinkedList<Room>();
		LinkedList<Room> searchQueue = new LinkedList<Room>();
		searchQueue.add(indexRoom);
		
		while(searchQueue.size() > 0) {
			Room currentRoom = searchQueue.removeFirst();
			if(!exploredRooms.contains(currentRoom)) {
				exploredRooms.add(currentRoom);
				if(currentRoom.matchesRoom(findMe)) {
					return currentRoom;
				} else {
					for(Exit e : currentRoom.exits) {
						if(e.nextRoom != null) {
							searchQueue.add(e.nextRoom);
						}
					}
				}
			}
			
		}
		return null;
	}
	
	static public ArrayList<Exit> searchForPathBetweenRooms(Room start, Room dest) {
		return searchForPathBetweenRooms(start, dest, new ArrayList<Exit>());
	}
	
	/**
	 * Depth-First-Search for a path between two rooms.
	 */
	static public ArrayList<Exit> searchForPathBetweenRooms(Room start, Room dest, ArrayList<Exit> path) {
		if(start == dest) {
			return path;
		} else {
			for(Exit e : start.exits) {
				if(e.nextRoom != null) {
					ArrayList<Exit> nPath = (ArrayList<Exit>)path.clone();
					nPath.add(e);
					ArrayList<Exit> tmp = searchForPathBetweenRooms(e.nextRoom, dest, nPath);
					if (tmp != null) {
						return tmp;
					}
				}
			}
		}
		return null;
	}
	
}
