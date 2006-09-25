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
				this.sleep(2);
			} catch(Exception e) {
				
			}
		}
		return foundRooms.getFirst();
	}
}
