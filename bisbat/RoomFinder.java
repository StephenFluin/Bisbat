package bisbat;

import java.util.ArrayList;
import java.util.LinkedList;;

public class RoomFinder extends Thread {
	
	private LinkedList<Room> foundRooms;
	public LinkedList<String> commandList;
	private boolean failure = false;
	private Bisbat bisbat = null;
	
	public RoomFinder(Bisbat bisbat) {
		this.bisbat = bisbat;
	}
	
	public void start() {
		foundRooms = new LinkedList<Room>();
		commandList = new LinkedList<String>();
	}
	
	public void add(Room room) {
		foundRooms.add(room);
		
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
		//Bisbat.print("Waiting for a room."); // debugger
		while(foundRooms.size() <= 0) {
			try {
				if(failure) {
					commandList.removeFirst();
					failure = false;
					return null;
				}
				Thread.sleep(50); // Optimize with semaphores? !TODO
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		//Bisbat.debug("Found a room to pop."); // debugger
		if(!discovering) {
			return popFirstRoom();
		}
		if(bisbat.currentRoom == null) {
			bisbat.currentRoom = foundRooms.getFirst(); // look at me !TODO replace with removeFirst()?
		} else {
			Room temp = searchForMatchingRoom(bisbat.currentRoom, foundRooms.getFirst(), 
					commandList.getFirst(), discovering);
			if(temp != null) {
				// System.out.println("We found a room that matched!  Replacing " + foundRooms.getFirst().title + " with " + tmp.title); // debugger
				foundRooms.removeFirst();
				foundRooms.addFirst(temp);
			} else {
				//System.out.println("We didn't find a room that matched: " + foundRooms.getFirst().title); // debugger
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
		//Bisbat.print("searching for matching room"); // debugger
		
		LinkedList<Room> exploredRooms = new LinkedList<Room>();
		LinkedList<Room> searchQueue = new LinkedList<Room>();
		ArrayList<Exit> path;
		searchQueue.add(indexRoom);
		
		while(searchQueue.size() > 0) {
			Room currentRoom = searchQueue.removeFirst();
			if(!exploredRooms.contains(currentRoom)) {
				exploredRooms.add(currentRoom);
				
				//!TODO - Check for orthogonol room matches (grid areas).
				/*************** DO THIS FOR EVERY ROOM IN THE SEARCH QUEUE ***************/
				
				if( currentRoom.matchesRoom(findMe) ) {
					//System.out.println("We matched " + findMe.title + " and " + currentRoom.title + " -- BUT THIS IS WRONG!"); // debugger
					path = searchForPathBetweenRooms(indexRoom, currentRoom);
					path.add(0,new Exit(Exit.getOpposite(command)));
					if(path.size() > 2) {
						if(Exit.spatialRelativityCalculation(path) < .99) {
							//Bisbat.print("Found a room that matched."); // debugger
							return currentRoom;
						} else {
							//System.out.println("Probably was too far away."); // debugger
						}
					} else {
						//System.out.println("It wasn't far enough away to be the same."); // debugger
					}
				} else {
					//System.out.println("Didn't match because it didn't look the same."); // debugger
				}
				
				for(Exit e : currentRoom.exits) {
					if(e.nextRoom != null) {
						//System.out.println("We found another room to search");
						searchQueue.add(e.nextRoom);
					}
				}
				
				/*************** STOP DOING THIS FOR EVERY ROOM IN THE SEARCH QUEUE ***************/
			}
		}
		//Bisbat.print("Didn't find a room that matched."); // debugger
		return null;
	}
	
	/**
	 * Finds a path between two rooms
	 * @param start: intial room in search for destination room
	 * @param destination: destination room
	 */
	static public ArrayList<Exit> searchForPathBetweenRooms(Room start, Room destination) {
		return searchForPathBetweenRooms(start, destination, new ArrayList<Exit>(), new ArrayList<Room>());
	}
	
	/**
	 * recursive method that returns path between two rooms
	 * @param start: current room in search for destination room
	 * @param destination: destination room
	 * @param path: path from initial room to current room
	 * @param explored: list of already exlpored rooms (avoid duplicate checks)
	 */
	@SuppressWarnings("unchecked")
	static public ArrayList<Exit> searchForPathBetweenRooms(Room start, Room destination, ArrayList<Exit> path, ArrayList<Room> explored) {
		explored.add(start);
		if(start == destination) {
			return path;
		} else {
			
			for(Exit e : start.exits) {
				if(e.nextRoom != null && !explored.contains(e.nextRoom)) {
					ArrayList<Exit> nPath = (ArrayList<Exit>)path.clone();
					nPath.add(e);
					ArrayList<Exit> temp = searchForPathBetweenRooms(e.nextRoom, destination, nPath, explored);
					if (temp != null) {
						return temp;
					}
				}
			}
			
		}
		return null;
	}

	public void failure() {
		failure = true;
		
	}
	
}
