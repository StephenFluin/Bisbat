package bisbat;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Vector;

public class Bisbat extends Thread {

	static GregorianCalendar cal;
	
	public Connection connection;
	public String name;
	public String password;
	private String prompt;
	public Room currentRoom;
	public Vector<Being> knwonBeingList;
	public Vector<Item> knownItemList;
	public RoomFinder roomFindingThread;
	public ResultQueue resultQueue;
	
	/**
	 * Constructs a new instance of Bisbat
	 * @param loginName: Name of this instance of Bisbat
	 * @param loginPassword: Password used by this instance of Bisbat to login to the game
	 */
	public Bisbat(String loginName, String loginPassword) {
		name = loginName;
		password = loginPassword;
		prompt = "";
		knwonBeingList = new Vector<Being>();
		knownItemList = new Vector<Item>();
		roomFindingThread = new RoomFinder(this);
		roomFindingThread.start();
		resultQueue  = new ResultQueue();
	}

	public void run() {
		connection = new Connection(this, "www.mortalpowers.com", 4000);
	 	login();
	 	explore();
	}
	
	public void login() {
		connection.send(name);
		connection.send(password);
		setUpPrompt();
		resultQueue.pop();
		roomFindingThread.add("look");
		currentRoom = roomFindingThread.pop();
	}
	
	public void explore() {	
		print("Starting exploration"); // debugger
		String otherExitDirection = "";
		try {
			while(true) {
				//print("Explore loop entrance."); // debugger
				//currentRoom.printTree(); // debugger
				Exit chosenExit = currentRoom.getRandomUnexploredExit();
				if(chosenExit == null) {
					walkToRoomIfKnown(findRoomWithUnexploredExits());
					chosenExit = currentRoom.getRandomUnexploredExit();
				}
				connection.follow(chosenExit);
				otherExitDirection = Exit.getOpposite(chosenExit.getDirection());
				chosenExit.nextRoom = roomFindingThread.pop();
				Room previousRoom = currentRoom;
				currentRoom = chosenExit.nextRoom;
				//System.out.println("Other direction is: " + otherExitDirection + " and it should exist in the most recently found room."); // debugger
				currentRoom.getExit(otherExitDirection).nextRoom = previousRoom;
				//System.out.println("Printing Current Room: "); // debugger
				//currentRoom.print(); // debugger
			}
		} catch(NullPointerException e) {
			System.err.println("Null Pointer\nCurrent Room Title: " + currentRoom.title + " otherExitDirection:" + otherExitDirection);
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Error in random walk"); 
			e.printStackTrace();
		} 
		print("Done exploration"); // debugger
	}
	
	public Room findRoomWithUnexploredExits() {
		LinkedList<Room> exploredRooms = new LinkedList<Room>();
		LinkedList<Room> searchQueue = new LinkedList<Room>();
		searchQueue.add(currentRoom);
		
		while(searchQueue.size() > 0) {
			Room currentRoom = searchQueue.removeFirst();
			if(!exploredRooms.contains(currentRoom)) {
				exploredRooms.add(currentRoom);
				if(currentRoom.getUnexploredExits().size() > 0) {
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
		return null; // no unexplored exits found in knowledge base
	}
	
	public void setUpPrompt() {
		prompt = "<prompt>%c";
		connection.send("prompt " + prompt);
		
	}
	public String getPrompt() {
		return prompt;
	}
	public String getPromptMatch() {
		String s = getPrompt().replaceAll("%c", ".?.?");
		s = s.replaceAll("%.", "\\\\d+");
		return s;
	}

	public void foundRoom(Room recentlyDiscoveredRoom) {
		roomFindingThread.add(recentlyDiscoveredRoom);
	}
	
	/**
	 * This method will be used to walk to a room that has unexplored exits, or
	 * whenever we want to walk to a destination room.
	 * @param walkMeToHere
	 */
	public void walkToRoomIfKnown(Room walkMeToHere) {
		//System.out.println("We are walkToRoomIfKnown ing."); // debugger
		//System.out.println("Current KB:"); // debugger
		//currentRoom.printTree(); // debugger
		ArrayList<Exit> path = null;
		try {
			//System.out.println("I don't see a room '" + s + "'!"); // debugger
			path = RoomFinder.searchForPathBetweenRooms(currentRoom, walkMeToHere);
		} catch(OutOfMemoryError e) {
			System.out.println("We had a wee bit of trouble searching for the path between two rooms. OUT OF MEMORY!");
		}
		//System.out.println("Following a path from " + currentRoom.title + " to  " + walkMeToHere.title); // debugger
		for(Exit e : path) {
			connection.follow(e);
			roomFindingThread.pop(false);
			currentRoom = e.nextRoom;
			//currentRoom.printTree(); // debugger
		}
		//System.out.println("Done following the path."); // debugger
	}
	
	public void addKnowledgeOf(Item i) {
		if(!knownItemList.contains(i)) {
			knownItemList.add(i);
		}
	}
	
	public void addKnowledgeOf(Being b) {
		if(!knwonBeingList.contains(b)) {
			considerAndGuessName(b);
			knwonBeingList.add(b);
		}
	}

	// look at me!
	private void considerAndGuessName(Being being) {
		System.out.println("COnsidering and GUessing! V2.0"); // debugger
		while(!being.isSureOfName()) {
			connection.send("consider " + being.guessName());
			String result = resultQueue.pop();
			if(result.equals("You don't see that here.")) {
				being.setGuessResult(false);
			} else {
				being.setGuessResult(true);
			}
		}
		
	}
	
	/**
	 * Prints the time with given string
	 * @param string: prints this string along with the current time
	 */
    static public void print(String string) {
    	cal = new GregorianCalendar();
        System.out.println("(" + cal.getTime().toString() + ") "  + string);
    }
	
	/**
	 * Creates and runs default instance of Bisbat
	 * @param args
	 */
	public static void main(String[] args) {
		Bisbat alpha = new Bisbat("Bisbat", "alpha");
		alpha.start(); 
	}
	


}
