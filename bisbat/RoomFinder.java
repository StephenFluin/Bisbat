package bisbat;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;;

public class RoomFinder extends Thread {
	
	
	
	private LinkedList<Room> foundRooms;
	private LinkedList<String> commandList;
	private Bisbat bisbat = null;
	public RoomFinder(Bisbat b) {
		bisbat = b;
	}
	public void start() {
		foundRooms = new LinkedList<Room>();
		commandList = new LinkedList<String>();
	}
	public void add(Room r) {
		foundRooms.add(r);
		
	}
	public void add(String command) {
		commandList.add(command);
	}
	public Room popFirstRoom() {
		commandList.removeFirst();
		return foundRooms.removeFirst();
	}
	public Room pop() {
		return pop(true);
	}
	public Room pop(boolean discovering) {

		while(foundRooms.size() <= 0) {
			try {
				Thread.sleep(250); // Optimize with semaphores? !TODO
			} catch(Exception e) {
				
			}
		}
		if(!discovering) {
			return popFirstRoom();

		}
		if(bisbat.currentRoom == null) {
			bisbat.currentRoom = foundRooms.getFirst();
		} else {
			Room tmp = searchForMatchingRoom(bisbat.currentRoom, foundRooms.getFirst(), commandList.getFirst(), discovering);
			if(tmp != null) {
				//System.out.println("We found a room that matched!  Replacing " + foundRooms.getFirst().title + " with " + tmp.title);
				foundRooms.removeFirst();
				foundRooms.addFirst(tmp);
			} else {
				//System.out.println("We didn't find a room that matched: " + foundRooms.getFirst().title);
			}
		}
		
		return popFirstRoom(); // get first does not "pop" the list
	}
	
	/**
	 *  Breadth-First-Search from a room matching findMe
	 *  Room needs to pass 3 tests.  It has to look like a room we know,
	 *  it has to be at least 2 away, and its spacial relativity must be less than 2.
	 */
	static public Room searchForMatchingRoom(Room indexRoom, Room findMe, String command, boolean discovering) {
		
		
		LinkedList<Room> exploredRooms = new LinkedList<Room>();
		LinkedList<Room> searchQueue = new LinkedList<Room>();
		ArrayList<Exit> path;
		
		searchQueue.add(indexRoom);
		
		while(searchQueue.size() > 0) {
			Room currentRoom = searchQueue.removeFirst();
			if(!exploredRooms.contains(currentRoom)) {
				exploredRooms.add(currentRoom);
				
				//!TODO - Check for orthogonol room matches (grid areas).
				/*************** DO THIS FOR EVERY ROOM IN THE SEARCH QUEUE */
				
				if( currentRoom.matchesRoom(findMe) ) {
					//System.out.println("We matched " + findMe.title + " and " + currentRoom.title + " -- BUT THIS IS WRONG!");
					path = searchForPathBetweenRooms(indexRoom, currentRoom);
					path.add(0,new Exit(Exit.getOpposite(command)));
					if(path.size() > 2) {
						if(Exit.spatialRelativityCalculation(path) < .99) {
							return currentRoom;
						} else {
							//System.out.println("Probably was too far away.");
						}

					} else {
						//System.out.println("It wasn't far enough away to be the same.");
					}
				} else {
					//System.out.println("Didn't match because it didn't look the same.");
				}
				
				
				for(Exit e : currentRoom.exits) {
					if(e.nextRoom != null) {
						//System.out.println("We found another room to search");
						searchQueue.add(e.nextRoom);
					}
				}
				/*************** STOP DOING THIS FOR EVERY ROOM IN THE SEARCH QUEUE */

			}
			
		}
		return null;
	}
	
	static public ArrayList<Exit> searchForPathBetweenRooms(Room start, Room dest) {
		return searchForPathBetweenRooms(start, dest, new ArrayList<Exit>(), new ArrayList<Room>());
	}
	
	/**
	 * Depth-First-Search for a path between two rooms.
	 */
	static public ArrayList<Exit> searchForPathBetweenRooms(Room start, Room dest, ArrayList<Exit> path, ArrayList<Room> explored) {
		explored.add(start);
		if(start == dest) {
			return path;
		} else {
			
			for(Exit e : start.exits) {
				if(e.nextRoom != null && !explored.contains(e.nextRoom)) {
					ArrayList<Exit> nPath = (ArrayList<Exit>)path.clone();
					nPath.add(e);
					ArrayList<Exit> tmp = searchForPathBetweenRooms(e.nextRoom, dest, nPath, explored);
					if (tmp != null) {
						return tmp;
					}
				}
			}
		}
		return null;
	}
	
}
