package bisbat;

import java.util.ArrayList;

public class Bisbat extends Thread {

	public Connection c;
	public String name = "Bisbat";
	public String password = "alpha";
	private String prompt;
	public Room currentRoom;
	public RoomFinder roomFindingThread;
	public Bisbat() {
		prompt = "";
		roomFindingThread = new RoomFinder(this);
		roomFindingThread.start();
		
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
		currentRoom = roomFindingThread.pop();

	}
	

	public void explore() {
		
		// situated search: random walk
		// picks a random exit from the current room and goes that way
		try {
			while(true) {
				
				Exit chosenExit = currentRoom.getRandomUnexploredExit();
				
				c.send(chosenExit.getCommand());
				String otherExitDirection = Exit.getOpposite(chosenExit.getDirection());
				chosenExit.nextRoom = roomFindingThread.pop();
				Room previousRoom = currentRoom;
				currentRoom = chosenExit.nextRoom;
				//System.out.println("Other direction is: " + otherExitDirection + " and it should exist in the most recently found room.");
				currentRoom.getExit(otherExitDirection).nextRoom = previousRoom;
				
				
				this.sleep(2000); // wait awhile (medium walk)
				
				//System.out.println("Printing Current Room: ");
				//currentRoom.print(); debugger
				
			}
		} catch (Exception e) {
			System.err.println("Error in random walk"); 
			e.printStackTrace();
		}
		
	}
	public Room findRoomWithUnexploredExits(Room r) {
		
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
		return ".?" + getPrompt().replaceAll("%.", ".?.?");
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
		ArrayList<Exit> path = RoomFinder.searchForPathBetweenRooms(currentRoom, walkMeToHere);
		for(Exit e : path) {
			c.send(e.getCommand());
			roomFindingThread.pop();
		}
	}
	


}
