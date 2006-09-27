package bisbat;

import java.util.ArrayList;
import java.util.LinkedList;;

public class RoomFinder extends Thread {
	private LinkedList<Room> foundRooms;
	public void start() {
		foundRooms = new LinkedList<Room>();
	}
	public void add(Room r) {
		foundRooms.add(r);
	}
	public Room pop() {
		while(foundRooms.size() <= 0) {
			try {
				this.sleep(2); // opptimize by using wait() and notify()?
			} catch(Exception e) {
				
			}
		}
		return foundRooms.removeFirst(); // get first does not "pop" the list
	}
}
