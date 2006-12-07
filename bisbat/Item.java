package bisbat;

import java.util.Vector;

public class Item {
	
	public String name, shortDesc, longDesc;
	public String type;
	public Vector<Room> seenIn;
	
	public Item(String string) {
		longDesc = string;
	}
	public boolean equals(Item other) {
		return longDesc.equalsIgnoreCase(other.longDesc);
	}
	public String getShort() {
		return shortDesc;
	}
}
