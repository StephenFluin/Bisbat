package bisbat;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Vector;

import java.util.GregorianCalendar;

public class Bisbat extends Thread {

	public Connection c;
	public String name = "Bisbat";
	public String password = "alpha";
	private String prompt;
	public Room currentRoom;
	public Vector<Being> beingList = new Vector<Being>();
	public Vector<Item> itemList = new Vector<Item>();
	public RoomFinder roomFindingThread;
	public ResultQueue resultQueue;
	public Bisbat() {
		prompt = "";
		roomFindingThread = new RoomFinder(this);
		roomFindingThread.start();
		resultQueue  = new ResultQueue();
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Bisbat alpha = new Bisbat();
		alpha.start();
		
		 
	}
	public void run() {
		c = new Connection(this, "www.mortalpowers.com", 4000);
	 	login();
	 	explore();
	}
	public void login() {
		c.send(name);
		c.send(password);
		setUpPrompt();
		resultQueue.pop();
		roomFindingThread.add("look");
		currentRoom = roomFindingThread.pop();

	}
	

	public void explore() {
		
		// situated search: random walk
		// picks a random exit from the current room and goes that way
		print("Starting exploration");
		String otherExitDirection = "";
		try {
			while(true) {
				//print("Explore loop entrance.");
				//currentRoom.printTree();
				Exit chosenExit = currentRoom.getRandomUnexploredExit();
				if(chosenExit == null) {
					walkToRoomIfKnown(findRoomWithUnexploredExits());
					chosenExit = currentRoom.getRandomUnexploredExit();
				}
				c.follow(chosenExit);
				otherExitDirection = Exit.getOpposite(chosenExit.getDirection());
				chosenExit.nextRoom = roomFindingThread.pop();
				Room previousRoom = currentRoom;
				currentRoom = chosenExit.nextRoom;
				//System.out.println("Other direction is: " + otherExitDirection + " and it should exist in the most recently found room.");
				currentRoom.getExit(otherExitDirection).nextRoom = previousRoom;
				
			
				
				//System.out.println("Printing Current Room: ");
				//currentRoom.print(); debugger
				
			}
		} catch(NullPointerException e) {
			System.out.println("Null Pointer\nCurrent Room Title: " + currentRoom.title + " otherExitDirection:" + otherExitDirection);
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Error in random walk"); 
			e.printStackTrace();
		} 
		print("Done exploration");
		
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
		// If there are no rooms with unexplored exits, then we are done mapping!
		return null;
	}
	public void setUpPrompt() {
		prompt = "<prompt>%c";
		c.send("prompt " + prompt);
		
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
		//System.out.println("We are walkToRoomIfKnown ing.");
		//System.out.println("Current KB:");
		//currentRoom.printTree();
		ArrayList<Exit> path = null;
		try {//System.out.println("I don't see a room '" + s + "'!");
			path = RoomFinder.searchForPathBetweenRooms(currentRoom, walkMeToHere);
		} catch(OutOfMemoryError e) {
			System.out.println("We had a wee bit of trouble searching for the path between two rooms. OUT OF MEMORY!");
		}
		//System.out.println("Following a path from " + currentRoom.title + " to  " + walkMeToHere.title);
		for(Exit e : path) {
			c.follow(e);
			
			
			roomFindingThread.pop(false);
			currentRoom = e.nextRoom;
			//currentRoom.printTree();
			
		}
		//System.out.println("Done following the path.");
	}
	static GregorianCalendar cal;
    static public void print(String string) {
    	cal = new GregorianCalendar();
        System.out.println("(" + cal.getTime().toString() + ") "  + string);
    }

	public void addKnowledgeOf(Item i) {
		if(itemList.contains(i)) {
			
		} else {
			itemList.add(i);
		}
		
	}
	public void addKnowledgeOf(Being b) {
		if(beingList.contains(b)) {
			
		} else {
			considerAndGuessName(b);
			beingList.add(b);
		}
	}

	private void considerAndGuessName(Being b) {
		System.out.println("COnsidering and GUessing! V2.0");
		while(!b.isSureOfName()) {
			c.send("consider " + b.guessName());
			String result = resultQueue.pop();
			System.out.println("Jujubeans go pop");
			if(result.equals("You don't see that here.")) {
				b.setGuessResult(false);
			} else {
				b.setGuessResult(true);
			}
		}
		
	}
	


}
