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
	
	public boolean isEmpty() {
		return foundRooms.isEmpty();
	}
	public Room getLastRoom() {
		if(foundRooms.isEmpty()){
			Bisbat.debug("RoomFinder.foundRooms.isEmpty = true");
			return null;
		}
		return foundRooms.getLast();
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
			} catch(InterruptedException e) {
				Bisbat.debug("InterruptedException: ... not sure what's causing this one!");
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
				
				if(currentRoom.matchesRoom(findMe) ) {
					//System.out.println("We matched " + findMe.title + " and " + currentRoom.title + " -- BUT THIS IS WRONG!"); // debugger
					path = searchForPathBetweenRooms(indexRoom, currentRoom);
					path.add(0,new Exit(Exit.getOpposite(command)));
					if(path.size() > 2) {
						//Bisbat.debug("Using spatial relativity calculation");
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
	 * Finds every room in the knowledge base that matches this given room
	 * @param indexRoom: room to start search from (knowledge base)
	 * @param findMe: room that we are searching for matches of
	 * @return: all rooms that might actually be the given room findMe
	 */
	static public LinkedList<Room> searchForAllMatchingRooms(Room indexRoom, Room findMe) {
		LinkedList<Room> result = new LinkedList<Room>();
		LinkedList<Room> exploredRooms = new LinkedList<Room>();
		LinkedList<Room> searchQueue = new LinkedList<Room>();
		searchQueue.add(indexRoom);
		
		while(searchQueue.size() > 0) {
			Room currentRoom = searchQueue.removeFirst();
			if(!exploredRooms.contains(currentRoom)) {
				exploredRooms.add(currentRoom);
				if(currentRoom.matchesRoom(findMe) ) {
					result.add(currentRoom);
				}
				for(Exit e : currentRoom.exits) {
					if(e.nextRoom != null) {
						searchQueue.add(e.nextRoom);
					}
				}
			}
		}
		//Bisbat.debug("matching rooms: " + result.size() + " Number of considered rooms: " + exploredRooms.size());
		return result;
	}
	
	/**
	 * Finds a path to an unconfirmed room if there is an unconfirmed room.
	 * @param start: room to start search room
	 * @return: path to an unconfirmed room if one exists
	 */
	static public ArrayList<Exit> searchForPathToUnconfirmedRoom(Room start) {
		return searchForPathToUnconfirmedRoom(start, new ArrayList<Exit>(), new ArrayList<Room>());
	}
	
	/**
	 * Finds a path to an unconfirmed room if there is an unconfirmed room.
	 * @param start: Room to start looking for the path
	 * @param path: Path to start room
	 * @param explored: rooms that have been consider (eleminate repeat searches)
	 * @return: path to an unconfirmed room if one exists
	 */
	@SuppressWarnings("unchecked")
	static private ArrayList<Exit> searchForPathToUnconfirmedRoom(Room start, ArrayList<Exit> path, ArrayList<Room> explored) {
		explored.add(start);
		if(!start.confirmed()) {
			return path;
		} else {
			for(Exit e : start.exits) {
				if(e.nextRoom != null && !explored.contains(e.nextRoom)) {
					ArrayList<Exit> nPath = (ArrayList<Exit>)path.clone();
					nPath.add(e);
					ArrayList<Exit> temp = searchForPathToUnconfirmedRoom(e.nextRoom, nPath, explored);
					if (temp != null) {
						return temp;
					}
				}
			}
		}
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
	static private ArrayList<Exit> searchForPathBetweenRooms(Room start, Room destination, ArrayList<Exit> path, ArrayList<Room> explored) {
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
	
	/**
	 * Finds directions that have been confirmed from every room in 'rooms'.
	 * @param rooms: set of rooms we want to find common confirmed directions from
	 * @return: a list of the common directions.
	 */
	static public LinkedList<String> commonConfirmedDirection(LinkedList<Room> rooms) {
		//Bisbat.debug("entering commonConfirmedDirection");
		if (rooms == null || rooms.isEmpty()) {
			//Bisbat.debug("exiting commonConfirmedDirection at exit 1");
			return null;
		} else if (rooms.size() == 1) {
			LinkedList<String> result = new LinkedList<String>();
			//Bisbat.debug("commonConfirmedDirection line 1");
			ArrayList<Exit> commonExits = rooms.getFirst().getConfirmedExits();
			//Bisbat.debug("commonConfirmedDirection line 2 " + commonExits.size());
			for(Exit exit : commonExits) {
				result.add(exit.getDirection());
			}
			//Bisbat.debug("commonConfirmedDirection line 2 " + result.size());
			//Bisbat.debug("exiting commonConfirmedDirection at exit 2");
			return result;
		}
		LinkedList<String> result = new LinkedList<String>();
		ArrayList<Exit> exits = rooms.getFirst().getConfirmedExits();
		for(Exit exit : exits) {
			String dir = exit.getDirection();
			for(Room room : rooms) {
				Exit ex = room.getExit(dir);
				if (ex == null || !ex.isConfirmed()) {
					continue;
				}
			}
			result.add(dir);
		}
		//Bisbat.debug("exiting commonConfirmedDirection at exit 3");
		return result;
	}
	
	/**
	 * Recusively finds the longest confirmed path (or a path of length 4) that 'rooms' share.
	 * @param rooms: rooms that we want to find a confirmed path from.
	 * @return: longest confirmed path (or path of length 4) from 'rooms'.
	 */
	static public LinkedList<String> commonConfirmedPath (LinkedList<Room> rooms){
		return commonConfirmedPath(rooms, 0, new LinkedList<String>());
	}
	
	/**
	 * Recusively finds the longest confirmed path (or a path of length 4) that 'rooms' share.
	 * @param rooms: rooms that we want to find a confirmed path from.
	 * @param depth: how far resursively we have traveled
	 * @param path: path to this point
	 * @return: longest confirmed path (or path of length 4) from 'rooms'.
	 */
	static private LinkedList<String> commonConfirmedPath(LinkedList<Room> rooms, int depth, LinkedList<String> path){
		if (depth >= 4) {
			return path;
		}
		LinkedList<String> commonDirections = commonConfirmedDirection(rooms);
		//Bisbat.debug("Number of matching rooms: " + rooms.size());
		if (commonDirections == null || commonDirections.isEmpty()) {
			return path;
		}
		//Bisbat.debug("Number of common confirmed directions: " + commonDirections.size() + " at depth: " + depth);
		LinkedList<LinkedList<String>> allPaths = new LinkedList<LinkedList<String>>();
		for (String dir : commonDirections) {
			LinkedList<Room> nextRooms = new LinkedList<Room>();
			for (Room room : rooms) {
				if (room != null && room.getExit(dir) != null) {
					Room temp = room.getExit(dir).nextRoom;
					if (temp != null) {
						nextRooms.add(temp);
					}
				}	
			}
			LinkedList<String> newPath = new LinkedList<String>(path);
			newPath.addLast(dir);
			allPaths.add(commonConfirmedPath(nextRooms, depth + 1, newPath));
		}
		LinkedList<String> longest = new LinkedList<String>();
		double spatialMax = 0.0d;
		for (LinkedList<String> list : allPaths) {
			if (list.size() > longest.size()) {
				longest = list;
			} else if (list.size() ==  longest.size()) {
				double temp = Exit.spatialRelativityCalculation(list);
				if (temp >= spatialMax) {
					if (Math.random() >= .2) {
						//Bisbat.debug("using a higher spatial relativity path");
						longest = list;
						spatialMax = temp;
					}
				}
			}
		}
		//Bisbat.debug("Longest confirming path <= 4 is this long: " + longest.size());\
		if (longest.isEmpty()) {
			Bisbat.debug("Turns out that these rooms have no Common Confirmed Path.");
		}
		return longest;
	}
	
	public void failure() {
		failure = true;	
	}
	
}
